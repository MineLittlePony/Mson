package com.minelittlepony.mson.impl;

import net.minecraft.client.model.Cuboid;
import net.minecraft.client.model.Model;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class ModelFoundry {

    private static final Gson GSON = new Gson();

    private final MsonImpl mson;
    private final ResourceManager manager;

    private final Map<Identifier, JsonContext> contexts = new HashMap<>();

    public ModelFoundry(ResourceManager manager, MsonImpl mson) {
        this.manager = manager;
        this.mson = mson;
    }

    public JsonContext loadJsonModel(Identifier id) {
        synchronized (contexts) {
            if (contexts.containsKey(id)) {
                return contexts.get(id);
            }
        }

        Identifier file = new Identifier(id.getNamespace(), "models/" + id.getPath() + ".json");

        try (Resource res = manager.getResource(file)) {
            try (Reader reader = new InputStreamReader(res.getInputStream(), Charsets.UTF_8)) {
                JsonContext context = new StoredModelData(GSON.fromJson(reader, JsonObject.class));

                synchronized (contexts) {
                    contexts.put(id, context);
                }

                return context;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return NullContext.INSTANCE;
    }

    public JsonContext getModelData(ModelKey<?> key) {
        synchronized (contexts) {
            return contexts.get(key.getId());
        }
    }

    class StoredModelData implements JsonContext {

        private final Map<String, JsonComponent<?>> elements;

        private JsonContext parent = NullContext.INSTANCE;

        private final Texture texture;

        StoredModelData(JsonObject json) {
            if (json.has("parent")) {
                parent = loadJsonModel(new Identifier(json.get("parent").getAsString()));
            }
            texture = new JsonTexture(json, parent.getTexture());
            elements = json.entrySet().stream().collect(Collectors.toMap(
                    e -> e.getKey(),
                    e -> loadComponent(e.getValue().getAsJsonObject())
            ));
        }

        @Override
        public <T> void addNamedComponent(String name, JsonComponent<T> component) {
            elements.put(name, component);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> JsonComponent<T> loadComponent(JsonElement json) {
            if (json.isJsonObject()) {
                JsonObject o = json.getAsJsonObject();
                Identifier id = new Identifier(o.get("id").getAsString());
                return (JsonComponent<T>)mson.componentTypes.get(id).loadJson(this, o);
            }

            throw new NotImplementedException("Json was not a js object");
        }

        @Override
        public Texture getTexture() {
            return texture;
        }

        @Override
        public Supplier<JsonContext> resolve(JsonElement json) {

            if (json.isJsonPrimitive()) {
                Identifier id = new Identifier(json.getAsString());

                loadJsonModel(id);
                return () -> contexts.get(id);
            }

            JsonContext ctx = new StoredModelData(json.getAsJsonObject());

            return () -> ctx;
        }

        @Override
        public ModelContext createContext(Model model) {
            return new RootContext(model, parent.createContext(model));
        }

        class RootContext implements ModelContext {

            private final Model model;

            private final Map<String, Object> objectCache = new HashMap<>();

            private final ModelContext inherited;

            RootContext(Model model, ModelContext inherited) {
                this.model = model;
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

            @SuppressWarnings("unchecked")
            @Override
            public <T> T findByName(String name) {
                if (elements.containsKey(name)) {
                    return (T)elements.get(name).export(this);
                }
                return inherited.findByName(name);
            }

            @Override
            public void findByName(String name, Cuboid output) {
                if (elements.containsKey(name)) {
                    elements.get(name).export(this, output);
                } else {
                    inherited.findByName(name, output);
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> T computeIfAbsent(String name, Function<String, T> supplier) {
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
