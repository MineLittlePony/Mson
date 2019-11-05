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

    private final Map<Identifier, StoredModelData> contexts = new HashMap<>();

    public ModelFoundry(MsonImpl mson) {
        this.mson = mson;
    }

    public void loadJsonModel(ResourceManager manager, ModelKey<?> key) {

        Identifier id = key.getId();
        id = new Identifier(id.getNamespace(), "models/" + id.getPath() + ".json");

        try (Resource res = manager.getResource(id)) {
            try (Reader reader = new InputStreamReader(res.getInputStream(), Charsets.UTF_8)) {
                contexts.put(key.getId(), new StoredModelData(GSON.fromJson(reader, JsonObject.class)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StoredModelData getModelData(ModelKey<?> key) {
        return contexts.get(key.getId());
    }

    class StoredModelData implements JsonContext {

        private final Map<String, JsonComponent<?>> elements;

        StoredModelData(JsonObject json) {
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
            //return null; // TODO
        }

        public ModelContext createContext(Model model) {
            return new RootContext(model);
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

            RootContext(Model model) {
                this.model = model;
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
                return (T)elements.get(name).export(this);
            }

            @Override
            public void findByName(String name, Cuboid output) {
                elements.get(name).export(this, output);
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
