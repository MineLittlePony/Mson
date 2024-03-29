package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.util.Identifier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.mson.api.FutureFunction;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.exception.EmptyContextException;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.impl.ModelContextImpl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

final class EmptyModelContext implements ModelContextImpl, ModelContext.Locals {

    static ModelContextImpl INSTANCE = new EmptyModelContext();
    static Identifier ID = new Identifier("mson", "null");

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
    public <T> T findByName(ModelContext context, String name, @Nullable Function<ModelPart, T> function, @Nullable Class<T> rootType) {
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
    public Set<String> keys() {
        return new HashSet<>();
    }

    @Override
    public Texture getTexture() {
        return Texture.EMPTY;
    }

    @Override
    public Identifier getModelId() {
        return ID;
    }

    @Override
    public float getLocal(String name, float defaultValue) {
        return defaultValue;
    }

    @Override
    public float[] getDilation() {
        return new float[] { 0, 0, 0 };
    }

    @Override
    public Locals getLocals() {
        return this;
    }
}
