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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class ModelFoundry {

    private static final Gson GSON = new Gson();

    private final MsonImpl mson;

    private final Map<Identifier, JsonContext> contexts = new HashMap<>();

    public ModelFoundry(MsonImpl mson) {
        this.mson = mson;
    }

    public JsonContext loadJsonModel(ResourceManager manager, Identifier id) {
        synchronized (contexts) {
            if (contexts.containsKey(id)) {
                return contexts.get(id);
            }
        }

        Identifier file = new Identifier(id.getNamespace(), "models/" + id.getPath() + ".json");

        try (Resource res = manager.getResource(file)) {
            try (Reader reader = new InputStreamReader(res.getInputStream(), Charsets.UTF_8)) {
                JsonContext context = new StoredModelData(manager, GSON.fromJson(reader, JsonObject.class));

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
        return contexts.get(key.getId());
    }

    class StoredModelData implements JsonContext {

        private final Map<String, JsonComponent<?>> elements;

        private JsonContext parent = NullContext.INSTANCE;

        StoredModelData(ResourceManager manager, JsonObject json) {
            elements = json.entrySet().stream().collect(Collectors.toMap(
                    e -> e.getKey(),
                    e -> loadComponent(e.getValue().getAsJsonObject())
            ));

            if (json.has("parent")) {
                parent = loadJsonModel(manager, new Identifier(json.get("parent").getAsString()));
            }
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
            //return null; // TODO
        }

        @Override
        public ModelContext createContext(Model model) {
            return new RootContext(model, parent.createContext(model));
        }

        class LeafContext implements ModelContext {

            private final ModelContext parent;

            private final Object context;

            LeafContext(ModelContext parent, Object context) {
                this.parent = parent;
                this.context = context;
            }

            @Override
            public Model getModel() {
                return parent.getModel();
            }

            @Override
            public Object getContext() {
                return context;
            }

            @Override
            public <T> T findByName(String name) {
                return parent.findByName(name);
            }

            @Override
            public void findByName(String name, Cuboid output) {
                parent.findByName(name, output);
            }

            @Override
            public <T> T computeIfAbsent(String name, Function<String, T> supplier) {
                return parent.computeIfAbsent(name, supplier);
            }

            @Override
            public ModelContext resolve(Object child) {
                return new LeafContext(this, child);
            }

            @Override
            public ModelContext getRoot() {
                return parent.getRoot();
            }
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
                return new LeafContext(this, child);
            }

            @Override
            public ModelContext getRoot() {
                return this;
            }
        }
    }
}
