package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.network.ClientPlayerLikeEntity;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.equipment.EquipmentModelLoader;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.texture.AtlasManager;
import net.minecraft.client.texture.PlayerSkinCache;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.resource.ResourceManager;

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

@Mixin(EntityRenderManager.class)
abstract class MixinEntityRenderDispatcher {
    @Shadow
    private Map<EntityType<?>, EntityRenderer<? extends Entity, ?>> renderers;

    @Shadow
    private @Final ItemModelManager itemModelManager;
    @Shadow
    private @Final MapRenderer mapRenderer;
    @Shadow
    private @Final BlockRenderManager blockRenderManager;
    @Shadow
    private @Final AtlasManager atlasManager;
    @Shadow
    private @Final TextRenderer textRenderer;
    @Shadow
    private @Final Supplier<LoadedEntityModels> entityModelsGetter;
    @Shadow
    private @Final EquipmentModelLoader equipmentModelLoader;
    @Shadow
    private @Final PlayerSkinCache skinCache;

    @Nullable
    private AppliedEntityRendererRegistry mson_registry;

    @Inject(method = "reload(Lnet/minecraft/resource/ResourceManager;)V", at = @At("RETURN"))
    private void onRegisterRenderers(ResourceManager manager, CallbackInfo info) {
        renderers = new HashMap<>(renderers);
        mson_registry = new AppliedEntityRendererRegistry(renderers, new EntityRendererFactory.Context(
                (EntityRenderManager)(Object)this,
                itemModelManager,
                mapRenderer,
                blockRenderManager,
                manager,
                entityModelsGetter.get(),
                equipmentModelLoader,
                atlasManager,
                textRenderer,
                skinCache
        ));
    }

    @Inject(
            method = "getRenderer(Lnet/minecraft/entity/Entity;)Lnet/minecraft/client/render/entity/EntityRenderer;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onGetRenderer(Entity entity, CallbackInfoReturnable<EntityRenderer<?, ?>> info) {
        if (mson_registry != null) {
            mson_registry.getRenderer(entity).ifPresent(info::setReturnValue);
        }
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "getPlayerRenderer(Ljava/util/Map;Lnet/minecraft/entity/PlayerLikeEntity;)Lnet/minecraft/client/render/entity/PlayerEntityRenderer;",
            at = @At("HEAD"),
            cancellable = true)
    private <T extends PlayerLikeEntity & ClientPlayerLikeEntity> void onGetPlayerRenderer(
            Map<PlayerSkinType, PlayerEntityRenderer<T>> skinTypeToRenderer, T player,
            CallbackInfoReturnable<PlayerEntityRenderer<T>> info
        ) {
        mson_registry.getRenderer(player).ifPresent(renderer -> {
            if (renderer instanceof PlayerEntityRenderer p) {
                info.setReturnValue(p);
            }
        });
    }
    @Inject(
            method = "getRenderer(Lnet/minecraft/client/render/entity/state/EntityRenderState;)Lnet/minecraft/client/render/entity/EntityRenderer;",
            at = @At("HEAD"),
            cancellable = true
    )
    private <S extends EntityRenderState> void onGetRenderer(S state, CallbackInfoReturnable<EntityRenderer<?, ?>> info) {
        if (mson_registry != null) {
            mson_registry.getRenderer(state).ifPresent(info::setReturnValue);
        }
    }
}
