package com.minelittlepony.mson.impl;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.Model;
import com.minelittlepony.mson.api.Mson;
import com.minelittlepony.mson.api.event.MsonModelsReadyCallback;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.impl.components.JsonCuboid;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MsonImpl implements Mson, IdentifiableResourceReloadListener {

    private static final Identifier ID = new Identifier("mson", "models");

    public static final Mson INSTANCE = new MsonImpl();

    private final Map<Identifier, Key<?>> registeredModels = new HashMap<>();

    final Map<Identifier, JsonContext.Constructor<?>> componentTypes = new HashMap<>();

    private ModelFoundry foundry = new ModelFoundry(this);

    private MsonImpl() {
        componentTypes.put(JsonCuboid.ID, JsonCuboid::new);
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer sync, ResourceManager sender,
            Profiler serverProfiler, Profiler clientProfiler,
            Executor serverExecutor, Executor clientExecutor) {

        foundry = new ModelFoundry(this);
        CompletableFuture<?>[] tasks = registeredModels.values().stream().map(key -> {
            return CompletableFuture.runAsync(() -> {
                serverProfiler.startTick();
                clientProfiler.push("Loading MASON models - " + key.getId());
                foundry.loadJsonModel(sender, key);
                clientProfiler.pop();
                serverProfiler.endTick();
            }, serverExecutor);
        }).toArray(i -> new CompletableFuture[i]);

        CompletableFuture<?> all = CompletableFuture.allOf(tasks);

        sync.getClass();
        return all.thenCompose(sync::whenPrepared).thenRunAsync(() -> {
            MsonModelsReadyCallback.EVENT.invoker().init();
        }, clientExecutor);
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Model> ModelKey<T> registerModel(Identifier id, Class<T> implementation) {
        Objects.requireNonNull(id, "Id must not be null");
        Objects.requireNonNull(implementation, "Implementation class must not be null");

        if (registeredModels.containsKey(id) && registeredModels.get(id).clazz.equals(implementation)) {
            throw new IllegalArgumentException(String.format("A model with the id `%s` was already registered", id.toString()));
        }
        if ("minecraft".equalsIgnoreCase(id.getNamespace())) {
            throw new IllegalArgumentException("Id must have a namespace other than `minecraft`.");
        }

        return (ModelKey<T>)registeredModels.computeIfAbsent(id, i -> new Key<>(id, implementation));
    }

    @Override
    public void registerComponentType(Identifier id, JsonContext.Constructor<?> loadHandler) {
        Objects.requireNonNull(id, "Id must not be null");
        if ("minecraft".equalsIgnoreCase(id.getNamespace())) {
            throw new IllegalArgumentException("Id must have a namespace other than `minecraft`.");
        }
        componentTypes.put(id, loadHandler);
    }

    class Key<T extends Model> implements ModelKey<T> {

        private final Class<T> clazz;
        private final Identifier id;

        public Key(Identifier id, Class<T> clazz) {
            this.id = id;
            this.clazz = clazz;
        }

        @Override
        public Identifier getId() {
            return id;
        }

        @Override
        public T createModel() {
            try {
                T t = clazz.newInstance();
                t.init(foundry.getContext(this));
                return t;
            } catch (InstantiationException | IllegalAccessException e) {
                return null;
            }
        }

    }
}
