package com.minelittlepony.mson.impl;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.util.Identifier;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.exception.FutureAwaitException;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.impl.model.JsonCuboid;
import com.minelittlepony.mson.impl.model.JsonLink;
import com.minelittlepony.mson.impl.model.JsonTexture;
import com.minelittlepony.mson.util.Incomplete;
import com.minelittlepony.mson.util.JsonUtil;
import com.minelittlepony.mson.util.Maps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

class StoredModelData implements JsonContext {

    private final ModelFoundry foundry;

    private final Map<String, JsonComponent<?>> elements = new HashMap<>();

    private final CompletableFuture<JsonContext> parent;

    private final JsonContext.Variables variables;

    private float scale = -1;

    private final Identifier id;

    StoredModelData(ModelFoundry foundry, Identifier id, JsonObject json) {
        this.foundry = foundry;
        this.id = id;
        parent = JsonUtil.accept(json, "parent")
            .map(JsonElement::getAsString)
            .map(Identifier::new)
            .map(foundry::loadJsonModel)
            .orElseGet(() -> CompletableFuture.completedFuture(NullContext.INSTANCE));

        JsonUtil.accept(json, "scale")
            .map(JsonElement::getAsFloat)
            .ifPresent(scale -> this.scale = scale);

        variables = new RootVariables(json, parent.thenApplyAsync(JsonContext::getVariables));

        elements.putAll(json.entrySet().stream()
                .filter(this::isElement)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> loadComponent(entry.getKey(), entry.getValue(), JsonCuboid.ID).orElseGet(null)
        )));;
    }

    private boolean isElement(Map.Entry<String, JsonElement> entry) {
        switch (entry.getKey()) {
            case "scale":
            case "parent":
            case "texture":
            case "locals":
                return false;
            default:
                return entry.getValue().isJsonObject()
                   || (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString());
        }
    }

    @Override
    public Identifier getId() {
        return id;
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

    @SuppressWarnings("unchecked")
    private <T> Optional<JsonComponent<T>> loadComponent(String name, JsonElement json, Identifier defaultAs) {
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

        Identifier autoGen = new Identifier(getId().getNamespace(), getId().getPath() + "_dynamic");

        return CompletableFuture.completedFuture(new StoredModelData(foundry, autoGen, json.getAsJsonObject()));
    }

    @Override
    public ModelContext createContext(Model model, ModelContext.Locals locals) {
        return new RootContext(model, parent.getNow(NullContext.INSTANCE).createContext(model, locals), locals);
    }

    @Override
    public JsonContext.Variables getVariables() {
        return variables;
    }

    public static class RootVariables implements VariablesImpl {
        private final CompletableFuture<JsonContext.Variables> parent;
        private final CompletableFuture<Texture> texture;
        private final Map<String, Incomplete<Float>> locals;

        RootVariables(JsonObject json, CompletableFuture<JsonContext.Variables> parent) {
            this.parent = parent;
            texture = JsonTexture.unlocalized(JsonUtil.accept(json, "texture"), parent.thenComposeAsync(Variables::getTexture));
            locals = JsonUtil.accept(json, "locals")
                    .map(JsonElement::getAsJsonObject)
                    .map(JsonObject::entrySet)
                    .orElseGet(() -> new HashSet<>())
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> LocalsImpl.createLocal(e.getValue())));
        }

        @Override
        public CompletableFuture<Texture> getTexture() {
            return texture;
        }

        @Override
        public CompletableFuture<Incomplete<Float>> getVariable(String name) {
            if (locals.containsKey(name)) {
                return CompletableFuture.completedFuture(locals.get(name));
            }
            return parent.thenComposeAsync(p -> p.getVariable(name));
        }

        @Override
        public CompletableFuture<Set<String>> getKeys() {
            return parent.thenComposeAsync(Variables::getKeys).thenApply(output -> {
                output.addAll(locals.keySet());
                return output;
            });
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
        public float getScale() {
            if (scale > 0) {
                return scale;
            }
            return inherited.getScale();
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
        public <T> T computeIfAbsent(String name, ContentSupplier<T> supplier) {
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