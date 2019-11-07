package com.minelittlepony.mson.impl;

import net.minecraft.client.model.Cuboid;
import net.minecraft.client.model.Model;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import org.apache.commons.lang3.NotImplementedException;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.impl.model.JsonTexture;
import com.minelittlepony.mson.util.JsonUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
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

                    Identifier file = new Identifier(id.getNamespace(), "models/" + id.getPath() + ".json");

                    try (Resource res = manager.getResource(file);
                         Reader reader = new InputStreamReader(res.getInputStream(), Charsets.UTF_8)) {
                        return new StoredModelData(GSON.fromJson(reader, JsonObject.class));
                    } catch (IOException e) {
                        e.printStackTrace();
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

        private final Map<String, JsonComponent<?>> elements;

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


            texture = parent.thenComposeAsync(JsonContext::getTexture).thenApplyAsync(t -> new JsonTexture(json, t));
            elements = json.entrySet().stream().collect(Collectors.toMap(
                    entry -> entry.getKey(),
                    entry -> loadComponent(entry.getValue().getAsJsonObject()).orElseGet(null)
            ));
        }

        @Override
        public <T> void addNamedComponent(String name, JsonComponent<T> component) {
            elements.put(name, component);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> Optional<JsonComponent<T>> loadComponent(JsonElement json) {
            if (json.isJsonObject()) {
                JsonObject o = json.getAsJsonObject();
                Identifier id = new Identifier(JsonUtil.require(o, "id").getAsString());

                return Optional
                        .ofNullable(mson.componentTypes.get(id))
                        .map(c -> (JsonComponent<T>)c.loadJson(this, o));
            }

            throw new NotImplementedException("Json was not a js object");
        }

        @Override
        public CompletableFuture<Texture> getTexture() {
            return texture;
        }

        @Override
        public CompletableFuture<JsonContext> resolve(JsonElement json) {

            if (json.isJsonPrimitive()) {
                return loadJsonModel(new Identifier(json.getAsString()));
            }

            return CompletableFuture.completedFuture(new StoredModelData(json.getAsJsonObject()));
        }

        @Override
        public ModelContext createContext(Model model) {
            return new RootContext(model, scale, parent.getNow(NullContext.INSTANCE).createContext(model));
        }

        class RootContext implements ModelContext {

            private final Model model;

            private final Map<String, Object> objectCache = new HashMap<>();

            private final ModelContext inherited;

            private final float scale;

            RootContext(Model model, float scale, ModelContext inherited) {
                this.model = model;
                this.scale = scale;
                this.inherited = inherited;
            }

            @Override
            public Model getModel() {
                return model;
            }

            @Override
            public Object getContext() {
                return model;
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
                        throw new RuntimeException(e);
                    }
                }
                return inherited.findByName(name);
            }

            @Override
            public void findByName(String name, Cuboid output) {
                if (elements.containsKey(name)) {
                    try {
                        elements.get(name).export(this, output);
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
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
                return new SubContext(this, child);
            }

            @Override
            public ModelContext getRoot() {
                return this;
            }
        }
    }
}
