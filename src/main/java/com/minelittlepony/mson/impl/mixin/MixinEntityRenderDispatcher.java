package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.mson.impl.AppliedEntityRendererRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(EntityRenderDispatcher.class)
abstract class MixinEntityRenderDispatcher {
    @Shadow
    private Map<EntityType<?>, EntityRenderer<? extends Entity, ?>> renderers;

    @Shadow
    private @Final ItemModelResolver itemModelResolver;
    @Shadow
    private @Final MapRenderer mapRenderer;
    @Shadow
    private @Final BlockRenderDispatcher blockRenderDispatcher;
    @Shadow
    private @Final AtlasManager atlasManager;
    @Shadow
    private @Final Font font;
    @Shadow
    private @Final Supplier<EntityModelSet> entityModels;
    @Shadow
    private @Final EquipmentAssetManager equipmentAssets;
    @Shadow
    private @Final PlayerSkinRenderCache playerSkinRenderCache;

    @Nullable
    private AppliedEntityRendererRegistry mson_registry;

    @Inject(method = "reload(Lnet/minecraft/resource/ResourceManager;)V", at = @At("RETURN"))
    private void onRegisterRenderers(ResourceManager manager, CallbackInfo info) {
        renderers = new HashMap<>(renderers);
        mson_registry = new AppliedEntityRendererRegistry(renderers, new EntityRendererProvider.Context(
                (EntityRenderDispatcher)(Object)this,
                itemModelResolver,
                mapRenderer,
                blockRenderDispatcher,
                manager,
                entityModels.get(),
                equipmentAssets,
                atlasManager,
                font,
                playerSkinRenderCache
        ));
    }

    @Inject(
            method = "getRenderer(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/client/renderer/entity/EntityRenderer;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onGetRenderer(Entity entity, CallbackInfoReturnable<EntityRenderer<?, ?>> info) {
        if (mson_registry != null) {
            mson_registry.getRenderer(entity).ifPresent(info::setReturnValue);
        }
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "getAvatarRenderer(Ljava/util/Map;Lnet/minecraft/world/entity/Avatar;)Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;",
            at = @At("HEAD"),
            cancellable = true)
    private <T extends Avatar & ClientAvatarEntity> void onGetPlayerRenderer(
            Map<PlayerModelType, AvatarRenderer<T>> skinTypeToRenderer, T player,
            CallbackInfoReturnable<AvatarRenderer<T>> info
        ) {
        mson_registry.getRenderer(player).ifPresent(renderer -> {
            if (renderer instanceof AvatarRenderer p) {
                info.setReturnValue(p);
            }
        });
    }

    @Inject(
            method = "getRenderer(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)Lnet/minecraft/client/renderer/entity/EntityRenderer;",
            at = @At("HEAD"),
            cancellable = true
    )
    private <S extends EntityRenderState> void onGetRenderer(S state, CallbackInfoReturnable<EntityRenderer<?, ?>> info) {
        if (mson_registry != null) {
            mson_registry.getRenderer(state).ifPresent(info::setReturnValue);
        }
    }
}
