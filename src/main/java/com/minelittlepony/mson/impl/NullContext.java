package com.minelittlepony.mson.impl;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

final class NullContext implements ModelContext, ModelContext.Locals, JsonContext, JsonLocalsImpl {

    static NullContext INSTANCE = new NullContext();
    static Identifier ID = new Identifier("mson", "null");

    private NullContext() {}

    @Override
    public <T extends Model> T getModel() {
        throw new EmptyContextException("getModel");
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public <T> T getContext() {
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
    public void getTree(ModelContext context, Map<String, ModelPart> tree) {
    }

    @Override
    public <T> T findByName(ModelContext context, String name) {
        throw new IllegalArgumentException(String.format("Key not found `%s`", name));
    }

    @Override
    public ModelContext getRoot() {
        return this;
    }

    @Override
    public ModelContext resolve(Object child, ModelContext.Locals locals) {
        return this;
    }

    @Override
    public CompletableFuture<Set<String>> getComponentNames() {
        return CompletableFuture.completedFuture(new HashSet<>());
    }

    @Override
    public CompletableFuture<Set<String>> getKeys() {
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
        return this;
    }

    @Override
    public CompletableFuture<Texture> getTexture() {
        return CompletableFuture.completedFuture(JsonTexture.EMPTY);
    }

    @Override
    public CompletableFuture<Incomplete<Float>> getInheritedValue(String name) {
        return CompletableFuture.completedFuture(Incomplete.ZERO);
    }

    @Override
    public Identifier getModelId() {
        return ID;
    }

    @Override
    public CompletableFuture<Float> getValue(String name) {
        return CompletableFuture.completedFuture(0F);
    }

    @Override
    public CompletableFuture<float[]> getDilation() {
        return CompletableFuture.completedFuture(new float[] { 0, 0, 0 });
    }

    @Override
    public float getScale() {
        return 0;
    }

    @Override
    public ModelContext.Locals getLocals() {
        return this;
    }

    @Override
    public JsonContext.Locals getVariables() {
        return this;
    }
}
