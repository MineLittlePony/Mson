package com.minelittlepony.mson.impl.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.minelittlepony.mson.api.EntityRendererRegistry;
import com.minelittlepony.mson.impl.MsonImpl;

import java.util.Map;
import java.util.function.Function;

@Mixin(BlockEntityRenderDispatcher.class)
abstract class MixinBlockEntityRenderDispatcher implements EntityRendererRegistry {

    @Shadow @Final
    private Map<BlockEntityType<?>, BlockEntityRenderer<?>> renderers;

    @Override
    public <P extends BlockEntity, R extends BlockEntityRenderer<?>> void registerBlockRenderer(BlockEntityType<P> type, Function<BlockEntityRenderDispatcher, R> constructor) {
        try {
            renderers.put(type, constructor.apply((BlockEntityRenderDispatcher)(Object)this));
        } catch (Exception e) {
            MsonImpl.LOGGER.error("Error whilst updating entity renderer " + BlockEntityType.getId(type) + ": " + e.getMessage());
        }
    }
}
