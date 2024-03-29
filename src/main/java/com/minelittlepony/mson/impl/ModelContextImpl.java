package com.minelittlepony.mson.impl;

import net.minecraft.client.model.ModelPart;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.mson.api.ModelContext;

import java.util.Map;
import java.util.function.Function;

public interface ModelContextImpl extends ModelContext {
    @Override
    default float getLocalValue(String name, float defaultValue) {
        return getLocals().getLocal(name, defaultValue);
    }

    @Override
    default void getTree(Map<String, ModelPart> tree) {
        getTree(this, tree);
    }

    void getTree(ModelContext context, Map<String, ModelPart> tree);

    @Override
    default <T> T findByName(String name) {
        return findByName(this, name, null, null);
    }

    @Override
    default <T> T findByName(String name, @Nullable Function<ModelPart, T> function) {
        return findByName(this, name, function, null);
    }

    @Override
    default <T> T findByName(String name, @Nullable Function<ModelPart, T> function, @Nullable Class<T> rootType) {
        return findByName(this, name, function, rootType);
    }

    <T> T findByName(ModelContext context, String name, @Nullable Function<ModelPart, T> function, @Nullable Class<T> rootType);

    @Override
    default ModelContext bind(Object thisObj, Function<Locals, Locals> inheritedLocals) {
        return bind(thisObj, inheritedLocals.apply(getLocals()));
    }

    ModelContext bind(Object thisObj, Locals locals);
}
