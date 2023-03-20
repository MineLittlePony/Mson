package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.util.Identifier;

import com.minelittlepony.mson.api.FutureFunction;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.ModelMetadata;
import com.minelittlepony.mson.api.exception.EmptyContextException;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.impl.ModelContextImpl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

final class EmptyModelContext implements ModelContextImpl, ModelContext.Locals {

    static ModelContextImpl INSTANCE = new EmptyModelContext();
    static Identifier ID = new Identifier("mson", "null");


    private final ModelMetadataImpl metadata = new ModelMetadataImpl(this);

    private EmptyModelContext() {}

    @Override
    public <T extends Model> T getModel() {
        throw new EmptyContextException("getModel");
    }

    @Override
    public <T> T getThis() {
        throw new EmptyContextException("getThis");
    }

    @Override
    public <T> T computeIfAbsent(String name, FutureFunction<T> supplier) {
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
    public ModelContext bind(Object thisObj, ModelContext.Locals locals) {
        return this;
    }

    @Override
    public CompletableFuture<Set<String>> keys() {
        return CompletableFuture.completedFuture(new HashSet<>());
    }

    @Override
    public CompletableFuture<Texture> getTexture() {
        return CompletableFuture.completedFuture(Texture.EMPTY);
    }

    @Override
    public Identifier getModelId() {
        return ID;
    }

    @Override
    public CompletableFuture<Float> getLocal(String name, float defaultValue) {
        return CompletableFuture.completedFuture(defaultValue);
    }

    @Override
    public CompletableFuture<float[]> getDilation() {
        return CompletableFuture.completedFuture(new float[] { 0, 0, 0 });
    }

    @Override
    public ModelContext.Locals getLocals() {
        return this;
    }

    @Override
    public ModelMetadata getMetadata() {
        return metadata;
    }
}
