package com.minelittlepony.mson.impl;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class AppliedEntityRendererRegistry {
    private final Map<EntityType<?>, Map<Predicate<EntityRenderState>, EntityRenderer<? extends Entity, ?>>> customEntityStateRenderers = new HashMap<>();
    private final Map<Identifier, Map.Entry<Predicate<PlayerEntityRenderState>, PlayerEntityRenderer>> customStateRenderers = new HashMap<>();

    private final Map<EntityType<?>, Map<Predicate<Entity>, EntityRenderer<? extends Entity, ?>>> customEntityRenderers = new HashMap<>();
    private final Map<Identifier, Map.Entry<Predicate<AbstractClientPlayerEntity>, PlayerEntityRenderer>> customModelRenderers = new HashMap<>();

    public AppliedEntityRendererRegistry(Map<EntityType<?>, EntityRenderer<? extends Entity, ?>> renderers, EntityRendererFactory.Context context) {
        var pendingRegistrations = MsonImpl.INSTANCE.getEntityRendererRegistry();
        pendingRegistrations.player.publish((id, entry) -> {
            try {
                customModelRenderers.put(id, Map.entry(entry.getKey(), entry.getValue().apply(context)));
            } catch (Exception e) {
                MsonImpl.LOGGER.error("Error whilst adding player renderer with id " + id + ": " + e.getMessage(), e);
            }
        });
        pendingRegistrations.playerState.publish((id, entry) -> {
            try {
                customStateRenderers.put(id, Map.entry(entry.getKey(), entry.getValue().apply(context)));
            } catch (Exception e) {
                MsonImpl.LOGGER.error("Error whilst adding player renderer for render state with id " + id + ": " + e.getMessage(), e);
            }
        });
        pendingRegistrations.entity.publish((type, entry) -> {
            try {
                entry.getKey().ifLeft(unit -> {
                    renderers.put(type, entry.getValue().apply(context));
                }).ifRight(condition -> {
                    customEntityRenderers.computeIfAbsent(type, t -> new HashMap<>()).put(condition, entry.getValue().apply(context));
                });
            } catch (Exception e) {
                MsonImpl.LOGGER.error("Error whilst updating entity renderer with custom condition for entity type " + EntityType.getId(type) + ": " + e.getMessage(), e);
            }
        });
        pendingRegistrations.entityState.publish((type, entry) -> {
            try {
                customEntityStateRenderers.computeIfAbsent(type, t -> new HashMap<>()).put(entry.getKey(), entry.getValue().apply(context));
            } catch (Exception e) {
                MsonImpl.LOGGER.error("Error whilst updating entity renderer with custom condition for entity render state " + EntityType.getId(type) + ": " + e.getMessage(), e);
            }
        });
    }

    public Optional<EntityRenderer<?, ?>> getRenderer(Entity entity) {
        if (entity instanceof AbstractClientPlayerEntity player) {
            if (!customModelRenderers.isEmpty()) {
                return customModelRenderers.values().stream()
                    .filter(entry -> entry.getKey().test(player))
                    .findFirst()
                    .map(Map.Entry::getValue);
            }
        } else if (!customEntityRenderers.isEmpty()) {
            return customEntityRenderers.getOrDefault(entity.getType(), Map.of()).entrySet().stream()
                    .filter(entry -> entry.getKey().test(entity))
                    .findFirst()
                    .map(Map.Entry::getValue);
        }
        return Optional.empty();
    }

    public <S extends EntityRenderState> Optional<EntityRenderer<?, ?>> getRenderer(S state) {
        if (state instanceof PlayerEntityRenderState player) {
            if (!customStateRenderers.isEmpty()) {
                return customStateRenderers.values().stream()
                    .filter(entry -> entry.getKey().test(player))
                    .findFirst()
                    .map(Map.Entry::getValue);
            }
        } else if (!customEntityStateRenderers.isEmpty() && state.entityType != null) {
            return customEntityStateRenderers.getOrDefault(state.entityType, Map.of()).entrySet().stream()
                .filter(entry -> entry.getKey().test(state))
                .findFirst()
                .map(Map.Entry::getValue);
        }
        return Optional.empty();
    }
}
