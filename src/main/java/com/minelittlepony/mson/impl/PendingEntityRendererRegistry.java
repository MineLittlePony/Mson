package com.minelittlepony.mson.impl;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
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
import java.util.function.Predicate;

public final class PendingEntityRendererRegistry implements EntityRendererRegistry {
    public final PendingRegistrations<
                    Identifier,
                    Map.Entry<Predicate<AbstractClientPlayerEntity>, Function<EntityRendererFactory.Context, ? extends PlayerEntityRenderer>>
                > player = new PendingRegistrations<>(new Identifier("mson", "renderers/player"), (registry, key, entry) -> {
                    registry.registerPlayerRenderer(key, entry.getKey(), entry.getValue());
                });
    public final PendingRegistrations<
                    EntityType<?>,
                    Function<EntityRendererFactory.Context, ? extends EntityRenderer<?>>
                > entity = new PendingRegistrations<>(new Identifier("mson", "renderers/entity"), EntityRendererRegistry::registerEntityRenderer);
    public final PendingRegistrations<
                    BlockEntityType<?>,
                    Function<BlockEntityRendererFactory.Context, ? extends BlockEntityRenderer<?>>
                > block = new PendingRegistrations<>(new Identifier("mson", "renderers/block"), EntityRendererRegistry::registerBlockRenderer);

    @Override
    public <T extends PlayerEntityRenderer> void registerPlayerRenderer(Identifier skinType, Predicate<AbstractClientPlayerEntity> playerPredicate, Function<Context, T> constructor) {
        player.register(skinType, Map.entry(playerPredicate, constructor));
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

    public class PendingRegistrations<Key, Entry> {
        private final HashMap<Key, Entry> entries = new HashMap<>();

        private final Registerable<Key, Entry> registerable;

        private boolean waiting;

        @Nullable
        private EntityRendererRegistry runtimeRegistry;

        private final Identifier registryId;

        public PendingRegistrations(Identifier registryId, Registerable<Key, Entry> registerable) {
            this.registryId = registryId;
            this.registerable = registerable;
        }

        public void register(Key key, Entry entry) {
            entries.put(key, entry);
            if (runtimeRegistry != null && !waiting) {
                registerable.register(runtimeRegistry, key, entry);
            }
        }

        void reload() {
            boolean delayed = waiting;
            waiting = false;
            if (runtimeRegistry != null) {
                MsonImpl.LOGGER.info(delayed ? "Running delayed initialization for registry '{}'" : "Initializing registry '{}'", registryId);
                entries.forEach((k, v) -> registerable.register(runtimeRegistry, k, v));
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

        interface Registerable<Key, Entry> {
            void register(EntityRendererRegistry registry, Key key, Entry entry);
        }
    }
}
