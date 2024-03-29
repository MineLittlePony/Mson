package com.minelittlepony.mson.impl;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
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

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.exception.FutureAwaitException;
import com.minelittlepony.mson.api.model.traversal.PartSkeleton;
import com.minelittlepony.mson.api.model.traversal.SkeletonisedModel;
import com.minelittlepony.mson.api.parser.ModelFormat;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.api.Mson;
import com.minelittlepony.mson.impl.key.AbstractModelKeyImpl;
import com.minelittlepony.mson.impl.model.RootContext;
import com.minelittlepony.mson.impl.model.bbmodel.BBModelFormat;
import com.minelittlepony.mson.impl.model.json.MsonModelFormat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class MsonImpl implements Mson, IdentifiableResourceReloadListener {
    public static final Logger LOGGER = LogManager.getLogger("Mson");
    public static final MsonImpl INSTANCE = new MsonImpl();

    private static final Identifier ID = new Identifier("mson", "models");

    private final PendingEntityRendererRegistry renderers = new PendingEntityRendererRegistry();

    private final Map<Identifier, Key<?>> registeredModels = new HashMap<>();

    final Map<Identifier, ModelFormat<?>> formatHandlers = new HashMap<>();
    final Map<String, Set<ModelFormat<?>>> handlersByExtension = new HashMap<>();

    private final AtomicReference<ModelFoundry> foundry = new AtomicReference<>(new ModelFoundry(this));

    private final Set<Identifier> vanillaModels = new HashSet<>();

    private MsonImpl() {
        registerModelFormatHandler(ModelFormat.MSON, MsonModelFormat.INSTANCE);
        registerModelFormatHandler(ModelFormat.BBMODEL, BBModelFormat.INSTANCE);
    }

    public Stream<ModelFormat<?>> getHandlers(String extension) {
        return handlersByExtension.getOrDefault(extension, Set.of()).stream();
    }

    public void registerVanillaModels(Map<EntityModelLayer, TexturedModelData> modelParts) {
        synchronized (vanillaModels) {
            vanillaModels.clear();
            modelParts.forEach((layer, vanilla) -> {
                Identifier id = new Identifier(layer.getId().getNamespace(), String.format("mson/%s", layer.getId().getPath()));
                ((MsonImpl.KeyHolder)vanilla).setKey(registeredModels.computeIfAbsent(id, VanillaKey::new));
                vanillaModels.add(id);
            });
        }

        if (MsonMod.DEBUG) {
            Test.exportVanillaModels(foundry.get());
        }
    }

    private CompletableFuture<Void> requireVanillaModels(Synchronizer sync, ResourceManager sender,
            Profiler prepareProfiler, Profiler applyProfiler,
            Executor prepareExecutor, Executor applyExecutor) {
        boolean hasVanillaModels;
        synchronized (vanillaModels) {
            hasVanillaModels = !vanillaModels.isEmpty();
        }
        if (!hasVanillaModels) {
            return MinecraftClient.getInstance().getEntityModelLoader()
                    .reload(sync, sender, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer sync, ResourceManager sender,
            Profiler prepareProfiler, Profiler applyProfiler,
            Executor prepareExecutor, Executor applyExecutor) {
        ModelFoundry loadingFoundry = new ModelFoundry(this).setWorker(LoadWorker.async(prepareExecutor, prepareProfiler));

        return requireVanillaModels(sync, sender, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor)
                .thenComposeAsync(v -> loadingFoundry.load(), prepareExecutor)
                .thenCompose(sync::whenPrepared)
                .thenRunAsync(() -> {
                    foundry.set(loadingFoundry.setWorker(LoadWorker.sync()));
                    renderers.initialize();

                    if (MsonMod.DEBUG) {
                        Test.exportBbModels(registeredModels.values());
                    }

                }, applyExecutor);
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

    public static void checkNamespace(String namespace) {
        Preconditions.checkArgument(!"minecraft".equalsIgnoreCase(namespace), "Id must have a namespace other than `minecraft`.");
        Preconditions.checkArgument(!"mson".equalsIgnoreCase(namespace), "`mson` is a reserved namespace.");
        Preconditions.checkArgument(!"dynamic".equalsIgnoreCase(namespace), "`dynamic` is a reserved namespace.");
    }

    @Override
    public ModelFormat<JsonElement> getDefaultFormatHandler() {
        return MsonModelFormat.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <Data, T extends ModelFormat<Data>> T registerModelFormatHandler(Identifier id, T format) {
        Objects.requireNonNull(id, "Id must not be null");
        Objects.requireNonNull(format, "Format must not be null");
        Objects.requireNonNull(format.getFileExtension(), "Format must have a valid, non-null, non-empty file extension");
        Preconditions.checkArgument(!format.getFileExtension().isEmpty(), "Format must have a valid, non-null, non-empty file extension");
        Preconditions.checkArgument(!format.getFileExtension().startsWith("."), "Extension must not have a leading decimal (.)");
        if (formatHandlers.containsKey(id)) {
            LOGGER.warn("A format handler with id `{}`and extension {} and has already been registered.", id, formatHandlers.get(id).getFileExtension());
            return (T)formatHandlers.get(id);
        }
        formatHandlers.put(id, format);
        handlersByExtension.computeIfAbsent(format.getFileExtension(), e -> new HashSet<>()).add(format);
        return format;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <Data> Optional<ModelFormat<Data>> getFormatHandler(Identifier id) {
        return Optional.ofNullable((ModelFormat<Data>)formatHandlers.get(id));
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
                return context.createContext(null, null, context.getLocals().bake()).toTree();
            });
        }

        @Override
        public <V extends T> V createModel(MsonModel.Factory<V> factory) {
            Preconditions.checkNotNull(factory, "Factory should not be null");

            return getModelData().map(context -> {
                ModelContext ctx = context.createContext(null, null, context.getLocals().bake());

                ModelPart root = ctx.toTree();
                V t = factory.create(root);

                if (t instanceof SkeletonisedModel) {
                    ((SkeletonisedModel)t).setSkeleton(context.getSkeleton()
                            .map(s -> PartSkeleton.of(root, s))
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
        public Optional<FileContent<?>> getModelData() {
            try {
                return foundry.get().getOrLoadModelData(this);
            } catch (InterruptedException | ExecutionException | FutureAwaitException e) {
                throw new RuntimeException("Could not create model", e);
            }
        }
    }
}
