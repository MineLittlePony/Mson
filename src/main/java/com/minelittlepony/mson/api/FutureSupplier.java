package com.minelittlepony.mson.api;

import com.minelittlepony.mson.api.exception.FutureAwaitException;

import java.util.concurrent.ExecutionException;
import java.util.function.Function;

@FunctionalInterface
public interface FutureSupplier<T> extends Function<String, T> {
    @Override
    default T apply(String key) throws FutureAwaitException {
        try {
            return get(key);
        } catch (InterruptedException | ExecutionException e) {
            throw new FutureAwaitException(e);
        }
    }

    T get(String key) throws InterruptedException, ExecutionException;
}