package com.minelittlepony.mson.impl;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
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
import com.minelittlepony.mson.impl.export.VanillaModelExportWriter;
import com.minelittlepony.mson.impl.key.AbstractModelKeyImpl;
import com.minelittlepony.mson.impl.model.JsonBox;
import com.minelittlepony.mson.impl.model.JsonCompound;
import com.minelittlepony.mson.impl.model.JsonCone;
import com.minelittlepony.mson.impl.model.JsonImport;
import com.minelittlepony.mson.impl.model.JsonPlanar;
import com.minelittlepony.mson.impl.model.JsonPlane;
import com.minelittlepony.mson.impl.model.JsonQuads;
import com.minelittlepony.mson.impl.model.JsonSlot;
import com.minelittlepony.mson.impl.skeleton.PartSkeleton;
import com.minelittlepony.mson.impl.skeleton.SkeletonisedModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
        componentTypes.put(JsonCompound.ID, JsonCompound::new);
        componentTypes.put(JsonBox.ID, JsonBox::new);
        componentTypes.put(JsonPlane.ID, JsonPlane::new);
        componentTypes.put(JsonPlanar.ID, JsonPlanar::new);
        componentTypes.put(JsonSlot.ID, JsonSlot::new);
        componentTypes.put(JsonCone.ID, JsonCone::new);
        componentTypes.put(JsonQuads.ID, JsonQuads::new);
        componentTypes.put(JsonImport.ID, JsonImport::new);
    }

    public void registerVanillaModels(Map<EntityModelLayer, TexturedModelData> modelParts) {
        modelParts.forEach((layer, vanilla) -> {
            Identifier id = new Identifier(layer.getId().getNamespace(), String.format("mson/%s", layer.getId().getPath()));
            ((MsonImpl.KeyHolder)vanilla).setKey(registeredModels.computeIfAbsent(id, VanillaKey::new));
        });
        if (DEBUG) {
            new VanillaModelExportWriter().exportAll(FabricLoader.getInstance().getGameDir().resolve("debug_model_export").normalize());
        }
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer sync, ResourceManager sender,
            Profiler serverProfiler, Profiler clientProfiler,
            Executor serverExecutor, Executor clientExecutor) {
        foundry = new ModelFoundry(sender, serverExecutor, serverProfiler, clientProfiler);
        return MinecraftClient.getInstance().getEntityModelLoader().reload(sync, sender, serverProfiler, clientProfiler, serverExecutor, clientExecutor).thenCompose(v -> {
            return CompletableFuture.allOf(sender.findResources("models", id -> id.getPath().endsWith(".json"))
                    .entrySet()
                    .stream()
                    .map(entry -> {
                        Identifier id = entry.getKey().withPath(p -> p.replace("models/", "").replace(".json", ""));
                        return foundry.loadJsonModel(id, entry.getKey(), entry.getValue(), registeredModels.containsKey(id));
                    })
                    .toArray(CompletableFuture[]::new))
                    .thenCompose(sync::whenPrepared)
                    .thenRunAsync(renderers::initialize, clientExecutor);
        });
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

    public interface KeyHolder {
        void setKey(ModelKey<?> key);
    }

    private final class VanillaKey<T extends Model> extends Key<T> {
        VanillaKey(Identifier id) {
            super(id, null);
        }

        @Override
        public <V extends T> V createModel() {
            throw new IllegalStateException("Cannot create a model for a key (" + getId() + ") with unknown type. For built-in models please use createModel(factory)");
        }
    }

    private class Key<T extends Model> extends AbstractModelKeyImpl<T> {
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
        public Optional<ModelPart> createTree() {
            return getModelData().map(context -> {
                ModelContext.Locals locals = new ModelLocalsImpl(context.getLocals());

                Map<String, ModelPart> tree = new HashMap<>();
                context.createContext(null, locals).getTree(tree);

                return new ModelPart(new ArrayList<>(), tree);
            });
        }

        @Override
        public <V extends T> V createModel(MsonModel.Factory<V> factory) {
            Preconditions.checkNotNull(factory, "Factory should not be null");

            return getModelData().map(context -> {
                ModelContext.Locals locals = new ModelLocalsImpl(context.getLocals());

                Map<String, ModelPart> tree = new HashMap<>();
                ModelContext ctx = context.createContext(null, locals);
                ctx.getTree(tree);

                ModelPart root = new ModelPart(new ArrayList<>(), tree);
                V t = factory.create(root);

                if (t instanceof SkeletonisedModel) {
                    ((SkeletonisedModel)t).setSkeleton(context.getSkeleton()
                            .map(s -> s.getSkeleton(root))
                            .orElseGet(() -> PartSkeleton.of(root)));
                }
                if (t instanceof MsonModel) {
                    if (ctx instanceof RootContext) {
                        ((RootContext)ctx).setModel(t);
                    }
                    ((MsonModel)t).init(ctx);
                }
                return t;
            })
            .orElseThrow(() -> new IllegalStateException("Model file for " + getId() + " was not loaded!"));
        }

        @Override
        public Optional<JsonContext> getModelData() {
            Preconditions.checkState(foundry != null, "You're too early. Wait for Mson to load first.");
            try {
                return foundry.getModelData(this);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Could not create model", e);
            }
        }
    }
}
