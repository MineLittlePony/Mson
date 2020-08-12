package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.resource.ReloadableResourceManager;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.mson.api.EntityRendererRegistry;
import com.minelittlepony.mson.impl.MsonImpl;

import java.util.Map;
import java.util.function.Function;

@Mixin(EntityRenderDispatcher.class)
abstract class MixinEntityRenderDispatcher implements EntityRendererRegistry {

    @Shadow @Final
    private Map<EntityType<?>, EntityRenderer<? extends Entity>> renderers;

    @Shadow @Final
    private Map<String, PlayerEntityRenderer> modelRenderers;

    @Shadow @Final @Mutable
    private PlayerEntityRenderer playerRenderer;

    @Inject(method = "registerRenderers(Lnet/minecraft/client/render/item/ItemRenderer;Lnet/minecraft/resource/ReloadableResourceManager;)V", at = @At("RETURN"))
    private void onRegisterRenderers(ItemRenderer itemRenderer, ReloadableResourceManager manager, CallbackInfo info) {
        MsonImpl.instance().getEntityRendererRegistry().player.publish(this);
        MsonImpl.instance().getEntityRendererRegistry().entity.publish(this);
    }

    @Override
    public <R extends PlayerEntityRenderer> void registerPlayerRenderer(String skinType, Function<EntityRenderDispatcher, R> constructor) {
        try {
            R renderer = constructor.apply((EntityRenderDispatcher)(Object)this);

            if ("default".equalsIgnoreCase(skinType)) {
                playerRenderer = renderer;
            }
            modelRenderers.put(skinType, renderer);
        } catch (Exception e) {
            MsonImpl.LOGGER.error("Error whilst updating player renderer " + skinType + ": " + e.getMessage(), e);
        }
    }

    @Override
    public <T extends Entity, R extends EntityRenderer<?>> void registerEntityRenderer(EntityType<T> type, Function<EntityRenderDispatcher, R> constructor) {
        try {
            renderers.put(type, constructor.apply((EntityRenderDispatcher)(Object)this));
        } catch (Exception e) {
            MsonImpl.LOGGER.error("Error whilst updating entity renderer " + EntityType.getId(type) + ": " + e.getMessage(), e);
        }
    }

}
