package com.minelittlepony.mson.impl;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.util.Identifier;

import com.minelittlepony.mson.api.FutureSupplier;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.exception.EmptyContextException;
import com.minelittlepony.mson.api.model.Texture;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

final class EmptyModelContext implements ModelContext, ModelContext.Locals {

    static ModelContext INSTANCE = new EmptyModelContext();
    static Identifier ID = new Identifier("mson", "null");

    private EmptyModelContext() {}

    @Override
    public <T extends Model> T getModel() {
        throw new EmptyContextException("getModel");
    }

    @Override
    public <T> T getContext() {
        throw new EmptyContextException("getContext");
    }

    @Override
    public <T> T computeIfAbsent(String name, FutureSupplier<T> supplier) {
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
    public CompletableFuture<Float> getLocal(String name) {
        return CompletableFuture.completedFuture(0F);
    }

    @Override
    public CompletableFuture<float[]> getDilation() {
        return CompletableFuture.completedFuture(new float[] { 0, 0, 0 });
    }

    @Override
    public ModelContext.Locals getLocals() {
        return this;
    }
}
