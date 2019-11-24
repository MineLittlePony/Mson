package com.minelittlepony.mson.impl;

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

    private final Map<String, Function<EntityRenderDispatcher, ? extends PlayerEntityRenderer>> pendingPlayerRenderers = new HashMap<>();
    private final PendingList<Entity, ?> pendingEntityRenderers = new PendingList<>();

    @Nullable
    private EntityRendererRegistry runtimeRegistry;

    @Override
    public <R extends PlayerEntityRenderer> void registerPlayerRenderer(String skinType, Function<EntityRenderDispatcher, R> constructor) {
        pendingPlayerRenderers.put(skinType, constructor);
        if (runtimeRegistry != null) {
            runtimeRegistry.registerPlayerRenderer(skinType, constructor);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Entity, R extends EntityRenderer<? super T>> void registerEntityRenderer(EntityType<T> type, Function<EntityRenderDispatcher, R> constructor) {
        ((PendingList<T, R>)pendingEntityRenderers).put(type, constructor);
        if (runtimeRegistry != null) {
            runtimeRegistry.registerEntityRenderer(type, constructor);
        }
    }

    void initialize(EntityRendererRegistry runtimeRegistry) {
        if (runtimeRegistry instanceof PendingEntityRendererRegistry) {
            throw new IllegalStateException("Uh oh");
        }
        this.runtimeRegistry = runtimeRegistry;
        pendingPlayerRenderers.forEach(runtimeRegistry::registerPlayerRenderer);
        pendingEntityRenderers.forEach(runtimeRegistry::registerEntityRenderer);
    }

    class PendingList<T extends Entity, R extends EntityRenderer<? super T>> extends HashMap<EntityType<T>, Function<EntityRenderDispatcher, R>> {
        private static final long serialVersionUID = -4586716048493207127L;
    }
}
