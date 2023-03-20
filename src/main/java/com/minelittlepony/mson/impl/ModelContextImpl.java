package com.minelittlepony.mson.impl;

import net.minecraft.client.model.ModelPart;

import com.minelittlepony.mson.api.ModelContext;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public interface ModelContextImpl extends ModelContext {
    @Override
    default float getLocalValue(String name, float defaultValue) {
        try {
            return getLocals().getLocal(name, defaultValue).get();
        } catch (InterruptedException | ExecutionException e) {
            return defaultValue;
        }
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

    <T> T findByName(ModelContext context, String name);

    @Override
    default ModelContext bind(Object thisObj, Function<Locals, Locals> inheritedLocals) {
        return bind(thisObj, inheritedLocals.apply(getLocals()));
    }

    ModelContext bind(Object thisObj, Locals locals);
}
