package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Strings;
import com.minelittlepony.mson.api.FutureFunction;
import com.minelittlepony.mson.api.InstanceCreator;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.ModelMetadata;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.exception.FutureAwaitException;
import com.minelittlepony.mson.api.parser.ModelComponent;
import com.minelittlepony.mson.impl.ModelContextImpl;
import com.minelittlepony.mson.util.Maps;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class RootContext implements ModelContextImpl {

    private Model model;

    private final Map<String, Object> objectCache = new HashMap<>();

    private final ModelContextImpl inherited;
    private final ModelMetadataImpl metadata;

    private final Map<String, ModelComponent<?>> elements;

    public RootContext(Model model, ModelContextImpl inherited, Map<String, ModelComponent<?>> elements, Locals locals) {
        this.model = model;
        this.inherited = inherited;
        this.metadata = new ModelMetadataImpl(locals);
        this.elements = elements;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Model> T getModel() {
        return (T)model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getThis() {
        return (T)model;
    }

    @Override
    public Locals getLocals() {
        return metadata.getUnchecked();
    }

    @Override
    public ModelMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void getTree(ModelContext context, Map<String, ModelPart> tree) {
        elements.entrySet().forEach(entry -> {
            if (!tree.containsKey(entry.getKey())) {
                entry.getValue().tryExportTreeNodes(context, ModelPart.class).ifPresent(part -> {
                    tree.put(entry.getKey(), part);
                });
            }
        });
        inherited.getTree(context, tree);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T findByName(ModelContext context, String name, @Nullable InstanceCreator<T> customType) {
        if (elements.containsKey(name)) {

            try {
                if (customType == null) {
                    return (T)elements.get(name).export(context);
                } else {
                    return elements.get(name).exportToType(context, customType)
                            .map(instance -> {
                                if (instance instanceof MsonModel model) {
                                    model.init(context);
                                }
                                return instance;
                            })
                            .orElseThrow(() -> new ClassCastException("Element " + name + " does not support conversion to the requested type."));
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new FutureAwaitException(e);
            }
        }
        return inherited.findByName(context, name, customType);
    }

    @Override
    public Optional<ModelComponent<?>> findComponent(ModelContext context, String name) {
        if (elements.containsKey(name)) {
            return Optional.of(elements.get(name));
        }
        return inherited.findComponent(context, name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T computeIfAbsent(String name, FutureFunction<T> supplier) {
        Objects.requireNonNull(supplier);

        if (Strings.isNullOrEmpty(name)) {
            return supplier.apply(name);
        }

        return (T)Maps.computeIfAbsent(objectCache, name, supplier);
    }

    @Override
    public ModelContext bind(Object thisObj, Locals locals) {
        if (thisObj == getThis() && locals == getLocals()) {
            return this;
        }
        return new SubContext(this, locals, thisObj);
    }

    @Override
    public ModelContext getRoot() {
        return this;
    }
}