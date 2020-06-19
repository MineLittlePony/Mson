package com.minelittlepony.mson.util.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

import javax.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
abstract class RenderListImpl<T> extends ImmutableFastList<T> implements RenderList<T> {
    private final RenderConsumer<T> consumer;

    RenderListImpl(RenderConsumer<T> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        final Object[] values = values();
        for (int i = 0; i < values.length; i++) {
            consumer.render((T)values[i], matrices, vertices, light, overlay, red, green, blue, alpha);
        }
    }

    static <T> Object[] toArray(Iterable<T> list) {
        if (list instanceof RenderListImpl) {
            return ((RenderListImpl<T>)list).values();
        }
        @SuppressWarnings("serial")
        final List<T> items = new LinkedList<T>() {
            @Override
            public boolean add(T t) {
                return super.add(Objects.requireNonNull(t));
            }
        };
        list.forEach(items::add);
        return items.toArray();
    }

    static final class Flat<T> extends RenderListImpl<T> {
        private final Object[] values;

        Flat(Iterable<T> values, RenderConsumer<T> consumer) {
            super(consumer);
            this.values = toArray(values);
        }

        @Override
        protected Object[] values() {
            return values;
        }

    }

    static final class Lazy<T> extends RenderListImpl<T> {
        private final Supplier<Iterable<T>> factory;
        @Nullable
        private Object[] values;

        Lazy(Supplier<Iterable<T>> factory, RenderConsumer<T> consumer) {
            super(consumer);
            this.factory = factory;
        }

        @Override
        protected Object[] values() {
            if (values == null) {
                values = toArray(factory.get());
            }
            return values;
        }
    }
}
