package com.minelittlepony.mson.impl;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.model.Model;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.Mson;
import com.minelittlepony.mson.api.event.MsonModelsReadyCallback;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.impl.components.JsonBox;
import com.minelittlepony.mson.impl.components.JsonCuboid;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MsonImpl implements Mson, IdentifiableResourceReloadListener {

    private static final Identifier ID = new Identifier("mson", "models");

    static final MsonImpl INSTANCE = new MsonImpl();

    public static Mson instance() {
        return INSTANCE;
    }

    private final Map<Identifier, Key<?>> registeredModels = new HashMap<>();

    final Map<Identifier, JsonContext.Constructor<?>> componentTypes = new HashMap<>();

    private ModelFoundry foundry = new ModelFoundry(this);

    private MsonImpl() {
        componentTypes.put(JsonCuboid.ID, JsonCuboid::new);
        componentTypes.put(JsonBox.ID, JsonBox::new);
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer sync, ResourceManager sender,
            Profiler serverProfiler, Profiler clientProfiler,
            Executor serverExecutor, Executor clientExecutor) {

        foundry = new ModelFoundry(this);
        CompletableFuture<?>[] tasks = registeredModels.values().stream().map(key -> {
            return CompletableFuture.runAsync(() -> {
                serverProfiler.startTick();
                clientProfiler.push("Loading MSON models - " + key.getId());
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
    public <T extends Model & MsonModel> ModelKey<T> registerModel(Identifier id, Class<T> implementation) {
        Objects.requireNonNull(id, "Id must not be null");
        Objects.requireNonNull(implementation, "Implementation class must not be null");
        checkNamespace(id.getNamespace());

        if (registeredModels.containsKey(id) && registeredModels.get(id).clazz.equals(implementation)) {
            throw new IllegalArgumentException(String.format("A model with the id `%s` was already registered", id.toString()));
        }

        return (ModelKey<T>)registeredModels.computeIfAbsent(id, i -> new Key<>(id, implementation));
    }

    @Override
    public void registerComponentType(Identifier id, JsonContext.Constructor<?> constructor) {
        Objects.requireNonNull(id, "Id must not be null");
        Objects.requireNonNull(constructor, "Constructor must not be null");
        checkNamespace(id.getNamespace());

        if (componentTypes.containsKey(id)) {
            throw new IllegalArgumentException(String.format("A component with the id `%s` was already registered", id.toString()));
        }

        componentTypes.put(id, constructor);
    }

    private void checkNamespace(String namespace) {
        if ("minecraft".equalsIgnoreCase(namespace)) {
            throw new IllegalArgumentException("Id must have a namespace other than `minecraft`.");
        }
        if ("mson".equalsIgnoreCase(namespace)) {
            throw new IllegalArgumentException("`mson` is a reserved namespace.");
        }
    }

    class Key<T extends Model & MsonModel> implements ModelKey<T> {

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
                t.init(foundry.getModelData(this).createContext(t));
                return t;
            } catch (InstantiationException | IllegalAccessException e) {
                return null;
            }
        }

    }
}
