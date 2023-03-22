package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;

import com.minelittlepony.mson.api.FutureFunction;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.ModelView;
import com.minelittlepony.mson.impl.ModelContextImpl;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

class SubContext implements ModelContextImpl {

    private final ModelContextImpl parent;

    private final Locals locals;

    private final Object context;

    public SubContext(ModelContextImpl parent, Locals locals, Object context) {
        this.parent = Objects.requireNonNull(parent, "Parent context is required");
        this.locals = Objects.requireNonNull(locals, "Locals is required");
        this.context = Objects.requireNonNull(context, "Sub-context element is required");
    }

    @Override
    public ModelView getRoot() {
        return parent.getRoot();
    }

    @Nullable
    @Override
    public <T extends Model> T getModel() {
        return parent.getModel();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getThis() {
        return (T)context;
    }

    @Override
    public Locals getLocals() {
        return locals;
    }

    @Override
    public void getTree(ModelContext context, Map<String, ModelPart> tree) {
        parent.getTree(context, tree);
    }

    @Override
    public <T> T findByName(ModelContext context, String name, @Nullable Function<ModelPart, T> function, @Nullable Class<T> rootType) {
        return parent.findByName(context, name, function, rootType);
    }

    @Override
    public <T> T computeIfAbsent(String name, FutureFunction<T> supplier) {
        return parent.computeIfAbsent(name, supplier);
    }

    @Override
    public ModelContext bind(Object child, Locals locals) {
        if (child == getThis() && locals == getLocals()) {
            return this;
        }
        return new SubContext(parent, locals, child);
    }
}
