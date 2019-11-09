package com.minelittlepony.mson.impl;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minelittlepony.mson.api.EntityRendererRegistry;
import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.Mson;
import com.minelittlepony.mson.impl.key.AbstractModelKeyImpl;
import com.minelittlepony.mson.impl.model.JsonBox;
import com.minelittlepony.mson.impl.model.JsonCuboid;
import com.minelittlepony.mson.impl.model.JsonCone;
import com.minelittlepony.mson.impl.model.JsonPlanar;
import com.minelittlepony.mson.impl.model.JsonPlane;
import com.minelittlepony.mson.impl.model.JsonQuads;
import com.minelittlepony.mson.impl.model.JsonSlot;

import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class MsonImpl implements Mson, IdentifiableResourceReloadListener {

    private static final Identifier ID = new Identifier("mson", "models");

    static final MsonImpl INSTANCE = new MsonImpl();

    static boolean debug = false;

    public static final Logger LOGGER = LogManager.getLogger("Mson");

    public static Mson instance() {
        return INSTANCE;
    }

    private final PendingEntityRendererRegistry renderers = new PendingEntityRendererRegistry();

    private final Map<Identifier, Key<?>> registeredModels = new HashMap<>();

    final Map<Identifier, JsonComponent.Constructor<?>> componentTypes = new HashMap<>();

    @Nullable
    ModelFoundry foundry;

    private MsonImpl() {
        componentTypes.put(JsonCuboid.ID, JsonCuboid::new);
        componentTypes.put(JsonBox.ID, JsonBox::new);
        componentTypes.put(JsonPlane.ID, JsonPlane::new);
        componentTypes.put(JsonPlanar.ID, JsonPlanar::new);
        componentTypes.put(JsonSlot.ID, JsonSlot::new);
        componentTypes.put(JsonCone.ID, JsonCone::new);
        componentTypes.put(JsonQuads.ID, JsonQuads::new);
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer sync, ResourceManager sender,
            Profiler serverProfiler, Profiler clientProfiler,
            Executor serverExecutor, Executor clientExecutor) {

        foundry = new ModelFoundry(sender, serverExecutor, serverProfiler, clientProfiler, this);
        CompletableFuture<?>[] tasks = registeredModels.values().stream()
                .map(key -> foundry.loadJsonModel(key.getId()))
                .toArray(i -> new CompletableFuture[i]);

        CompletableFuture<?> all = CompletableFuture.allOf(tasks);

        sync.getClass();
        return all.thenCompose(sync::whenPrepared).thenRunAsync(() -> {
            renderers.initialize((EntityRendererRegistry)MinecraftClient.getInstance().getEntityRenderManager());
        }, clientExecutor);
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Model & MsonModel> ModelKey<T> registerModel(Identifier id, Supplier<T> constructor) {
        Objects.requireNonNull(id, "Id must not be null");
        Objects.requireNonNull(constructor, "Implementation class must not be null");
        checkNamespace(id.getNamespace());

        if (registeredModels.containsKey(id)) {
            throw new IllegalArgumentException(String.format("A model with the id `%s` was already registered", id.toString()));
        }

        return (ModelKey<T>)registeredModels.computeIfAbsent(id, i -> new Key<>(id, constructor));
    }

    @Override
    public void registerComponentType(Identifier id, JsonComponent.Constructor<?> constructor) {
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
        if (!debug && "mson".equalsIgnoreCase(namespace)) {
            throw new IllegalArgumentException("`mson` is a reserved namespace.");
        }
        if ("dynamic".equalsIgnoreCase(namespace)) {
            throw new IllegalArgumentException("`dynamic` is a reserved namespace.");
        }
    }

    class Key<T extends Model & MsonModel> extends AbstractModelKeyImpl<T> {

        private final Supplier<T> constr;

        public Key(Identifier id, Supplier<T> constr) {
            this.id = id;
            this.constr = constr;
        }

        @Override
        public T createModel() {
            if (foundry == null) {
                throw new IllegalStateException("You're too early. Wait for Mson to load first.");
            }

            T t = constr.get();
            try {
                JsonContext context = foundry.getModelData(this);

                t.init(context.createContext(t, new LocalizedJsonContext(context)));
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Could not create model", e);
            }
            return t;
        }
    }

    @Override
    public EntityRendererRegistry getEntityRendererRegistry() {
        return renderers;
    }
}
