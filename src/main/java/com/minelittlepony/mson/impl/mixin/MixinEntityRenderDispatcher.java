package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableMap;
import com.minelittlepony.mson.api.EntityRendererRegistry;
import com.minelittlepony.mson.impl.MsonImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(EntityRenderDispatcher.class)
abstract class MixinEntityRenderDispatcher implements EntityRendererRegistry {
    @Shadow
    private Map<EntityType<?>, EntityRenderer<? extends Entity>> renderers;
    private Map<Identifier, Map.Entry<Predicate<AbstractClientPlayerEntity>, PlayerEntityRenderer>> customModelRenderers;
    @Shadow
    private @Final EntityModelLoader modelLoader;

    @Inject(method = "reload(Lnet/minecraft/resource/ResourceManager;)V", at = @At("RETURN"))
    private void onRegisterRenderers(ResourceManager manager, CallbackInfo info) {
        MsonImpl.INSTANCE.getEntityRendererRegistry().player.publish(this);
        MsonImpl.INSTANCE.getEntityRendererRegistry().entity.publish(this);
    }

    private EntityRendererFactory.Context createContext() {
        MinecraftClient mc = MinecraftClient.getInstance();
        EntityRenderDispatcher self = (EntityRenderDispatcher)(Object)this;
        return new EntityRendererFactory.Context(self,
                mc.getItemRenderer(),
                mc.getBlockRenderManager(),
                mc.getEntityRenderDispatcher().getHeldItemRenderer(), mc.getResourceManager(),
                modelLoader,
                mc.textRenderer
        );
    }

    @Override
    public <R extends PlayerEntityRenderer> void registerPlayerRenderer(Identifier id, Predicate<AbstractClientPlayerEntity> playerPredicate, Function<EntityRendererFactory.Context, R> constructor) {
        try {
            if (customModelRenderers == null) {
                customModelRenderers = new HashMap<>();
            }
            customModelRenderers.put(id, Map.entry(playerPredicate, constructor.apply(createContext())));
        } catch (Exception e) {
            MsonImpl.LOGGER.error("Error whilst updating adding player renderer with id " + id + ": " + e.getMessage(), e);
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

    @Inject(
            method = "getRenderer(Lnet/minecraft/entity/Entity;)Lnet/minecraft/client/render/entity/EntityRenderer;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onGetRenderer(Entity entity, CallbackInfoReturnable<EntityRenderer<?>> info) {
        if (entity instanceof AbstractClientPlayerEntity player) {
            customModelRenderers.values().stream()
                .filter(entry -> entry.getKey().test(player))
                .findFirst()
                .map(Map.Entry::getValue)
                .ifPresent(info::setReturnValue);
        }
    }
}
