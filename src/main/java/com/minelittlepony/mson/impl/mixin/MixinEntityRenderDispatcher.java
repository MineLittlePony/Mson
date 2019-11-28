package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import com.minelittlepony.mson.api.EntityRendererRegistry;
import com.minelittlepony.mson.impl.MsonImpl;

import java.util.Map;
import java.util.function.Function;

@Mixin(EntityRenderDispatcher.class)
class MixinEntityRenderDispatcher implements EntityRendererRegistry {

    @Shadow @Final
    private Map<EntityType<?>, EntityRenderer<? extends Entity>> renderers;

    @Shadow @Final
    private Map<String, PlayerEntityRenderer> modelRenderers;

    @Shadow @Final @Mutable
    private PlayerEntityRenderer playerRenderer;

    @Override
    public <R extends PlayerEntityRenderer> void registerPlayerRenderer(String skinType, Function<EntityRenderDispatcher, R> constructor) {
        try {
            R renderer = constructor.apply((EntityRenderDispatcher)(Object)this);

            if ("default".equalsIgnoreCase(skinType)) {
                playerRenderer = renderer;
            }
            modelRenderers.put(skinType, renderer);
        } catch (Exception e) {
            MsonImpl.LOGGER.error("Error whilst updating player renderer " + skinType + ": " + e.getMessage());
        }
    }

    @Override
    public <T extends Entity, R extends EntityRenderer<? extends Entity>> void registerEntityRenderer(EntityType<T> type, Function<EntityRenderDispatcher, R> constructor) {
        try {
            renderers.put(type, constructor.apply((EntityRenderDispatcher)(Object)this));
        } catch (Exception e) {
            MsonImpl.LOGGER.error("Error whilst updating entity renderer " + EntityType.getId(type) + ": " + e.getMessage());
        }
    }
}
