package com.minelittlepony.mson.impl;

import net.minecraft.client.model.Cuboid;
import net.minecraft.client.model.Model;

import com.google.gson.JsonElement;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.impl.exception.EmptyContextException;
import com.minelittlepony.mson.impl.model.JsonTexture;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

final class NullContext implements JsonContext, ModelContext {

    static NullContext INSTANCE = new NullContext();

    private NullContext() {}

    @Override
    public Model getModel() {
        throw new EmptyContextException("getModel");
    }

    @Override
    public Object getContext() {
        throw new EmptyContextException("getContext");
    }

    @Override
    public CompletableFuture<JsonContext> resolve(JsonElement json) {
        throw new EmptyContextException("resolve");
    }

    @Override
    public <T> T computeIfAbsent(String name, ContentSupplier<T> supplier) {
        return supplier.apply(name);
    }

    @Override
    public <T> T findByName(String name) {
        throw new IllegalArgumentException(String.format("Key not found `%s`", name));
    }

    @Override
    public void findByName(String name, Cuboid output) {
        throw new IllegalArgumentException(String.format("Key not found `%s`", name));
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
    public <T> Optional<JsonComponent<T>> loadComponent(JsonElement json) {
        return Optional.empty();
    }

    @Override
    public ModelContext createContext(Model model) {
        return this;
    }

    @Override
    public CompletableFuture<Texture> getTexture() {
        return CompletableFuture.completedFuture(JsonTexture.EMPTY);
    }

    @Override
    public float getScale() {
        return 0;
    }
}
