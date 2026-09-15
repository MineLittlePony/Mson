package com.minelittlepony.mson.impl;

import net.minecraft.client.model.geom.ModelPart;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.SlotKey;

import java.util.Map;
import java.util.Optional;
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
        return findByName(this, name);
    }

    @Override
    default <T> Optional<T> findByName(String name, SlotKey<T> type) {
        return findByName(this, name, type);
    }

    default <T> T findByName(ModelContext context, String name) {
        return this.<T>findByName(context, name, null).orElseThrow(() -> new IllegalArgumentException(String.format("Key not found `%s`", name)));
    }

    <T> Optional<T> findByName(ModelContext context, String name, @Nullable SlotKey<T> type);

    @Override
    default ModelContext bind(Object thisObj, Function<Locals, Locals> inheritedLocals) {
        return bind(thisObj, inheritedLocals.apply(getLocals()));
    }

    ModelContext bind(Object thisObj, Locals locals);
}
