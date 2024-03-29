package com.minelittlepony.mson.api;

import com.minelittlepony.mson.api.exception.FutureAwaitException;

import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

@FunctionalInterface
public interface FutureSupplier<T> extends Supplier<T> {
    @Override
    default T get() throws FutureAwaitException {
        try {
            return supply();
        } catch (InterruptedException | ExecutionException e) {
            throw new FutureAwaitException(e);
        }
    }

    T supply() throws InterruptedException, ExecutionException;
}