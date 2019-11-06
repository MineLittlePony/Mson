package com.minelittlepony.mson.impl;

import net.minecraft.client.model.Cuboid;
import net.minecraft.client.model.Model;

import com.minelittlepony.mson.api.ModelContext;

public class SubContext implements ModelContext {

    private final ModelContext parent;

    private final Object context;

    SubContext(ModelContext parent, Object context) {
        this.parent = parent;
        this.context = context;
    }

    @Override
    public Model getModel() {
        return parent.getModel();
    }

    @Override
    public Object getContext() {
        return context;
    }

    @Override
    public <T> T findByName(String name) {
        return parent.findByName(name);
    }

    @Override
    public void findByName(String name, Cuboid output) {
        parent.findByName(name, output);
    }

    @Override
    public <T> T computeIfAbsent(String name, ContentSupplier<T> supplier) {
        return parent.computeIfAbsent(name, supplier);
    }

    @Override
    public ModelContext resolve(Object child) {
        return new SubContext(this, child);
    }

    @Override
    public ModelContext getRoot() {
        return parent.getRoot();
    }

}
