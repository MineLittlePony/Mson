package com.minelittlepony.mson.impl;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
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

    static boolean DEBUG = false;

    public static final Logger LOGGER = LogManager.getLogger("Mson");

    public static MsonImpl instance() {
        return INSTANCE;
    }

    private final PendingEntityRendererRegistry renderers = new PendingEntityRendererRegistry();

    private final Map<Identifier, Key<?>> registeredModels = new HashMap<>();

    final Map<Identifier, JsonComponent.Factory<?>> componentTypes = new HashMap<>();

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
        return all.thenCompose(sync::whenPrepared).thenRunAsync(renderers::initialize, clientExecutor);
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends MsonModel> ModelKey<T> registerModel(Identifier id, Supplier<T> constructor) {
        Objects.requireNonNull(id, "Id must not be null");
        Objects.requireNonNull(constructor, "Implementation class must not be null");
        checkNamespace(id.getNamespace());
        Preconditions.checkArgument(!registeredModels.containsKey(id), "A model with the id `%s` was already registered", id);

        return (ModelKey<T>)registeredModels.computeIfAbsent(id, i -> new Key<>(id, constructor));
    }

    @Override
    public void registerComponentType(Identifier id, JsonComponent.Factory<?> constructor) {
        Objects.requireNonNull(id, "Id must not be null");
        Objects.requireNonNull(constructor, "Constructor must not be null");
        checkNamespace(id.getNamespace());
        Preconditions.checkArgument(!componentTypes.containsKey(id), "A component with the id `%s` was already registered", id);

        componentTypes.put(id, constructor);
    }

    private void checkNamespace(String namespace) {
        Preconditions.checkArgument(!"minecraft".equalsIgnoreCase(namespace), "Id must have a namespace other than `minecraft`.");
        Preconditions.checkArgument(DEBUG || !"mson".equalsIgnoreCase(namespace), "`mson` is a reserved namespace.");
        Preconditions.checkArgument(!"dynamic".equalsIgnoreCase(namespace), "`dynamic` is a reserved namespace.");
    }

    class Key<T extends MsonModel> extends AbstractModelKeyImpl<T> {

        private final Supplier<T> constr;

        public Key(Identifier id, Supplier<T> constr) {
            this.id = id;
            this.constr = constr;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <V extends T> V createModel() {
            return (V)createModel(constr);
        }

        @Override
        public <V extends T> V createModel(Supplier<V> supplier) {
            if (foundry == null) {
                throw new IllegalStateException("You're too early. Wait for Mson to load first.");
            }

            JsonContext context = getModelData();
            V t = supplier.get();
            t.init(context.createContext(t, new LocalsImpl(getId(), context)));
            return t;
        }

        @Override
        public JsonContext getModelData() {
            if (foundry == null) {
                throw new IllegalStateException("You're too early. Wait for Mson to load first.");
            }
            try {
                return foundry.getModelData(this);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Could not create model", e);
            }
        }
    }

    @Override
    public PendingEntityRendererRegistry getEntityRendererRegistry() {
        return renderers;
    }
}
