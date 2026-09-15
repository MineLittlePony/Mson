package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.mson.api.FutureFunction;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.SlotKey;
import com.minelittlepony.mson.api.exception.EmptyContextException;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.api.parser.ModelComponent;
import com.minelittlepony.mson.impl.ModelContextImpl;
import com.minelittlepony.mson.impl.MsonImpl;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class EmptyModelContext implements ModelContextImpl, ModelContext.Locals {

    static ModelContextImpl INSTANCE = new EmptyModelContext();
    static Identifier ID = MsonImpl.id("null");

    private EmptyModelContext() {}

    @Override
    public <T extends Model<?>> T getModel() {
        throw new EmptyContextException("getModel");
    }

    @Override
    public <T> T getThis() {
        throw new EmptyContextException("getThis");
    }

    @Override
    public ModelComponent<?> getComponent(String name) {
        return null;
    }

    @Override
    public <T> T computeIfAbsent(String name, FutureFunction<T> supplier) {
        return supplier.apply(name);
    }

    @Override
    public void getTree(ModelContext context, Map<String, ModelPart> tree) {
    }

    @Override
    public <T> Optional<T> findByName(ModelContext context, String name, @Nullable SlotKey<T> type) {
        return Optional.empty();
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
    public Identifier modelId() {
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
