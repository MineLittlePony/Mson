package com.minelittlepony.mson.impl.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableMap;
import com.minelittlepony.mson.api.EntityRendererRegistry;
import com.minelittlepony.mson.impl.MsonImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(BlockEntityRenderDispatcher.class)
abstract class MixinBlockEntityRenderDispatcher implements EntityRendererRegistry {
    @Shadow
    private Map<BlockEntityType<?>, BlockEntityRenderer<?>> renderers;
    @Shadow
    private @Final TextRenderer textRenderer;
    @Shadow
    private @Final EntityModelLoader entityModelLoader;
    @Shadow
    private @Final Supplier<BlockRenderManager> blockRenderManager;

    @Inject(method = "reload(Lnet/minecraft/resource/ResourceManager;)V", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        MsonImpl.INSTANCE.getEntityRendererRegistry().block.publish(this);
    }

    @Override
    public <P extends BlockEntity, R extends BlockEntityRenderer<?>> void registerBlockRenderer(BlockEntityType<P> type, Function<BlockEntityRendererFactory.Context, R> constructor) {
        try {
            BlockEntityRendererFactory.Context context = new BlockEntityRendererFactory.Context((BlockEntityRenderDispatcher)(Object)this, blockRenderManager.get(), entityModelLoader, textRenderer);
            if (renderers instanceof ImmutableMap) {
                renderers = new HashMap<>(renderers);
            }
            renderers.put(type, constructor.apply(context));
        } catch (Exception e) {
            MsonImpl.LOGGER.error("Error whilst updating entity renderer " + BlockEntityType.getId(type) + ": " + e.getMessage());
        }
    }

}
