package com.minelittlepony.mson.impl;

import net.minecraft.client.model.ModelPart;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.MsonModel;

import java.util.Objects;

class SubContext implements ModelContext {

    private final ModelContext parent;

    private final Object context;

    SubContext(ModelContext parent, Object context) {
        this.parent = Objects.requireNonNull(parent, "Parent context is required");
        this.context = Objects.requireNonNull(context, "Sub-context element is required");
    }

    @Override
    public <T extends MsonModel> T getModel() {
        return parent.getModel();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getContext() {
        return (T)context;
    }

    @Override
    public <T> T findByName(String name) {
        return parent.findByName(name);
    }

    @Override
    public void findByName(String name, ModelPart output) {
        parent.findByName(name, output);
    }

    @Override
    public <T> T computeIfAbsent(String name, ContentSupplier<T> supplier) {
        return parent.computeIfAbsent(name, supplier);
    }

    @Override
    public ModelContext resolve(Object child) {
        if (child == getContext()) {
            return this;
        }
        return new SubContext(this, child);
    }

    @Override
    public ModelContext getRoot() {
        return parent.getRoot();
    }

    @Override
    public float getScale() {
        return parent.getScale();
    }

    @Override
    public Locals getLocals() {
        return parent.getLocals();
    }
}
