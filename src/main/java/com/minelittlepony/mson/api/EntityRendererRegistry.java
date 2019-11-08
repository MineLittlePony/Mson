package com.minelittlepony.mson.api;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.Entity;

import java.util.function.Function;

/**
 * Registry for adding entity and player renderers to the game.
 * Renderers added here will be inserted after Mson models are refreshed.
 */
public interface EntityRendererRegistry {
    /**
     * Adds a custom player renderer.
     */
    <R extends PlayerEntityRenderer> void registerPlayerRenderer(String skinType, Function<EntityRenderDispatcher, R> constructor);

    /**
     * Adds a custom entity renderer.
     */
    <T extends Entity, R extends EntityRenderer<? super T>> void registerEntityRenderer(Class<T> type, Function<EntityRenderDispatcher, R> constructor);
}
