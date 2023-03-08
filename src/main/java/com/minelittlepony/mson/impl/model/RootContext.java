package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;

import com.google.common.base.Strings;
import com.minelittlepony.mson.api.FutureSupplier;
import com.minelittlepony.mson.api.ModelContext;
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
    private final Locals locals;

    private final Map<String, ModelComponent<?>> elements;

    public RootContext(Model model, ModelContextImpl inherited, Map<String, ModelComponent<?>> elements, Locals locals) {
        this.model = model;
        this.inherited = inherited;
        this.locals = locals;
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
    public <T> T getContext() {
        return (T)model;
    }

    @Override
    public Locals getLocals() {
        return locals;
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
    @Deprecated
    @Override
    public <T> T findByName(ModelContext context, String name) {
        if (elements.containsKey(name)) {
            try {
                return (T)elements.get(name).export(context);
            } catch (InterruptedException | ExecutionException e) {
                throw new FutureAwaitException(e);
            }
        }
        return inherited.findByName(context, name);
    }

    @Override
    public <T> T findByName(ModelContext context, String name, MsonModel.Factory<T> customType) {
        if (elements.containsKey(name)) {
            try {
                return elements.get(name).exportToType(context, tree -> {
                    T inst = customType.create(tree);
                    if (inst instanceof MsonModel model) {
                        model.init(context);
                    }
                    return inst;
                }).orElseThrow(() -> new ClassCastException("Element " + name + " does not support conversion to the requested type."));
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
    public <T> T computeIfAbsent(String name, FutureSupplier<T> supplier) {
        Objects.requireNonNull(supplier);

        if (Strings.isNullOrEmpty(name)) {
            return supplier.apply(name);
        }

        return (T)Maps.computeIfAbsent(objectCache, name, supplier);
    }

    @Override
    public ModelContext resolve(Object child, Locals locals) {
        if (child == getContext() && locals == getLocals()) {
            return this;
        }
        return new SubContext(this, locals, child);
    }

    @Override
    public ModelContext getRoot() {
        return this;
    }
}