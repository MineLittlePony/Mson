package com.minelittlepony.mson.impl.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.PlayerSkinCache;
import net.minecraft.client.texture.SpriteHolder;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.mson.impl.AppliedBlockEntityRendererRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(BlockEntityRenderManager.class)
abstract class MixinBlockEntityRenderDispatcher {
    @Shadow
    private Map<BlockEntityType<?>, BlockEntityRenderer<?, ?>> renderers;
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
    private @Final EntityRenderManager entityRenderDispatcher;
    @Shadow
    private @Final SpriteHolder spriteHolder;
    @Shadow
    private @Final PlayerSkinCache playerSkinCache;

    @Nullable
    private AppliedBlockEntityRendererRegistry mson_registry;

    @Inject(method = "reload(Lnet/minecraft/resource/ResourceManager;)V", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        renderers = new HashMap<>(renderers);
        mson_registry = new AppliedBlockEntityRendererRegistry(renderers, new BlockEntityRendererFactory.Context(
                (BlockEntityRenderManager)(Object)this,
                blockRenderManager,
                itemModelManager,
                itemRenderer,
                entityRenderDispatcher,
                entityModelsGetter.get(),
                textRenderer,
                spriteHolder,
                playerSkinCache
        ));
    }

    @Inject(
            method = "get(Lnet/minecraft/block/entity/BlockEntity;)Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onGet(BlockEntity entity, CallbackInfoReturnable<BlockEntityRenderer<?, ?>> info) {
        if (mson_registry != null) {
            mson_registry.getRenderer(entity).ifPresent(info::setReturnValue);
        }
    }

    @Inject(
            method = "getByRenderState(Lnet/minecraft/client/render/block/entity/state/BlockEntityRenderState;)Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;",
            at = @At("HEAD"),
            cancellable = true
    )
    private <E extends BlockEntity, S extends BlockEntityRenderState> void onGetByRenderState(BlockEntityRenderState state, CallbackInfoReturnable<BlockEntityRenderer<?, ?>> info) {
        if (mson_registry != null) {
            mson_registry.getRenderer(state).ifPresent(info::setReturnValue);
        }
    }
}
