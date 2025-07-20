package com.minelittlepony.mson.impl.mixin;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.item.ItemRenderer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.mson.impl.AppliedBlockEntityRendererRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(BlockEntityRenderDispatcher.class)
abstract class MixinBlockEntityRenderDispatcher {
    @Shadow
    private Map<BlockEntityType<?>, BlockEntityRenderer<?>> renderers;
    @Shadow
    private @Final TextRenderer textRenderer;
    @Shadow
    private @Final Supplier<LoadedEntityModels> entityModelsGetter;
    @Shadow
    private @Final BlockRenderManager blockRenderManager;
    @Shadow
    private @Final ItemModelManager itemModelManager;
    @Shadow
    private @Final ItemRenderer itemRenderer;
    @Shadow
    private @Final EntityRenderDispatcher entityRenderDispatcher;

    @Inject(method = "reload(Lnet/minecraft/resource/ResourceManager;)V", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        renderers = new HashMap<>(renderers);
        AppliedBlockEntityRendererRegistry.reload(renderers, new BlockEntityRendererFactory.Context(
                (BlockEntityRenderDispatcher)(Object)this,
                blockRenderManager,
                itemModelManager,
                itemRenderer,
                entityRenderDispatcher,
                entityModelsGetter.get(),
                textRenderer
        ));
    }
}
