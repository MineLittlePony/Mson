package com.minelittlepony.mson.impl;

import net.minecraft.client.model.ModelPart;

import com.minelittlepony.mson.api.ModelContext;

abstract class InnerScope implements ModelContext {

    abstract <T> T findByName(ModelContext context, String name);

    abstract void findByName(ModelContext context, String name, ModelPart output);

    @Override
    public final <T> T findByName(String name) {
        return findByName(this, name);
    }

    @Override
    public final void findByName(String name, ModelPart output) {
        findByName(this, name, output);
    }
}
