package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

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

@Mixin(BlockEntityRenderDispatcher.class)
abstract class MixinBlockEntityRenderDispatcher {
    @Shadow
    private Map<BlockEntityType<?>, BlockEntityRenderer<?, ?>> renderers;
    @Shadow
    private @Final Font font;
    @Shadow
    private @Final Supplier<EntityModelSet> entityModelSet;
    @Shadow
    private @Final BlockRenderDispatcher blockRenderDispatcher;
    @Shadow
    private @Final ItemModelResolver itemModelResolver;
    @Shadow
    private @Final ItemRenderer itemRenderer;
    @Shadow
    private @Final EntityRenderDispatcher entityRenderer;
    @Shadow
    private @Final MaterialSet materials;
    @Shadow
    private @Final PlayerSkinRenderCache playerSkinRenderCache;

    @Nullable
    private AppliedBlockEntityRendererRegistry mson_registry;

    @Inject(method = "reload(Lnet/minecraft/resource/ResourceManager;)V", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        renderers = new HashMap<>(renderers);
        mson_registry = new AppliedBlockEntityRendererRegistry(renderers, new BlockEntityRendererProvider.Context(
                (BlockEntityRenderDispatcher)(Object)this,
                blockRenderDispatcher,
                itemModelResolver,
                itemRenderer,
                entityRenderer,
                entityModelSet.get(),
                font,
                materials,
                playerSkinRenderCache
        ));
    }

    @Inject(
            method = "getRenderer(Lnet/minecraft/world/level/block/entity/BlockEntity;)Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onGet(BlockEntity entity, CallbackInfoReturnable<BlockEntityRenderer<?, ?>> info) {
        if (mson_registry != null) {
            mson_registry.getRenderer(entity).ifPresent(info::setReturnValue);
        }
    }

    @Inject(
            method = "getRenderer(Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;)Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;",
            at = @At("HEAD"),
            cancellable = true
    )
    private <E extends BlockEntity, S extends BlockEntityRenderState> void onGetByRenderState(BlockEntityRenderState state, CallbackInfoReturnable<BlockEntityRenderer<?, ?>> info) {
        if (mson_registry != null) {
            mson_registry.getRenderer(state).ifPresent(info::setReturnValue);
        }
    }
}
