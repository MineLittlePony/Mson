package com.minelittlepony.mson.impl;

import net.minecraft.client.model.Model;
import net.minecraft.util.Identifier;

import com.google.gson.JsonElement;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.exception.EmptyContextException;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.impl.model.JsonTexture;
import com.minelittlepony.mson.util.Incomplete;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

final class EmptyJsonContext implements JsonContext, JsonLocalsImpl {

    static JsonContext INSTANCE = new EmptyJsonContext();

    private EmptyJsonContext() {}

    @Override
    public Identifier getModelId() {
        return EmptyModelContext.ID;
    }

    @Override
    public CompletableFuture<JsonContext> resolve(JsonElement json) {
        throw new EmptyContextException("resolve");
    }

    @Override
    public CompletableFuture<Set<String>> getComponentNames() {
        return CompletableFuture.completedFuture(new HashSet<>());
    }

    @Override
    public CompletableFuture<Set<String>> keys() {
        return CompletableFuture.completedFuture(new HashSet<>());
    }

    @Override
    public <T> void addNamedComponent(String name, JsonComponent<T> component) {
    }

    @Override
    public <T> Optional<JsonComponent<T>> loadComponent(JsonElement json, Identifier defaultAs) {
        return Optional.empty();
    }

    @Override
    public ModelContext createContext(Model model, ModelContext.Locals locals) {
        return EmptyModelContext.INSTANCE;
    }

    @Override
    public CompletableFuture<Texture> getTexture() {
        return CompletableFuture.completedFuture(JsonTexture.EMPTY);
    }

    @Override
    public CompletableFuture<Incomplete<Float>> getLocal(String name) {
        return CompletableFuture.completedFuture(Incomplete.ZERO);
    }

    @Override
    public CompletableFuture<float[]> getDilation() {
        return CompletableFuture.completedFuture(new float[] { 0, 0, 0 });
    }

    @Override
    public JsonContext.Locals getLocals() {
        return this;
    }
}
