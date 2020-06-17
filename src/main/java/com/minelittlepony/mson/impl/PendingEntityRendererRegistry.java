package com.minelittlepony.mson.impl;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

import com.minelittlepony.mson.api.EntityRendererRegistry;

import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

final class PendingEntityRendererRegistry implements EntityRendererRegistry {

    private final RendererList<
                    String,
                    EntityRenderDispatcher,
                    PlayerEntityRenderer
                > player = new RendererList<>(EntityRendererRegistry::registerPlayerRenderer);
    private final RendererList<
                    EntityType<?>,
                    EntityRenderDispatcher,
                    EntityRenderer<?>
                > entity = new RendererList<>(EntityRendererRegistry::registerEntityRenderer);
    private final RendererList<
                    BlockEntityType<?>,
                    BlockEntityRenderDispatcher,
                    BlockEntityRenderer<?>
                > block = new RendererList<>(EntityRendererRegistry::registerBlockRenderer);

    @Override
    public <T extends PlayerEntityRenderer> void registerPlayerRenderer(String skinType, Function<EntityRenderDispatcher, T> constructor) {
        player.register(skinType, constructor);
    }

    @Override
    public <T extends Entity, R extends EntityRenderer<?>> void registerEntityRenderer(EntityType<T> type, Function<EntityRenderDispatcher, R> constructor) {
        entity.register(type, constructor);
    }

    @Override
    public <P extends BlockEntity, R extends BlockEntityRenderer<?>> void registerBlockRenderer(BlockEntityType<P> type, Function<BlockEntityRenderDispatcher, R> constructor) {
        block.register(type, constructor);
    }

    void initialize() {
        EntityRendererRegistry runtimeEntityRegistry = (EntityRendererRegistry)MinecraftClient.getInstance().getEntityRenderManager();
        EntityRendererRegistry runtimeBlockEntityRegistry = (EntityRendererRegistry)BlockEntityRenderDispatcher.INSTANCE;

        player.publish(runtimeEntityRegistry);
        entity.publish(runtimeEntityRegistry);
        block.publish(runtimeBlockEntityRegistry);
    }

    class RendererList<Type, Dispatcher, Renderer> {
        private final HashMap<? extends Type, ? extends Function<Dispatcher, Renderer>> entries = new HashMap<>();

        private final RegisterAction<Type, Dispatcher, Renderer> runtimeAdd;

        @Nullable
        private EntityRendererRegistry runtimeRegistry;

        public RendererList(RegisterAction<Type, Dispatcher, Renderer> runtimeAdd) {
            this.runtimeAdd = runtimeAdd;
        }

        @SuppressWarnings("unchecked")
        public <T extends Type, R extends Renderer> void register(T type, Function<Dispatcher, R> constructor) {
            ((Map<T, Function<Dispatcher, R>>)(Object)entries).put(type, constructor);
            if (runtimeRegistry != null) {
                ((RegisterAction<T, Dispatcher, R>)runtimeAdd).call(runtimeRegistry, type, constructor);
            }
        }

        void publish(EntityRendererRegistry runtimeRegistry) {
            if (runtimeRegistry instanceof PendingEntityRendererRegistry) {
                throw new IllegalStateException("Uh oh");
            }
            this.runtimeRegistry = runtimeRegistry;
            entries.forEach((k, v) -> runtimeAdd.call(runtimeRegistry, k, v));
        }
    }

    interface RegisterAction<Type, Dispatcher, Renderer> {
        void call(EntityRendererRegistry registry, Type type, Function<Dispatcher, Renderer> constructor);
    }
}
