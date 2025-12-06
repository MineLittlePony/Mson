package com.minelittlepony.mson.impl;


import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@FunctionalInterface
public interface LoadWorker<T> {

    CompletableFuture<T> load(Supplier<T> loadFunc, String loadMessage);

    public static <T> LoadWorker<T> async(Executor executor) {
        return (loadFunc, loadMessage) -> {
            return CompletableFuture.supplyAsync(() -> {
                ProfilerFiller profiler = Profiler.get();
                profiler.startTick();
                profiler.push(loadMessage);
                try {
                    return loadFunc.get();
                } finally {
                    profiler.pop();
                    profiler.endTick();
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
