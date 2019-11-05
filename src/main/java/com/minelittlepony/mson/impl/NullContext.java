package com.minelittlepony.mson.impl;

import net.minecraft.client.model.Cuboid;
import net.minecraft.client.model.Model;

import org.apache.commons.lang3.NotImplementedException;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;

import java.util.function.Function;

final class NullContext implements JsonContext, ModelContext {

    static NullContext INSTANCE = new NullContext();

    private NullContext() {}

    @Override
    public Model getModel() {
        throw new NotImplementedException("getModel");
    }

    @Override
    public Object getContext() {
        throw new NotImplementedException("getContext");
    }

    @Override
    public <T> T computeIfAbsent(String name, Function<String, T> supplier) {
        return supplier.apply(name);
    }

    @Override
    public <T> T findByName(String name) {
        throw new RuntimeException(String.format("Key not found `%s`", name));
    }

    @Override
    public void findByName(String name, Cuboid output) {
        throw new RuntimeException(String.format("Key not found `%s`", name));
    }

    @Override
    public ModelContext getRoot() {
        return this;
    }

    @Override
    public ModelContext resolve(Object child) {
        return this;
    }

    @Override
    public <T> void addNamedComponent(String name, JsonComponent<T> component) {
    }

    @Override
    public <T> JsonComponent<T> loadComponent(JsonElement json) {
        throw new JsonParseException("Null Context");
    }

    @Override
    public ModelContext createContext(Model model) {
        return this;
    }
}
