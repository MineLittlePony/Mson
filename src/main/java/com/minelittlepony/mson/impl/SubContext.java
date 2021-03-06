package com.minelittlepony.mson.impl;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;

import com.minelittlepony.mson.api.FutureSupplier;
import com.minelittlepony.mson.api.ModelContext;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

class SubContext implements ModelContext {

    private final ModelContext parent;

    private final Locals locals;

    private final Object context;

    SubContext(ModelContext parent, Locals locals, Object context) {
        this.parent = Objects.requireNonNull(parent, "Parent context is required");
        this.locals = Objects.requireNonNull(locals, "Locals is required");
        this.context = Objects.requireNonNull(context, "Sub-context element is required");
    }

    @Nullable
    @Override
    public <T extends Model> T getModel() {
        return parent.getModel();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getContext() {
        return (T)context;
    }

    @Override
    public void getTree(ModelContext context, Map<String, ModelPart> tree) {
        parent.getTree(context, tree);
    }

    @Override
    public <T> T findByName(ModelContext context, String name) {
        return parent.findByName(context, name);
    }

    @Override
    public <T> T computeIfAbsent(String name, FutureSupplier<T> supplier) {
        return parent.computeIfAbsent(name, supplier);
    }

    @Override
    public ModelContext resolve(Object child, Locals locals) {
        if (child == getContext() && locals == getLocals()) {
            return this;
        }
        return new SubContext(parent, locals, child);
    }

    @Override
    public ModelContext getRoot() {
        return parent.getRoot();
    }

    @Override
    public Locals getLocals() {
        return locals;
    }
}
