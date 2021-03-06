package com.minelittlepony.mson.impl;

import net.minecraft.resource.Resource;
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
import java.io.Reader;
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
                    Identifier file = new Identifier(id.getNamespace(), "models/" + id.getPath() + ".json");

                    if (!manager.containsResource(file)) {
                        if (failHard) {
                            MsonImpl.LOGGER.error("Could not load model json for {}", file);
                        }
                        return EmptyJsonContext.INSTANCE;
                    }
                    try (Resource res = manager.getResource(file);
                         Reader reader = new InputStreamReader(res.getInputStream(), Charsets.UTF_8)) {
                        return new StoredModelData(this, id, GSON.fromJson(reader, JsonObject.class));
                    } catch (Exception e) {
                        MsonImpl.LOGGER.error("Could not load model json for {}", file, ThrowableUtils.getRootCause(e));
                    } finally {
                        clientProfiler.pop();
                        serverProfiler.endTick();
                    }

                    return EmptyJsonContext.INSTANCE;
                }, executor));
            }
            return loadedFiles.get(id);
        }
    }
}
