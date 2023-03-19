package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;

import com.minelittlepony.mson.api.FutureFunction;
import com.minelittlepony.mson.api.InstanceCreator;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.ModelMetadata;
import com.minelittlepony.mson.api.ModelView;
import com.minelittlepony.mson.api.parser.ModelComponent;
import com.minelittlepony.mson.impl.ModelContextImpl;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

class SubContext implements ModelContextImpl {

    private final ModelContextImpl parent;

    private final ModelMetadataImpl metadata;

    private final Object context;

    public SubContext(ModelContextImpl parent, Locals locals, Object context) {
        this.parent = Objects.requireNonNull(parent, "Parent context is required");
        this.metadata = new ModelMetadataImpl(Objects.requireNonNull(locals, "Locals is required"));
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
    public <T> T findByName(ModelContext context, String name, @Nullable InstanceCreator<T> factory) {
        return parent.findByName(context, name, factory);
    }

    @Override
    public Optional<ModelComponent<?>> findComponent(ModelContext context, String name) {
        return parent.findComponent(context, name);
    }

    @Override
    public <T> T computeIfAbsent(String name, FutureFunction<T> supplier) {
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
    public ModelView getRoot() {
        return parent.getRoot();
    }

    @Override
    public Locals getLocals() {
        return metadata.getUnchecked();
    }

    @Override
    public ModelMetadata getMetadata() {
        return metadata;
    }
}
