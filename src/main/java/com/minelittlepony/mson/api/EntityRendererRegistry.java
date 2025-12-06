package com.minelittlepony.mson.api;


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
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

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
    <E extends Avatar & ClientAvatarEntity, T extends AvatarRenderer<E>> void registerPlayerRenderer(Identifier skinType, Predicate<? super E> playerPredicate, Function<EntityRendererProvider.Context, T> constructor);

    /**
     * Adds a custom player renderer.
     *
     * @param playerPredicate Predicate to determine which players this renderer should be used for.
     * @param constructor The renderer factory
     */
    <E extends Avatar & ClientAvatarEntity, T extends AvatarRenderer<E>> void registerPlayerStateRenderer(Identifier skinType, Predicate<AvatarRenderState> statePredicate, Function<EntityRendererProvider.Context, T> constructor);

    /**
     * Adds a custom entity renderer.
     */
    <T extends Entity, R extends EntityRenderer<?, ?>> void registerEntityRenderer(EntityType<T> type, Function<EntityRendererProvider.Context, R> constructor);

    /**
     * Adds a custom entity renderer.
     *
     * @param type        Type type of entity
     * @param condition   Predicate to determine when to use this renderer.
     * @param constructor The renderer factory
     */
    <T extends Entity, R extends EntityRenderer<?, ?>> void registerEntityRenderer(EntityType<T> type, Predicate<? super T> condition, Function<EntityRendererProvider.Context, R> constructor);

    /**
     * Adds a custom entity renderer.
     *
     * @param type        Type type of entity
     * @param condition   Predicate to determine when to use this renderer.
     * @param constructor The renderer factory
     */
    <T extends Entity, R extends EntityRenderer<?, ?>> void registerEntityStateRenderer(EntityType<T> type, Predicate<EntityRenderState> condition, Function<EntityRendererProvider.Context, R> constructor);

    /**
     * Adds a custom block entity renderer.
     */
    <P extends BlockEntity, R extends BlockEntityRenderer<?, ?>> void registerBlockRenderer(BlockEntityType<P> type, Function<BlockEntityRendererProvider.Context, R> constructor);

    /**
     * Adds a custom block entity renderer.
     *
     * @param type        The type of block entity
     * @param condition   Predicate to determine when to use this renderer.
     * @param constructor The renderer factory
     */
    <P extends BlockEntity, R extends BlockEntityRenderer<?, ?>> void registerBlockRenderer(BlockEntityType<P> type, Predicate<? super P> condition, Function<BlockEntityRendererProvider.Context, R> constructor);

    /**
     * Adds a custom block entity renderer.
     *
     * @param type        The type of block entity
     * @param condition   Predicate to determine when to use this renderer.
     * @param constructor The renderer factory
     */
    <P extends BlockEntity, R extends BlockEntityRenderer<?, ?>> void registerBlockStateRenderer(BlockEntityType<P> type, Predicate<BlockEntityRenderState> condition, Function<BlockEntityRendererProvider.Context, R> constructor);
}
