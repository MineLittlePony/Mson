package com.minelittlepony.mson.impl;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

import com.google.common.base.Preconditions;
import com.minelittlepony.mson.api.EntityRendererRegistry;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class PendingEntityRendererRegistry implements EntityRendererRegistry {

    public final RendererList<
                    String,
                    EntityRendererFactory.Context,
                    PlayerEntityRenderer
                > player = new RendererList<>(new Identifier("mson", "renderers/player"), EntityRendererRegistry::registerPlayerRenderer);
    public final RendererList<
                    EntityType<?>,
                    EntityRendererFactory.Context,
                    EntityRenderer<?>
                > entity = new RendererList<>(new Identifier("mson", "renderers/entity"), EntityRendererRegistry::registerEntityRenderer);
    public final RendererList<
                    BlockEntityType<?>,
                    BlockEntityRendererFactory.Context,
                    BlockEntityRenderer<?>
                > block = new RendererList<>(new Identifier("mson", "renderers/block"), EntityRendererRegistry::registerBlockRenderer);

    @Override
    public <T extends PlayerEntityRenderer> void registerPlayerRenderer(String skinType, Function<EntityRendererFactory.Context, T> constructor) {
        player.register(skinType, constructor);
    }

    @Override
    public <T extends Entity, R extends EntityRenderer<?>> void registerEntityRenderer(EntityType<T> type, Function<EntityRendererFactory.Context, R> constructor) {
        entity.register(type, constructor);
    }

    @Override
    public <P extends BlockEntity, R extends BlockEntityRenderer<?>> void registerBlockRenderer(BlockEntityType<P> type, Function<BlockEntityRendererFactory.Context, R> constructor) {
        block.register(type, constructor);
    }

    void initialize() {
        player.reload();
        entity.reload();
        block.reload();
    }

    public class RendererList<Type, Dispatcher, Renderer> {
        private final HashMap<? extends Type, ? extends Function<Dispatcher, Renderer>> entries = new HashMap<>();

        private final RegisterAction<Type, Dispatcher, Renderer> runtimeAdd;

        private boolean waiting;

        @Nullable
        private EntityRendererRegistry runtimeRegistry;

        private final Identifier registryId;

        public RendererList(Identifier registryId, RegisterAction<Type, Dispatcher, Renderer> runtimeAdd) {
            this.registryId = registryId;
            this.runtimeAdd = runtimeAdd;
        }

        @SuppressWarnings("unchecked")
        public <T extends Type, R extends Renderer> void register(T type, Function<Dispatcher, R> constructor) {
            ((Map<T, Function<Dispatcher, R>>)(Object)entries).put(type, constructor);
            if (runtimeRegistry != null && !waiting) {
                ((RegisterAction<T, Dispatcher, R>)runtimeAdd).call(runtimeRegistry, type, constructor);
            }
        }

        void reload() {
            boolean delayed = waiting;
            waiting = false;
            if (runtimeRegistry != null) {
                MsonImpl.LOGGER.info(delayed ? "Running delayed initialization for registry '{}'" : "Initializing registry '{}'", registryId);
                entries.forEach((k, v) -> runtimeAdd.call(runtimeRegistry, k, v));
            } else {
                MsonImpl.LOGGER.info("Registry '{}' queued for delayed initialization", registryId);
                waiting = true;
            }
        }

        public void publish(EntityRendererRegistry runtimeRegistry) {
            Preconditions.checkArgument(!(runtimeRegistry instanceof PendingEntityRendererRegistry), "Uh oh");
            this.runtimeRegistry = runtimeRegistry;
            if (waiting) {
                reload();
            }
        }
    }

    interface RegisterAction<Type, Dispatcher, Renderer> {
        void call(EntityRendererRegistry registry, Type type, Function<Dispatcher, Renderer> constructor);
    }
}
