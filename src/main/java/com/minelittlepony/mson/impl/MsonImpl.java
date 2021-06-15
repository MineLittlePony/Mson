package com.minelittlepony.mson.impl;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.Mson;
import com.minelittlepony.mson.impl.StoredModelData.RootContext;
import com.minelittlepony.mson.impl.key.AbstractModelKeyImpl;
import com.minelittlepony.mson.impl.model.JsonBox;
import com.minelittlepony.mson.impl.model.JsonCuboid;
import com.minelittlepony.mson.impl.model.JsonCone;
import com.minelittlepony.mson.impl.model.JsonPlanar;
import com.minelittlepony.mson.impl.model.JsonPlane;
import com.minelittlepony.mson.impl.model.JsonQuads;
import com.minelittlepony.mson.impl.model.JsonSlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class MsonImpl implements Mson, IdentifiableResourceReloadListener {
    public static final Logger LOGGER = LogManager.getLogger("Mson");
    public static final MsonImpl INSTANCE = new MsonImpl();

    private static final Identifier ID = new Identifier("mson", "models");

    static boolean DEBUG = false;

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

        foundry = new ModelFoundry(sender, serverExecutor, serverProfiler, clientProfiler);
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

    @Override
    public PendingEntityRendererRegistry getEntityRendererRegistry() {
        return renderers;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Model> ModelKey<T> registerModel(Identifier id, MsonModel.Factory<T> constructor) {
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

    private final class Key<T extends Model> extends AbstractModelKeyImpl<T> {
        private final MsonModel.Factory<T> constr;

        public Key(Identifier id, MsonModel.Factory<T> constr) {
            this.id = id;
            this.constr = constr;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <V extends T> V createModel() {
            return (V)createModel(constr);
        }

        @Override
        public <V extends T> V createModel(MsonModel.Factory<V> factory) {
            Preconditions.checkState(foundry != null, "You're too early. Wait for Mson to load first.");

            JsonContext context = getModelData();
            ModelContext.Locals locals = new LocalsImpl(getId(), context.getVariables());

            Map<String, ModelPart> tree = new HashMap<>();
            ModelContext ctx = context.createContext(null, locals);
            ctx.getTree(tree);

            V t = factory.create(new ModelPart(new ArrayList<>(), tree));
            if (t instanceof MsonModel) {
                if (ctx instanceof RootContext) {
                    ((RootContext)ctx).setModel(t);
                }
                ((MsonModel)t).init(ctx);
            }
            return t;
        }

        @Override
        public JsonContext getModelData() {
            Preconditions.checkState(foundry != null, "You're too early. Wait for Mson to load first.");
            try {
                return foundry.getModelData(this);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Could not create model", e);
            }
        }
    }
}
