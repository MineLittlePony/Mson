package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.resource.ResourceManager;

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

@Mixin(EntityRenderDispatcher.class)
abstract class MixinEntityRenderDispatcher implements EntityRendererRegistry {

    @Shadow
    private Map<EntityType<?>, EntityRenderer<? extends Entity>> renderers;

    @Shadow
    private Map<String, PlayerEntityRenderer> modelRenderers;

    @Shadow @Final
    private EntityModelLoader modelLoader;

    @Inject(method = "reload(Lnet/minecraft/resource/ResourceManager;)V", at = @At("RETURN"))
    private void onRegisterRenderers(ResourceManager manager, CallbackInfo info) {
        MsonImpl.instance().getEntityRendererRegistry().player.publish(this);
        MsonImpl.instance().getEntityRendererRegistry().entity.publish(this);
    }

    private EntityRendererFactory.Context createContext() {
        MinecraftClient mc = MinecraftClient.getInstance();
        EntityRenderDispatcher self = (EntityRenderDispatcher)(Object)this;
        return new EntityRendererFactory.Context(self, mc.getItemRenderer(), mc.getResourceManager(), modelLoader, mc.textRenderer);
    }

    @Override
    public <R extends PlayerEntityRenderer> void registerPlayerRenderer(String skinType, Function<EntityRendererFactory.Context, R> constructor) {
        try {
            if (modelRenderers instanceof ImmutableMap) {
                modelRenderers = new HashMap<>(modelRenderers);
            }
            modelRenderers.put(skinType, constructor.apply(createContext()));
        } catch (Exception e) {
            MsonImpl.LOGGER.error("Error whilst updating player renderer " + skinType + ": " + e.getMessage(), e);
        }
    }

    @Override
    public <T extends Entity, R extends EntityRenderer<?>> void registerEntityRenderer(EntityType<T> type, Function<EntityRendererFactory.Context, R> constructor) {
        try {
            if (renderers instanceof ImmutableMap) {
                renderers = new HashMap<>(renderers);
            }
            renderers.put(type, constructor.apply(createContext()));
        } catch (Exception e) {
            MsonImpl.LOGGER.error("Error whilst updating entity renderer " + EntityType.getId(type) + ": " + e.getMessage(), e);
        }
    }

}
