package com.minelittlepony.mson.impl;


import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class AppliedBlockEntityRendererRegistry {

    private final Map<BlockEntityType<?>, Map<Predicate<BlockEntity>, BlockEntityRenderer<? extends BlockEntity, ?>>> customBlockEntityRenderers = new HashMap<>();
    private final Map<BlockEntityType<?>, Map<Predicate<BlockEntityRenderState>, BlockEntityRenderer<? extends BlockEntity, ?>>> customBlockEntityStateRenderers = new HashMap<>();

    @SuppressWarnings("deprecation")
    public AppliedBlockEntityRendererRegistry(Map<BlockEntityType<?>, BlockEntityRenderer<?, ?>> renderers, BlockEntityRendererProvider.Context context) {
        MsonImpl.INSTANCE.getEntityRendererRegistry().block.publish((type, entry) -> {
            try {
                entry.getKey().ifLeft(_ -> {
                    renderers.put(type, entry.getValue().apply(context));
                }).ifRight(predicate -> {
                    customBlockEntityRenderers.computeIfAbsent(type, _ -> new HashMap<>()).put(predicate, entry.getValue().apply(context));
                });

            } catch (Exception e) {
                MsonImpl.LOGGER.error("Error whilst updating block entity renderer " + type.builtInRegistryHolder().getRegisteredName() + ": " + e.getMessage());
            }
        });
        MsonImpl.INSTANCE.getEntityRendererRegistry().blockState.publish((type, entry) -> {
            try {
                customBlockEntityStateRenderers.computeIfAbsent(type, _ -> new HashMap<>()).put(entry.getKey(), entry.getValue().apply(context));
            } catch (Exception e) {
                MsonImpl.LOGGER.error("Error whilst updating block entity renderer " + type.builtInRegistryHolder().getRegisteredName() + ": " + e.getMessage());
            }
        });
    }

    public Optional<BlockEntityRenderer<?, ?>> getRenderer(BlockEntity entity) {
        if (!customBlockEntityRenderers.isEmpty()) {
            return customBlockEntityRenderers.getOrDefault(entity.getType(), Map.of()).entrySet().stream()
                    .filter(entry -> entry.getKey().test(entity))
                    .findFirst()
                    .map(Map.Entry::getValue);
        }
        return Optional.empty();
    }

    public <S extends BlockEntityRenderState> Optional<BlockEntityRenderer<?, ?>> getRenderer(S state) {
        if (!customBlockEntityStateRenderers.isEmpty() && state.blockEntityType != null) {
            return customBlockEntityStateRenderers.getOrDefault(state.blockEntityType, Map.of()).entrySet().stream()
                .filter(entry -> entry.getKey().test(state))
                .findFirst()
                .map(Map.Entry::getValue);
        }
        return Optional.empty();
    }
}
