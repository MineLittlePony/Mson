package com.minelittlepony.mson.impl;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;

import java.util.Map;

public class AppliedBlockEntityRendererRegistry {
    public static void reload(Map<BlockEntityType<?>, BlockEntityRenderer<?, ?>> renderers, BlockEntityRendererFactory.Context context) {
        MsonImpl.INSTANCE.getEntityRendererRegistry().block.publish((type, constructor) -> {
            try {
                renderers.put(type, constructor.apply(context));
            } catch (Exception e) {
                MsonImpl.LOGGER.error("Error whilst updating block entity renderer " + BlockEntityType.getId(type) + ": " + e.getMessage());
            }
        });
    }
}
