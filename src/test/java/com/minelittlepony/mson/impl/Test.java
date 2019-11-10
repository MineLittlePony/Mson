package com.minelittlepony.mson.impl;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.CreeperEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PigEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.util.Identifier;

import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.Mson;
import com.minelittlepony.mson.api.model.entities.MsonCreeper;
import com.minelittlepony.mson.api.model.entities.MsonPig;
import com.minelittlepony.mson.api.model.entities.MsonPlayer;

public final class Test {

    public static void init() {
        MsonImpl.DEBUG = true;
        ModelKey<MsonPlayer<AbstractClientPlayerEntity>> STEVE = Mson.getInstance().registerModel(new Identifier("mson", "steve"), MsonPlayer::new);
        ModelKey<MsonPlayer<AbstractClientPlayerEntity>> ALEX = Mson.getInstance().registerModel(new Identifier("mson", "alex"), MsonPlayer::new);
        ModelKey<MsonCreeper<CreeperEntity>> CREEPER = Mson.getInstance().registerModel(new Identifier("mson", "creeper"), MsonCreeper::new);
        ModelKey<MsonPig<PigEntity>> PIG = Mson.getInstance().registerModel(new Identifier("mson", "pig"), MsonPig::new);
        MsonImpl.DEBUG = false;

        Mson.getInstance().getEntityRendererRegistry().registerPlayerRenderer("default", r -> new Renderer(r, STEVE));
        Mson.getInstance().getEntityRendererRegistry().registerPlayerRenderer("slim", r -> new Renderer(r, ALEX));
        Mson.getInstance().getEntityRendererRegistry().registerEntityRenderer(CreeperEntity.class, r -> new Creeper(r, CREEPER));
        Mson.getInstance().getEntityRendererRegistry().registerEntityRenderer(PigEntity.class, r -> new Pig(r, PIG));
    }

    static class Renderer extends PlayerEntityRenderer {
        Renderer(EntityRenderDispatcher dispatcher, ModelKey<MsonPlayer<AbstractClientPlayerEntity>> key) {
            super(dispatcher);
            this.model = key.createModel();
        }
    }
    static class Creeper extends CreeperEntityRenderer {
        public Creeper(EntityRenderDispatcher dispatcher, ModelKey<MsonCreeper<CreeperEntity>> key) {
            super(dispatcher);
            this.model = key.createModel();
        }
    }
    static class Pig extends PigEntityRenderer {
        public Pig(EntityRenderDispatcher dispatcher, ModelKey<MsonPig<PigEntity>> key) {
            super(dispatcher);
            this.model = key.createModel();
        }
    }
}
