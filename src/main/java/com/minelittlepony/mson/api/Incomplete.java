package com.minelittlepony.mson.api;

import com.minelittlepony.mson.api.ModelView.Locals;
import com.minelittlepony.mson.api.exception.FutureAwaitException;

import java.util.Objects;

/**
 * An incomplete value.
 *
 * Represents a value that cannot be fully resolved
 * without a completed {@link ModelContext}.
 */
@FunctionalInterface
public interface Incomplete<T> {
    /**
     * A completed Incomplete that always returns 0.
     */
    static Incomplete<Float> ZERO = Incomplete.completed(0F);

    /**
     * Returns a completed Incomplete that resolves to a fixed constant value.
     */
    static <T> Incomplete<T> completed(T value) {
        return new Constant<>(value);
    }

    /**
     * Resolves this incomplete's final value against the passed in {@link ModelContext.Locals}.
     * May defer to the inheritance hierarchy where necessary.
     */
    T complete(ModelContext.Locals locals) throws FutureAwaitException;

    /**
     * Resolves this incomplete's final value against the passed in {@link ModelContext}.
     * May defer to the inheritance hierarchy where necessary.
     */
    default T complete(ModelContext context) throws FutureAwaitException {
        return complete(context.getLocals());
    }

    record Constant<T>(T value) implements Incomplete<T> {
        public Constant {
            Objects.requireNonNull(value);
        }
        @Override
        public T complete(Locals locals) throws FutureAwaitException {
            return value;
        }
    }

    record FloatReference(String name, float def) implements Incomplete<Float> {
        @Override
        public Float complete(Locals locals) throws FutureAwaitException {
            return locals.getLocal(name, def);
        }
    }
}
