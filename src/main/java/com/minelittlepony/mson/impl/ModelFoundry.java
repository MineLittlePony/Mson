package com.minelittlepony.mson.impl;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.json.Variables;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.impl.exception.FutureAwaitException;
import com.minelittlepony.mson.impl.model.JsonCuboid;
import com.minelittlepony.mson.impl.model.JsonLink;
import com.minelittlepony.mson.impl.model.JsonTexture;
import com.minelittlepony.mson.util.Incomplete;
import com.minelittlepony.mson.util.JsonUtil;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

class ModelFoundry {

    private static final Gson GSON = new Gson();

    private final MsonImpl mson;
    private final ResourceManager manager;

    private final Executor executor;

    private final Profiler serverProfiler;
    private final Profiler clientProfiler;

    private final Map<Identifier, CompletableFuture<JsonContext>> load = new HashMap<>();

    public ModelFoundry(ResourceManager manager, Executor executor, Profiler serverProfiler, Profiler clientProfiler, MsonImpl mson) {
        this.manager = manager;
        this.executor = executor;
        this.serverProfiler = serverProfiler;
        this.clientProfiler = clientProfiler;
        this.mson = mson;
    }

    public CompletableFuture<JsonContext> loadJsonModel(Identifier id) {
        synchronized (load) {
            if (!load.containsKey(id)) {
                load.put(id, CompletableFuture.supplyAsync(() -> {
                    serverProfiler.startTick();
                    clientProfiler.push("Loading MSON model - " + id);
                    MsonImpl.LOGGER.info("Loading MSON model - {}", id);

                    Identifier file = new Identifier(id.getNamespace(), "models/" + id.getPath() + ".json");

                    try (Resource res = manager.getResource(file);
                         Reader reader = new InputStreamReader(res.getInputStream(), Charsets.UTF_8)) {
                        return new StoredModelData(GSON.fromJson(reader, JsonObject.class));
                    } catch (Exception e) {
                        MsonImpl.LOGGER.error("Could not load model json for {}", file, e);
                    } finally {
                        clientProfiler.pop();
                        serverProfiler.endTick();
                    }

                    return NullContext.INSTANCE;
                }, executor));
            }
            return load.get(id);
        }
    }

    public JsonContext getModelData(ModelKey<?> key) throws InterruptedException, ExecutionException {
        return load.get(key.getId()).get();
    }

    class StoredModelData implements JsonContext {

        private final Map<String, JsonComponent<?>> elements = new HashMap<>();

        private final Map<String, Incomplete<Float>> locals;

        private final CompletableFuture<JsonContext> parent;

        private final CompletableFuture<Texture> texture;

        private float scale = -1;

        StoredModelData(JsonObject json) {
            parent = JsonUtil.accept(json, "parent")
                .map(JsonElement::getAsString)
                .map(Identifier::new)
                .map(ModelFoundry.this::loadJsonModel)
                .orElseGet(() -> CompletableFuture.completedFuture(NullContext.INSTANCE));

            JsonUtil.accept(json, "scale")
                .map(JsonElement::getAsFloat)
                .ifPresent(scale -> this.scale = scale);

            texture = JsonTexture.unlocalized(JsonUtil.accept(json, "texture"), parent.thenComposeAsync(JsonContext::getTexture));

            locals = JsonUtil.accept(json, "locals")
                    .map(JsonElement::getAsJsonObject)
                    .map(JsonObject::entrySet)
                    .orElseGet(() -> new HashSet<>())
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> LocalsImpl.createLocal(e.getValue())));

            elements.putAll(json.entrySet().stream()
                    .filter(this::isElement)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> loadComponent(entry.getValue(), JsonCuboid.ID).orElseGet(null)
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
                    return entry.getValue().isJsonObject();
            }
        }

        @Override
        public <T> void addNamedComponent(String name, JsonComponent<T> component) {
            if (!Strings.isNullOrEmpty(name)) {
                elements.put(name, component);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> Optional<JsonComponent<T>> loadComponent(JsonElement json, Identifier defaultAs) {
            if (json.isJsonObject()) {
                JsonObject o = json.getAsJsonObject();

                return Optional
                        .ofNullable(mson.componentTypes.get(JsonUtil.accept(o, "type")
                                .map(JsonElement::getAsString)
                                .map(Identifier::new)
                                .orElse(defaultAs)))
                        .map(c -> (JsonComponent<T>)c.loadJson(this, o));
            }
            if (json.isJsonPrimitive()) {
                JsonPrimitive prim = json.getAsJsonPrimitive();
                if (prim.isString()) {
                    return Optional.of((JsonComponent<T>)new JsonLink(prim.getAsString()));
                }
            }

            throw new UnsupportedOperationException("Json was not a js object and could not be resolved to  js link");
        }

        @Override
        public CompletableFuture<Texture> getTexture() {
            return texture;
        }

        @Override
        public CompletableFuture<Incomplete<Float>> getLocalVariable(String name) {
            if (locals.containsKey(name)) {
                return CompletableFuture.completedFuture(locals.get(name));
            }
            return parent.thenComposeAsync(p -> p.getLocalVariable(name));
        }

        @Override
        public CompletableFuture<JsonContext> resolve(JsonElement json) {

            if (json.isJsonPrimitive()) {
                return loadJsonModel(new Identifier(json.getAsString()));
            }

            return CompletableFuture.completedFuture(new StoredModelData(json.getAsJsonObject()));
        }

        @Override
        public ModelContext createContext(Model model, ModelContext.Locals locals) {
            return new RootContext(model, scale, parent.getNow(NullContext.INSTANCE).createContext(model, locals), locals);
        }

        @Override
        public Variables getVarLookup() {
            return VariablesImpl.INSTANCE;
        }

        class RootContext implements ModelContext {

            private final Model model;

            private final Map<String, Object> objectCache = new HashMap<>();

            private final ModelContext inherited;
            private final Locals locals;

            private final float scale;

            RootContext(Model model, float scale, ModelContext inherited, Locals locals) {
                this.model = model;
                this.scale = scale;
                this.inherited = inherited;
                this.locals = locals;
            }

            @Override
            public Model getModel() {
                return model;
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

            @SuppressWarnings("unchecked")
            @Override
            public <T> T findByName(String name) {
                if (elements.containsKey(name)) {
                    try {
                        return (T)elements.get(name).export(this);
                    } catch (InterruptedException | ExecutionException e) {
                        throw new FutureAwaitException(e);
                    }
                }
                return inherited.findByName(name);
            }

            @Override
            public void findByName(String name, ModelPart output) {
                if (elements.containsKey(name)) {
                    try {
                        elements.get(name).export(this, output);
                    } catch (InterruptedException | ExecutionException e) {
                        throw new FutureAwaitException(e);
                    }
                } else {
                    inherited.findByName(name, output);
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> T computeIfAbsent(String name, ContentSupplier<T> supplier) {
                if (Strings.isNullOrEmpty(name)) {
                    return supplier.apply(name);
                }
                return (T)objectCache.computeIfAbsent(name, supplier);
            }

            @Override
            public ModelContext resolve(Object child) {
                if (child == getContext()) {
                    return this;
                }
                return new SubContext(this, child);
            }

            @Override
            public ModelContext getRoot() {
                return this;
            }
        }
    }
}
