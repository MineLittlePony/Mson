package com.minelittlepony.mson.impl;

import net.minecraft.client.model.ModelPart;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.parser.ModelComponent;

import java.util.Map;
import java.util.Optional;

public interface ModelContextImpl extends ModelContext {
    @Override
    default void getTree(Map<String, ModelPart> tree) {
        getTree(this, tree);
    }

    void getTree(ModelContext context, Map<String, ModelPart> tree);

    @Override
    @Deprecated
    default <T> T findByName(String name) {
        return findByName(this, name);
    }

    @Override
    default <T> T findByName(String name, MsonModel.Factory<T> factory) {
        return findByName(this, name, factory);
    }

    @Deprecated
    <T> T findByName(ModelContext context, String name);

    <T> T findByName(ModelContext context, String name, MsonModel.Factory<T> customType);

    @Override
    default Optional<ModelComponent<?>> findComponent(String name) {
        return findComponent(this, name);
    }

    Optional<ModelComponent<?>> findComponent(ModelContext context, String name);
}
