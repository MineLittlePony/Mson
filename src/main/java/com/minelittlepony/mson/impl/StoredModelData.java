package com.minelittlepony.mson.impl;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.util.Identifier;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.minelittlepony.mson.api.FutureSupplier;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.exception.FutureAwaitException;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.impl.model.JsonCompound;
import com.minelittlepony.mson.impl.model.JsonLink;
import com.minelittlepony.mson.impl.model.JsonTexture;
import com.minelittlepony.mson.util.JsonUtil;
import com.minelittlepony.mson.util.Maps;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class StoredModelData implements JsonContext {

    private final ModelFoundry foundry;

    private final Map<String, JsonComponent<?>> elements = new HashMap<>();

    private final CompletableFuture<JsonContext> parent;

    private final JsonContext.Locals variables;

    StoredModelData(ModelFoundry foundry, Identifier id, JsonObject json) {
        this.foundry = foundry;
        parent = JsonUtil.accept(json, "parent")
            .map(JsonElement::getAsString)
            .map(Identifier::new)
            .map(foundry::loadJsonModel)
            .orElseGet(() -> CompletableFuture.completedFuture(EmptyJsonContext.INSTANCE));

        variables = new RootVariables(id, json, parent.thenApplyAsync(JsonContext::getLocals));

        elements.putAll(getChildren(json).collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> loadComponent(entry.getKey(), entry.getValue(), JsonCompound.ID).orElseGet(null)
        )));
    }

    private Stream<Map.Entry<String, JsonElement>> getChildren(JsonObject json) {
        if (json.has("data")) {
            return json.get("data").getAsJsonObject().entrySet().stream().filter(entry -> {
                return entry.getValue().isJsonObject() || (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString());
            });
        }

        boolean[] warned = new boolean[1];

        return json.entrySet().stream().filter(entry -> {
            switch (entry.getKey()) {
                case "scale":
                case "dilate":
                case "parent":
                case "texture":
                case "data":
                case "locals":
                    return false;
                default:
                    if (!warned[0]) {
                        warned[0] = true;
                        MsonImpl.LOGGER.warn("Model {} is using a flat definition! This will be removed in 1.18. All structural components now belong under a `data` property", getLocals().getModelId());
                    }
                    return entry.getValue().isJsonObject()
                       || (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString());
            }
        });
    }

    @Override
    public CompletableFuture<Set<String>> getComponentNames() {
        return parent.thenComposeAsync(p -> p.getComponentNames()).thenApply(output -> {
            output.addAll(elements.keySet());
            return output;
        });
    }

    @Override
    public <T> void addNamedComponent(String name, JsonComponent<T> component) {
        if (!Strings.isNullOrEmpty(name)) {
            elements.put(name, component);
        }
    }

    @Override
    public <T> Optional<JsonComponent<T>> loadComponent(JsonElement json, Identifier defaultAs) {
        return loadComponent("", json, defaultAs);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<JsonComponent<T>> loadComponent(String name, JsonElement json, Identifier defaultAs) {
        if (json.isJsonObject()) {
            JsonObject o = json.getAsJsonObject();
            final String fname = Strings.nullToEmpty(name).trim();

            return Optional
                    .ofNullable(MsonImpl.INSTANCE.componentTypes.get(JsonUtil.accept(o, "type")
                            .map(JsonElement::getAsString)
                            .map(Identifier::new)
                            .orElse(defaultAs)))
                    .map(c -> (JsonComponent<T>)c.loadJson(this, fname, o));
        }
        if (json.isJsonPrimitive()) {
            JsonPrimitive prim = json.getAsJsonPrimitive();
            if (prim.isString()) {
                return Optional.of((JsonComponent<T>)new JsonLink(prim.getAsString()));
            }
        }

        throw new UnsupportedOperationException("Json was not a js object and could not be resolved to a js link");
    }

    @Override
    public CompletableFuture<JsonContext> resolve(JsonElement json) {

        if (json.isJsonPrimitive()) {
            return foundry.loadJsonModel(new Identifier(json.getAsString()));
        }

        Identifier id = getLocals().getModelId();
        Identifier autoGen = new Identifier(id.getNamespace(), id.getPath() + "_dynamic");

        if (json.getAsJsonObject().has("data")) {
            throw new JsonParseException("Model model files should not have a nested data block");
        }

        JsonObject file = new JsonObject();
        file.add("data", json.getAsJsonObject());

        return CompletableFuture.completedFuture(new StoredModelData(foundry, autoGen, file));
    }

    @Override
    public ModelContext createContext(Model model, ModelContext.Locals locals) {
        return new RootContext(model, parent.getNow(EmptyJsonContext.INSTANCE).createContext(model, locals), locals);
    }

    @Override
    public JsonContext.Locals getLocals() {
        return variables;
    }

    public static class RootVariables implements JsonLocalsImpl {
        private final Identifier id;
        private final CompletableFuture<JsonContext.Locals> parent;
        private final CompletableFuture<Texture> texture;
        private final CompletableFuture<float[]> dilate;

        private final Local.Block locals;

        RootVariables(Identifier id, JsonObject json, CompletableFuture<JsonContext.Locals> parent) {
            this.id = id;
            this.parent = parent;
            texture = JsonTexture.unlocalized(JsonUtil.accept(json, "texture"), parent.thenComposeAsync(Locals::getTexture));
            locals = Local.of(JsonUtil.accept(json, "locals"));

            boolean legacy = json.has("scale");
            if (legacy) {
                MsonImpl.LOGGER.warn("Model {} is using the `scale` property. This is deprecated and will be removed in 1.18. Please use `dilate`.", id);
            }
            dilate = JsonUtil.acceptFloats(json, legacy ? "scale" : "dilate", new float[3])
                    .map(CompletableFuture::completedFuture)
                    .orElseGet(() -> parent.thenComposeAsync(JsonContext.Locals::getDilation));
        }

        @Override
        public Identifier getModelId() {
            return id;
        }

        @Override
        public CompletableFuture<float[]> getDilation() {
            return dilate;
        }

        @Override
        public CompletableFuture<Texture> getTexture() {
            return texture;
        }

        @Override
        public CompletableFuture<Incomplete<Float>> getLocal(String name) {
            return locals.get(name).orElseGet(() -> parent.thenComposeAsync(p -> p.getLocal(name)));
        }

        @Override
        public CompletableFuture<Set<String>> keys() {
            return parent.thenComposeAsync(Locals::keys).thenApply(locals::appendKeys);
        }
    }

    public class RootContext implements ModelContext {

        private Model model;

        private final Map<String, Object> objectCache = new HashMap<>();

        private final ModelContext inherited;
        private final Locals locals;

        RootContext(Model model, ModelContext inherited, Locals locals) {
            this.model = model;
            this.inherited = inherited;
            this.locals = locals;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends Model> T getModel() {
            return (T)model;
        }

        public void setModel(Model model) {
            this.model = model;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getContext() {
            return (T)model;
        }

        @Override
        public Locals getLocals() {
            return locals;
        }

        @Override
        public void getTree(ModelContext context, Map<String, ModelPart> tree) {
            elements.entrySet().forEach(entry -> {
                if (!tree.containsKey(entry.getKey())) {
                    entry.getValue().tryExportTreeNodes(context, ModelPart.class).ifPresent(part -> {
                        tree.put(entry.getKey(), part);
                    });
                }
            });
            inherited.getTree(context, tree);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T findByName(ModelContext context, String name) {
            if (elements.containsKey(name)) {
                try {
                    return (T)elements.get(name).export(context);
                } catch (InterruptedException | ExecutionException e) {
                    throw new FutureAwaitException(e);
                }
            }
            return inherited.findByName(context, name);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T computeIfAbsent(String name, FutureSupplier<T> supplier) {
            Objects.requireNonNull(supplier);

            if (Strings.isNullOrEmpty(name)) {
                return supplier.apply(name);
            }

            return (T)Maps.computeIfAbsent(objectCache, name, supplier);
        }

        @Override
        public ModelContext resolve(Object child, Locals locals) {
            if (child == getContext() && locals == getLocals()) {
                return this;
            }
            return new SubContext(this, locals, child);
        }

        @Override
        public ModelContext getRoot() {
            return this;
        }
    }
}