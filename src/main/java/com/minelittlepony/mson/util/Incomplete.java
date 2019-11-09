package com.minelittlepony.mson.util;

import com.minelittlepony.mson.api.ModelContext;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * An incomplete value. Requires a context to fully resolve.
 */
@FunctionalInterface
public interface Incomplete<T> {

    static Incomplete<Float> ZERO = Incomplete.completed(0F);

    /**
     * A complete value. Resolves immediately to the supplied value.
     */
    static <T> Incomplete<T> completed(T value) {
        Objects.requireNonNull(value);
        return locals -> value;
    }

    T complete(ModelContext.Locals locals) throws InterruptedException, ExecutionException;

    default T complete(ModelContext context) throws InterruptedException, ExecutionException {
        return complete(context.getLocals());
    }
}
