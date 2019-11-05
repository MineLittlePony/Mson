package com.minelittlepony.mson.impl;

import net.minecraft.client.model.Cuboid;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
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
import java.util.stream.Collectors;

class ModelFoundry {

    private static final Gson GSON = new Gson();

    private final MsonImpl mson;

    private final Map<Identifier, Context> contexts = new HashMap<>();

    public ModelFoundry(MsonImpl mson) {
        this.mson = mson;
    }

    public void loadJsonModel(ResourceManager manager, ModelKey<?> key) {

        Identifier id = key.getId();
        id = new Identifier(id.getNamespace(), "models/" + id.getPath() + ".json");

        try (Resource res = manager.getResource(id)) {
            try (Reader reader = new InputStreamReader(res.getInputStream(), Charsets.UTF_8)) {
                contexts.put(key.getId(), new Context(GSON.fromJson(reader, JsonObject.class)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ModelContext getContext(ModelKey<?> key) {
        return contexts.get(key.getId());
    }

    class Context implements ModelContext, JsonContext {

        private final Map<String, JsonComponent<?>> elements;

        Context(JsonObject json) {
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
        public <T> JsonComponent<T> loadComponent(JsonObject json) {
            Identifier id = new Identifier(json.get("id").getAsString());
            return (JsonComponent<T>)mson.componentTypes.get(id).loadJson(this, json);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T findByName(String name) {
            return (T)elements.get(name).export();
        }

        @Override
        public void findByName(String name, Cuboid output) {
            elements.get(name).export(output);
        }
    }
}
