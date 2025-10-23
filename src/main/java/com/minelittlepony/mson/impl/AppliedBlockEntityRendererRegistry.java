package com.minelittlepony.mson.impl;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class AppliedBlockEntityRendererRegistry {

    private final Map<BlockEntityType<?>, Map<Predicate<BlockEntity>, BlockEntityRenderer<? extends BlockEntity, ?>>> customBlockEntityRenderers = new HashMap<>();
    private final Map<BlockEntityType<?>, Map<Predicate<BlockEntityRenderState>, BlockEntityRenderer<? extends BlockEntity, ?>>> customBlockEntityStateRenderers = new HashMap<>();

    public AppliedBlockEntityRendererRegistry(Map<BlockEntityType<?>, BlockEntityRenderer<?, ?>> renderers, BlockEntityRendererFactory.Context context) {

        MsonImpl.INSTANCE.getEntityRendererRegistry().block.publish((type, entry) -> {
            try {
                entry.getKey().ifLeft(left -> {
                    renderers.put(type, entry.getValue().apply(context));
                }).ifRight(predicate -> {
                    customBlockEntityRenderers.computeIfAbsent(type, t -> new HashMap<>()).put(predicate, entry.getValue().apply(context));
                });

            } catch (Exception e) {
                MsonImpl.LOGGER.error("Error whilst updating block entity renderer " + BlockEntityType.getId(type) + ": " + e.getMessage());
            }
        });
        MsonImpl.INSTANCE.getEntityRendererRegistry().blockState.publish((type, entry) -> {
            try {
                customBlockEntityStateRenderers.computeIfAbsent(type, t -> new HashMap<>()).put(entry.getKey(), entry.getValue().apply(context));
            } catch (Exception e) {
                MsonImpl.LOGGER.error("Error whilst updating block entity renderer " + BlockEntityType.getId(type) + ": " + e.getMessage());
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
        if (!customBlockEntityStateRenderers.isEmpty() && state.type != null) {
            return customBlockEntityStateRenderers.getOrDefault(state.type, Map.of()).entrySet().stream()
                .filter(entry -> entry.getKey().test(state))
                .findFirst()
                .map(Map.Entry::getValue);
        }
        return Optional.empty();
    }
}
