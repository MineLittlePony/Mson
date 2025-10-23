package com.minelittlepony.mson.api;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.network.ClientPlayerLikeEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.PlayerLikeEntity;
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
     * @param playerPredicate Predicate to determine which players this renderer should be used for.
     * @param constructor The renderer factory
     */
    <E extends PlayerLikeEntity & ClientPlayerLikeEntity, T extends PlayerEntityRenderer<E>> void registerPlayerRenderer(Identifier skinType, Predicate<? super E> playerPredicate, Function<EntityRendererFactory.Context, T> constructor);

    /**
     * Adds a custom player renderer.
     *
     * @param playerPredicate Predicate to determine which players this renderer should be used for.
     * @param constructor The renderer factory
     */
    <E extends PlayerLikeEntity & ClientPlayerLikeEntity, T extends PlayerEntityRenderer<E>> void registerPlayerStateRenderer(Identifier skinType, Predicate<PlayerEntityRenderState> statePredicate, Function<EntityRendererFactory.Context, T> constructor);

    /**
     * Adds a custom entity renderer.
     */
    <T extends Entity, R extends EntityRenderer<?, ?>> void registerEntityRenderer(EntityType<T> type, Function<EntityRendererFactory.Context, R> constructor);

    /**
     * Adds a custom entity renderer.
     *
     * @param type        Type type of entity
     * @param condition   Predicate to determine when to use this renderer.
     * @param constructor The renderer factory
     */
    <T extends Entity, R extends EntityRenderer<?, ?>> void registerEntityRenderer(EntityType<T> type, Predicate<? super T> condition, Function<EntityRendererFactory.Context, R> constructor);

    /**
     * Adds a custom entity renderer.
     *
     * @param type        Type type of entity
     * @param condition   Predicate to determine when to use this renderer.
     * @param constructor The renderer factory
     */
    <T extends Entity, R extends EntityRenderer<?, ?>> void registerEntityStateRenderer(EntityType<T> type, Predicate<EntityRenderState> condition, Function<EntityRendererFactory.Context, R> constructor);

    /**
     * Adds a custom block entity renderer.
     */
    <P extends BlockEntity, R extends BlockEntityRenderer<?, ?>> void registerBlockRenderer(BlockEntityType<P> type, Function<BlockEntityRendererFactory.Context, R> constructor);

    /**
     * Adds a custom block entity renderer.
     *
     * @param type        The type of block entity
     * @param condition   Predicate to determine when to use this renderer.
     * @param constructor The renderer factory
     */
    <P extends BlockEntity, R extends BlockEntityRenderer<?, ?>> void registerBlockRenderer(BlockEntityType<P> type, Predicate<? super P> condition, Function<BlockEntityRendererFactory.Context, R> constructor);

    /**
     * Adds a custom block entity renderer.
     *
     * @param type        The type of block entity
     * @param condition   Predicate to determine when to use this renderer.
     * @param constructor The renderer factory
     */
    <P extends BlockEntity, R extends BlockEntityRenderer<?, ?>> void registerBlockStateRenderer(BlockEntityType<P> type, Predicate<BlockEntityRenderState> condition, Function<BlockEntityRendererFactory.Context, R> constructor);
}
