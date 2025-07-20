package com.minelittlepony.mson.impl;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;

import com.minelittlepony.mson.api.EntityRendererRegistry;
import com.mojang.datafixers.util.Either;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

final class PendingEntityRendererRegistry implements EntityRendererRegistry {
    final PendingRegistrations<Identifier,
                    Map.Entry<Predicate<AbstractClientPlayerEntity>, Function<EntityRendererFactory.Context, ? extends PlayerEntityRenderer>>
                > player = new PendingRegistrations<>(MsonImpl.id("renderers/player"));
    final PendingRegistrations<Identifier,
                    Map.Entry<Predicate<PlayerEntityRenderState>, Function<EntityRendererFactory.Context, ? extends PlayerEntityRenderer>>
                > playerState = new PendingRegistrations<>(MsonImpl.id("renderers/player_render_state"));
    final PendingRegistrations<EntityType<?>,
                    Map.Entry<Either<Unit, Predicate<Entity>>, Function<EntityRendererFactory.Context, ? extends EntityRenderer<?, ?>>>
                > entity = new PendingRegistrations<>(MsonImpl.id("renderers/entity"));
    final PendingRegistrations<EntityType<?>,
                    Map.Entry<Predicate<EntityRenderState>, Function<EntityRendererFactory.Context, ? extends EntityRenderer<?, ?>>>
                > entityState = new PendingRegistrations<>(MsonImpl.id("renderers/entity_render_state"));
    final PendingRegistrations<BlockEntityType<?>,
                    Function<BlockEntityRendererFactory.Context, ? extends BlockEntityRenderer<?>>
                > block = new PendingRegistrations<>(MsonImpl.id("renderers/block"));

    @Override
    public <T extends PlayerEntityRenderer> void registerPlayerRenderer(Identifier skinType, Predicate<AbstractClientPlayerEntity> playerPredicate, Function<Context, T> constructor) {
        player.register(skinType, Map.entry(playerPredicate, constructor));
    }

    @Override
    public <T extends PlayerEntityRenderer> void registerPlayerStateRenderer(Identifier skinType, Predicate<PlayerEntityRenderState> statePredicate, Function<EntityRendererFactory.Context, T> constructor) {
        playerState.register(skinType, Map.entry(statePredicate, constructor));
    }

    @Override
    public <T extends Entity, R extends EntityRenderer<?, ?>> void registerEntityRenderer(EntityType<T> type, Function<EntityRendererFactory.Context, R> constructor) {
        entity.register(type, Map.entry(Either.left(Unit.INSTANCE), constructor));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity, R extends EntityRenderer<?, ?>> void registerEntityRenderer(EntityType<T> type, Predicate<? super T> condition, Function<Context, R> constructor) {
        entity.register(type, Map.entry(Either.right((Predicate<Entity>)condition), constructor));
    }

    @Override
    public <T extends Entity, R extends EntityRenderer<?, ?>> void registerEntityStateRenderer(EntityType<T> type, Predicate<EntityRenderState> condition, Function<Context, R> constructor) {
        entityState.register(type, Map.entry(condition, constructor));
    }

    @Override
    public <P extends BlockEntity, R extends BlockEntityRenderer<?>> void registerBlockRenderer(BlockEntityType<P> type, Function<BlockEntityRendererFactory.Context, R> constructor) {
        block.register(type, constructor);
    }

    void initialize() {
        player.reload();
        playerState.reload();
        entity.reload();
        entityState.reload();
        block.reload();
    }

    public class PendingRegistrations<Key, Entry> {
        private final HashMap<Key, Entry> entries = new HashMap<>();

        private boolean waiting;

        @Nullable
        private BiConsumer<Key, Entry> runtimeRegistry;

        private final Identifier registryId;

        public PendingRegistrations(Identifier registryId) {
            this.registryId = registryId;
        }

        public void register(Key key, Entry entry) {
            entries.put(key, entry);
            if (runtimeRegistry != null && !waiting) {
                runtimeRegistry.accept(key, entry);
            }
        }

        void reload() {
            boolean delayed = waiting;
            waiting = false;
            if (runtimeRegistry != null) {
                MsonImpl.LOGGER.info(delayed ? "Running delayed initialization for registry '{}'" : "Initializing registry '{}'", registryId);
                entries.forEach(runtimeRegistry);
            } else {
                MsonImpl.LOGGER.info("Registry '{}' queued for delayed initialization", registryId);
                waiting = true;
            }
        }

        public void publish(BiConsumer<Key, Entry> runtimeRegistry) {
            this.runtimeRegistry = runtimeRegistry;
            if (waiting) {
                reload();
            }
        }

        interface Registerable<Key, Entry> {
            void register(BiConsumer<Key, Entry> registry, Key key, Entry entry);
        }
    }
}
