package com.minelittlepony.mson.impl.key;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

import com.google.common.base.Preconditions;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.exception.FutureAwaitException;
import com.minelittlepony.mson.api.model.traversal.SkeletonisedModel;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.impl.model.RootContext;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractModelKeyImpl<T> implements ModelKey<T> {
    protected Identifier id;

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ModelKey && ((ModelKey<?>)other).getId().equals(getId());
    }

    public interface ModelDataSupplier {
        public Optional<FileContent<?>> getOrLoadModelData(ModelKey<?> key) throws InterruptedException, ExecutionException, FutureAwaitException;
    }

    public interface Holder {
        void setKey(ModelKey<?> key);
    }

    public static class Value<T extends Model<?>> extends Reference<T> {
        public Value(Identifier id, AtomicReference<? extends ModelDataSupplier> foundry) {
            super(id, foundry, null);
        }

        @Override
        public <V extends T> V createModel() {
            throw new IllegalStateException("Cannot create a model for a key (" + getId() + ") with unknown type. For built-in models please use createModel(factory)");
        }
    }

    public static class Reference<T extends Model<?>> extends AbstractModelKeyImpl<T> {
        private final AtomicReference<? extends ModelDataSupplier> foundry;
        private final MsonModel.Factory<T> constr;

        public Reference(Identifier id, AtomicReference<? extends ModelDataSupplier> foundry, MsonModel.Factory<T> constr) {
            this.id = id;
            this.foundry = foundry;
            this.constr = constr;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <V extends T> V createModel() {
            return (V)createModel(constr);
        }

        @Override
        public Optional<ModelPart> createTree() {
            return getModelData().map(context -> {
                return context.createContext(null, null, context.locals().bake()).toTree();
            });
        }

        @Override
        public <V extends T> V createModel(MsonModel.Factory<V> factory) {
            Preconditions.checkNotNull(factory, "Factory should not be null");

            return getModelData().map(context -> {
                ModelContext ctx = context.createContext(null, null, context.locals().bake());

                ModelPart root = ctx.toTree();
                V t = factory.create(root);

                if (t instanceof SkeletonisedModel sk) {
                    sk.setSkeleton(context.skeleton().map(root::ordered).orElse(root));
                }
                if (t instanceof MsonModel mm) {
                    if (ctx instanceof RootContext c) {
                        c.setModel(t);
                    }
                    mm.init(ctx);
                }
                return t;
            })
            .orElseThrow(() -> new IllegalStateException("Model file for " + getId() + " was not loaded!"));
        }

        @Override
        public Optional<FileContent<?>> getModelData() {
            try {
                return foundry.get().getOrLoadModelData(this);
            } catch (InterruptedException | ExecutionException | FutureAwaitException e) {
                throw new RuntimeException("Could not create model", e);
            }
        }
    }
}
