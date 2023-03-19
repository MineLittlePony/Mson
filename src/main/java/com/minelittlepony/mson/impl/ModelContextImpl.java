package com.minelittlepony.mson.impl;

import net.minecraft.client.model.ModelPart;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.mson.api.InstanceCreator;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.parser.ModelComponent;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

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
    default <T> T findByName(String name, @Nullable MsonModel.Factory<T> factory, @Nullable Class<T> type) {
        return findByName(this, name, factory == null ? null : InstanceCreator.ofFunction(type, factory));
    }

    <T> T findByName(ModelContext context, String name, @Nullable InstanceCreator<T> customType);

    @Override
    default Optional<ModelComponent<?>> findComponent(String name) {
        return findComponent(this, name);
    }

    Optional<ModelComponent<?>> findComponent(ModelContext context, String name);
}
