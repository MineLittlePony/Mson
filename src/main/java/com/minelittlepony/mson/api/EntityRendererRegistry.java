package com.minelittlepony.mson.api;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

import java.util.function.Function;

/**
 * Registry for adding entity and player renderers to the game.
 * Renderers added here will be inserted after Mson models are refreshed.
 */
public interface EntityRendererRegistry {
    /**
     * Adds a custom player renderer.
     */
    <T extends PlayerEntityRenderer> void registerPlayerRenderer(String skinType, Function<EntityRendererFactory.Context, T> constructor);

    /**
     * Adds a custom entity renderer.
     */
    <T extends Entity, R extends EntityRenderer<?>> void registerEntityRenderer(EntityType<T> type, Function<EntityRendererFactory.Context, R> constructor);

    /**
     * Adds a custom block entity renderer.
     */
    <P extends BlockEntity, R extends BlockEntityRenderer<?>> void registerBlockRenderer(BlockEntityType<P> type, Function<BlockEntityRenderDispatcher, R> constructor);
}
