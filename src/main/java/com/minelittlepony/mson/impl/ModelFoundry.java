package com.minelittlepony.mson.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import org.jetbrains.annotations.Nullable;

import com.google.common.io.Files;
import com.minelittlepony.mson.api.FutureSupplier;
import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.exception.FutureAwaitException;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.api.parser.ModelFormat;
import com.minelittlepony.mson.api.parser.ModelLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

class ModelFoundry implements ModelLoader {

    private final AtomicReference<LoadWorker<FileContent<?>>> worker = new AtomicReference<>(LoadWorker.sync());

    private final Map<Identifier, CompletableFuture<FileContent<?>>> loadedFiles = new HashMap<>();

    private final MsonImpl mson;

    private static final CompletableFuture<FileContent<?>> EMPTY_FILE = CompletableFuture.completedFuture(FileContent.empty());

    public ModelFoundry(MsonImpl mson) {
        this.mson = mson;
    }

    @Override
    public ResourceManager getResourceManager() {
        return MinecraftClient.getInstance().getResourceManager();
    }

    CompletableFuture<Void> load() {
        Set<String> extensions = mson.handlersByExtension.keySet();
        getResourceManager().findResources("models/entity", id -> {
            String path = id.getPath();
            return extensions.stream().anyMatch(extension -> path.endsWith("." + extension));
        }).entrySet().stream().map(this::loadModel);

        return CompletableFuture.allOf(loadedFiles.values().stream().toArray(CompletableFuture[]::new));
    }

    ModelFoundry setWorker(LoadWorker<FileContent<?>> worker) {
        this.worker.set(worker);
        return this;
    }

    LoadWorker<FileContent<?>> getWorker() {
        return worker.get();
    }

    public Optional<FileContent<?>> getOrLoadModelData(ModelKey<?> key) throws InterruptedException, ExecutionException, FutureAwaitException {
        return getModelData(key).or((FutureSupplier<Optional<FileContent<?>>>)(() -> {
            return Optional.ofNullable(loadModel(key.getId(), mson.getDefaultFormatHandler()).get());
        })).filter(m -> m != FileContent.empty());
    }

    @SuppressWarnings("unchecked")
    public Optional<FileContent<?>> getModelData(ModelKey<?> key) throws InterruptedException, ExecutionException {
        synchronized (loadedFiles) {
            if (!loadedFiles.containsKey(key.getId())) {
                return Optional.empty();
            }
            return (Optional<FileContent<?>>)(Object)Optional.ofNullable((loadedFiles.get(key.getId()).get()))
                    .filter(m -> m != FileContent.empty());
        }
    }

    public CompletableFuture<FileContent<?>> loadModel(Identifier id, Identifier file, Resource resource) {
        synchronized (loadedFiles) {
            if (!loadedFiles.containsKey(id)) {
                loadedFiles.put(id, getWorker().load(() -> {
                    return mson.getHandlers(Files.getFileExtension(file.getPath()))
                            .flatMap(handler -> handler.loadModel(id, file, resource, true, this).stream())
                            .findFirst()
                            .orElseGet(() -> {
                                MsonImpl.LOGGER.error("Could not load model for {}", file);
                                return FileContent.empty();
                            });
                }, "Loading MSON model - " + id));
            }
            return loadedFiles.get(id);
        }
    }

    public CompletableFuture<FileContent<?>> loadModel(Map.Entry<Identifier, Resource> entry) {
        String extension = Files.getFileExtension(entry.getKey().getPath());
        return loadModel(
                entry.getKey().withPath(p -> p.replace("models/entity/", "").replace("." + extension, "")),
                entry.getKey(),
                entry.getValue()
        );
    }

    @Override
    public CompletableFuture<FileContent<?>> loadModel(Identifier modelId, @Nullable ModelFormat<?> preferredFormat) {
        synchronized (loadedFiles) {
            if (loadedFiles.containsKey(modelId)) {
                return loadedFiles.get(modelId);
            }
        }
        Identifier file = new Identifier(modelId.getNamespace(), "models/entity/" + modelId.getPath());

        Map<Identifier, Resource> resources = getResourceManager().findResources("models/entity", id -> {
            return id.getNamespace().equals(file.getNamespace())
                    && PathUtil.removeExtension(id).contentEquals(PathUtil.removeExtension(file));
        });
        if (resources.size() > 1) {
            MsonImpl.LOGGER.warn("Ambiguous reference: {} could refer to [{}]", file, String.join(",", resources.keySet().stream().map(Identifier::toString).toArray(String[]::new)));
        }
        if (resources.isEmpty()) {
            synchronized (loadedFiles) {
                loadedFiles.put(modelId, EMPTY_FILE);
                return EMPTY_FILE;
            }
        }
        Identifier first = resources.keySet().stream().sorted().toList().get(0);
        if (preferredFormat != null) {
            Identifier preferredFile = file.withPath(p -> p + "." + preferredFormat.getFileExtension());
            if (resources.containsKey(preferredFile)) {
                return loadModel(modelId, preferredFile, resources.get(preferredFile));
            }
        }
        return loadModel(modelId, first, resources.get(first));
    }
}
