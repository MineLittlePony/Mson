package com.minelittlepony.mson.impl;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.util.ThrowableUtils;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

class ModelFoundry {
    private static final Gson GSON = new Gson();

    private final ResourceManager manager;

    private final Executor executor;

    private final Profiler serverProfiler;
    private final Profiler clientProfiler;

    private final Map<Identifier, CompletableFuture<JsonContext>> loadedFiles = new HashMap<>();

    public ModelFoundry(ResourceManager manager, Executor executor, Profiler serverProfiler, Profiler clientProfiler) {
        this.manager = manager;
        this.executor = executor;
        this.serverProfiler = serverProfiler;
        this.clientProfiler = clientProfiler;
    }

    public Optional<JsonContext> getModelData(ModelKey<?> key) throws InterruptedException, ExecutionException {
        if (!loadedFiles.containsKey(key.getId())) {
            return Optional.empty();
        }
        return Optional.ofNullable(loadedFiles.get(key.getId()).get()).filter(m -> m != EmptyJsonContext.INSTANCE);
    }

    public CompletableFuture<JsonContext> loadJsonModel(Identifier id) {
        return loadJsonModel(id, true);
    }

    public CompletableFuture<JsonContext> loadJsonModel(Identifier id, boolean failHard) {
        synchronized (loadedFiles) {
            if (!loadedFiles.containsKey(id)) {
                loadedFiles.put(id, CompletableFuture.supplyAsync(() -> {
                    serverProfiler.startTick();
                    clientProfiler.push("Loading MSON model - " + id);
                    try {
                        Identifier file = new Identifier(id.getNamespace(), "models/" + id.getPath() + ".json");

                        return manager.getResource(file).map(resource -> {
                            try (var reader = new InputStreamReader(resource.getInputStream(), Charsets.UTF_8)) {
                                return (JsonContext)new StoredModelData(this, id, GSON.fromJson(reader, JsonObject.class));
                            } catch (Exception e) {
                                MsonImpl.LOGGER.error("Could not load model json for {}", file, ThrowableUtils.getRootCause(e));
                            }
                            return null;
                        }).orElseGet(() -> {
                            if (failHard) {
                                MsonImpl.LOGGER.error("Could not load model json for {}", file);
                            }
                            return EmptyJsonContext.INSTANCE;
                        });
                    } finally {
                        clientProfiler.pop();
                        serverProfiler.endTick();
                    }
                }, executor));
            }
            return loadedFiles.get(id);
        }
    }
}
