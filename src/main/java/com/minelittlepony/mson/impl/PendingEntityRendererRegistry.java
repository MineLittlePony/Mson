package com.minelittlepony.mson.impl;

import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

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
                    Map.Entry<Predicate<ClientAvatarEntity>, Function<EntityRendererProvider.Context, ? extends AvatarRenderer<?>>>
                > player = new PendingRegistrations<>(MsonImpl.id("renderers/player"));
    final PendingRegistrations<Identifier,
                    Map.Entry<Predicate<AvatarRenderState>, Function<EntityRendererProvider.Context, ? extends AvatarRenderer<?>>>
                > playerState = new PendingRegistrations<>(MsonImpl.id("renderers/player_render_state"));
    final PendingRegistrations<EntityType<?>,
                    Map.Entry<Either<Unit, Predicate<Entity>>, Function<EntityRendererProvider.Context, ? extends EntityRenderer<?, ?>>>
                > entity = new PendingRegistrations<>(MsonImpl.id("renderers/entity"));
    final PendingRegistrations<EntityType<?>,
                    Map.Entry<Predicate<EntityRenderState>, Function<EntityRendererProvider.Context, ? extends EntityRenderer<?, ?>>>
                > entityState = new PendingRegistrations<>(MsonImpl.id("renderers/entity_render_state"));
    final PendingRegistrations<BlockEntityType<?>,
                    Map.Entry<Either<Unit, Predicate<BlockEntity>>, Function<BlockEntityRendererProvider.Context, ? extends BlockEntityRenderer<?, ?>>>
                > block = new PendingRegistrations<>(MsonImpl.id("renderers/block_entity"));
    final PendingRegistrations<BlockEntityType<?>,
        Map.Entry<Predicate<BlockEntityRenderState>, Function<BlockEntityRendererProvider.Context, ? extends BlockEntityRenderer<?, ?>>>
                > blockState = new PendingRegistrations<>(MsonImpl.id("renderers/block_entity_state"));

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Avatar & ClientAvatarEntity, T extends AvatarRenderer<E>> void registerPlayerRenderer(Identifier skinType, Predicate<? super E> playerPredicate, Function<EntityRendererProvider.Context, T> constructor) {
        player.register(skinType, Map.entry((Predicate<ClientAvatarEntity>)playerPredicate, constructor));
    }

    @Override
    public <E extends Avatar & ClientAvatarEntity, T extends AvatarRenderer<E>> void registerPlayerStateRenderer(Identifier skinType, Predicate<AvatarRenderState> statePredicate, Function<EntityRendererProvider.Context, T> constructor) {
        playerState.register(skinType, Map.entry(statePredicate, constructor));
    }

    @Override
    public <T extends Entity, R extends EntityRenderer<?, ?>> void registerEntityRenderer(EntityType<T> type, Function<EntityRendererProvider.Context, R> constructor) {
        entity.register(type, Map.entry(Either.left(Unit.INSTANCE), constructor));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity, R extends EntityRenderer<?, ?>> void registerEntityRenderer(EntityType<T> type, Predicate<? super T> condition, Function<EntityRendererProvider.Context, R> constructor) {
        entity.register(type, Map.entry(Either.right((Predicate<Entity>)condition), constructor));
    }

    @Override
    public <T extends Entity, R extends EntityRenderer<?, ?>> void registerEntityStateRenderer(EntityType<T> type, Predicate<EntityRenderState> condition, Function<EntityRendererProvider.Context, R> constructor) {
        entityState.register(type, Map.entry(condition, constructor));
    }

    @Override
    public <P extends BlockEntity, R extends BlockEntityRenderer<?, ?>> void registerBlockRenderer(BlockEntityType<P> type, Function<BlockEntityRendererProvider.Context, R> constructor) {
        block.register(type, Map.entry(Either.left(Unit.INSTANCE), constructor));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P extends BlockEntity, R extends BlockEntityRenderer<?, ?>> void registerBlockRenderer(BlockEntityType<P> type, Predicate<? super P> condition, Function<BlockEntityRendererProvider.Context, R> constructor) {
        block.register(type, Map.entry(Either.right((Predicate<BlockEntity>)condition), constructor));
    }

    @Override
    public <P extends BlockEntity, R extends BlockEntityRenderer<?, ?>> void registerBlockStateRenderer(BlockEntityType<P> type, Predicate<BlockEntityRenderState> condition, Function<BlockEntityRendererProvider.Context, R> constructor) {
        blockState.register(type, Map.entry((Predicate<BlockEntityRenderState>)condition, constructor));
    }

    void initialize() {
        player.reload();
        playerState.reload();
        entity.reload();
        entityState.reload();
        block.reload();
        blockState.reload();
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
