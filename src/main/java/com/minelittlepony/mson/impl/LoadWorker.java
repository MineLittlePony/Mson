package com.minelittlepony.mson.impl;

import net.minecraft.util.profiler.Profiler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@FunctionalInterface
public interface LoadWorker<T> {

    CompletableFuture<T> load(Supplier<T> loadFunc, String loadMessage);

    public static <T> LoadWorker<T> async(Executor executor, Profiler serverProfiler, Profiler clientProfiler) {
        return (loadFunc, loadMessage) -> {
            return CompletableFuture.supplyAsync(() -> {
                serverProfiler.startTick();
                clientProfiler.push(loadMessage);
                try {
                    return loadFunc.get();
                } finally {
                    clientProfiler.pop();
                    serverProfiler.endTick();
                }
            }, executor);
        };
    }

    public static <T> LoadWorker<T> sync() {
        return (loadFunc, loadMessage) -> {
            return CompletableFuture.completedFuture(loadFunc.get());
        };
    }
}
