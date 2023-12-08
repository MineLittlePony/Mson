package com.minelittlepony.mson.api;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Registry for adding entity and player renderers to the game.
 * Renderers added here will be inserted after Mson models are refreshed.
 */
public interface EntityRendererRegistry {
    /**
     * Adds a custom player renderer.
     *
     * @deprecated Use the more flexible predicate version instead.
     */
    @Deprecated
    default <T extends PlayerEntityRenderer> void registerPlayerRenderer(String skinType, Function<EntityRendererFactory.Context, T> constructor) {
        registerPlayerRenderer(new Identifier(skinType), player -> player.getSkinTextures().model().getName().equalsIgnoreCase(skinType), constructor);
    }
    /**
     * Adds a custom player renderer.
     *
     * @param playerPredicate Predicate to determine which players this renderer should be used for.
     * @param constructor The renderer factory
     */
    <T extends PlayerEntityRenderer> void registerPlayerRenderer(Identifier skinType, Predicate<AbstractClientPlayerEntity> playerPredicate, Function<EntityRendererFactory.Context, T> constructor);

    /**
     * Adds a custom entity renderer.
     */
    <T extends Entity, R extends EntityRenderer<?>> void registerEntityRenderer(EntityType<T> type, Function<EntityRendererFactory.Context, R> constructor);

    /**
     * Adds a custom block entity renderer.
     */
    <P extends BlockEntity, R extends BlockEntityRenderer<?>> void registerBlockRenderer(BlockEntityType<P> type, Function<BlockEntityRendererFactory.Context, R> constructor);
}
