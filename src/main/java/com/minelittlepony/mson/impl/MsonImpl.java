package com.minelittlepony.mson.impl;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

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
import com.minelittlepony.mson.impl.mixin.ModelListAccessor;
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

    private static final Identifier ID = id("models");

    public static Identifier id(String name) {
        return Identifier.of("mson", name);
    }

    private final PendingEntityRendererRegistry renderers = new PendingEntityRendererRegistry();

    private final Map<Identifier, Key<?>> registeredModels = new HashMap<>();

    final Map<Identifier, ModelFormat<?>> formatHandlers = new HashMap<>();
    final Map<String, Set<ModelFormat<?>>> handlersByExtension = new HashMap<>();

    private final AtomicReference<ModelFoundry> foundry = new AtomicReference<>(new ModelFoundry(this));

    @Nullable
    private volatile CompletableFuture<Void> vanillaModelsReloadTask = null;

    private MsonImpl() {
        registerModelFormatHandler(ModelFormat.MSON, MsonModelFormat.INSTANCE);
        registerModelFormatHandler(ModelFormat.BBMODEL, BBModelFormat.INSTANCE);
    }

    public Stream<ModelFormat<?>> getHandlers(String extension) {
        return handlersByExtension.getOrDefault(extension, Set.of()).stream();
    }

    public void onVanillaModelsPrepared(CompletableFuture<Void> reloadTask) {
        synchronized (this) {
            vanillaModelsReloadTask = reloadTask;
        }
    }

    public void onVanillaModelsApplied() {
        synchronized (this) {
            ((ModelListAccessor)MinecraftClient.getInstance().getLoadedEntityModels()).getModelParts().forEach((layer, vanilla) -> {
                Identifier id = layer.id().withPath(p -> String.format("mson/%s", p));
                ((MsonImpl.KeyHolder)vanilla).setKey(registeredModels.computeIfAbsent(id, VanillaKey::new));
            });

            if (MsonMod.DEBUG) {
                Test.exportVanillaModels(foundry.get());
            }
        }
    }

    private CompletableFuture<Void> requireVanillaModels(Synchronizer sync, ResourceManager sender, Executor prepareExecutor, Executor applyExecutor) {
        synchronized (this) {
            if (vanillaModelsReloadTask == null) {
                LOGGER.info("Vanilla models are not ready, preparing them ourselves...");
                MinecraftClient.getInstance().getBakedModelManager().reload(sync, sender, prepareExecutor, applyExecutor);
                if (vanillaModelsReloadTask == null) {
                    LOGGER.info("Vanilla models did not prepare. Some errors may occur");
                    return CompletableFuture.completedFuture((Void)null);
                }
            }
            if (vanillaModelsReloadTask.isDone()) {
                return CompletableFuture.completedFuture((Void)null);
            }
            LOGGER.info("Vanilla models are still preparing. Apply stage will be delayed until vanilla models are ready.");
            return vanillaModelsReloadTask;
        }
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer sync, ResourceManager sender, Executor prepareExecutor, Executor applyExecutor) {
        ModelFoundry loadingFoundry = new ModelFoundry(this).setWorker(LoadWorker.async(prepareExecutor));

        return loadingFoundry.load()
                .thenCompose(sync::whenPrepared)
                .thenComposeAsync(v -> requireVanillaModels(sync, sender, prepareExecutor, applyExecutor), prepareExecutor)
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
