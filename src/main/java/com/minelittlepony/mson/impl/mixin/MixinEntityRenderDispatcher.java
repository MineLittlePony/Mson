package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import com.minelittlepony.mson.api.EntityRendererRegistry;

import java.util.Map;
import java.util.function.Function;

@Mixin(EntityRenderDispatcher.class)
class MixinEntityRenderDispatcher implements EntityRendererRegistry {

    @Shadow @Final
    private Map<Class<? extends Entity>, EntityRenderer<? extends Entity>> renderers;

    @Shadow @Final
    private Map<String, PlayerEntityRenderer> modelRenderers;

    @Shadow @Final @Mutable
    private PlayerEntityRenderer playerRenderer;

    @Override
    public <R extends PlayerEntityRenderer> void registerPlayerRenderer(String skinType, Function<EntityRenderDispatcher, R> constructor) {

        R renderer = constructor.apply((EntityRenderDispatcher)(Object)this);

        if ("default".equalsIgnoreCase(skinType)) {
            playerRenderer = renderer;
        }
        modelRenderers.put(skinType, renderer);
    }

    @Override
    public <T extends Entity, R extends EntityRenderer<? super T>> void registerEntityRenderer(Class<T> type, Function<EntityRenderDispatcher, R> constructor) {
        renderers.put(type, constructor.apply((EntityRenderDispatcher)(Object)this));
    }

}
