package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.equipment.EquipmentModelLoader;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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

@Mixin(EntityRenderDispatcher.class)
abstract class MixinEntityRenderDispatcher {
    @Shadow
    private Map<EntityType<?>, EntityRenderer<? extends Entity, ?>> renderers;

    @Shadow
    private @Final Supplier<LoadedEntityModels> entityModelsGetter;
    @Shadow
    private @Final EquipmentModelLoader equipmentModelLoader;

    @Nullable
    private AppliedEntityRendererRegistry mson_registry;

    @Inject(method = "reload(Lnet/minecraft/resource/ResourceManager;)V", at = @At("RETURN"))
    private void onRegisterRenderers(ResourceManager manager, CallbackInfo info) {
        MinecraftClient mc = MinecraftClient.getInstance();
        renderers = new HashMap<>(renderers);
        mson_registry = new AppliedEntityRendererRegistry(renderers, new EntityRendererFactory.Context(
                (EntityRenderDispatcher)(Object)this,
                mc.getItemModelManager(),
                mc.getMapRenderer(),
                mc.getBlockRenderManager(),
                mc.getResourceManager(),
                entityModelsGetter.get(),
                equipmentModelLoader,
                mc.textRenderer
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
