package com.minelittlepony.mson.impl;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.util.Identifier;

import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.Mson;
import com.minelittlepony.mson.api.model.biped.MsonCreeper;
import com.minelittlepony.mson.api.model.biped.MsonEnderman;
import com.minelittlepony.mson.api.model.biped.MsonPlayer;
import com.minelittlepony.mson.api.model.quadruped.MsonCow;
import com.minelittlepony.mson.api.model.quadruped.MsonPig;
import com.minelittlepony.mson.api.model.quadruped.MsonSheep;

public final class Test {

    public static void init() {
        MsonImpl.DEBUG = true;
        ModelKey<MsonPlayer<AbstractClientPlayerEntity>> STEVE = Mson.getInstance().registerModel(new Identifier("mson", "steve"), MsonPlayer::new);
        ModelKey<MsonPlayer<AbstractClientPlayerEntity>> ALEX = Mson.getInstance().registerModel(new Identifier("mson", "rayman"), MsonPlayer::new);
        ModelKey<MsonCreeper<CreeperEntity>> CREEPER = Mson.getInstance().registerModel(new Identifier("mson", "creeper"), MsonCreeper::new);
        ModelKey<MsonPig<PigEntity>> PIG = Mson.getInstance().registerModel(new Identifier("mson", "pig"), MsonPig::new);
        ModelKey<MsonCow<CowEntity>> COW = Mson.getInstance().registerModel(new Identifier("mson", "cow"), MsonCow::new);
        ModelKey<MsonSheep<SheepEntity>> SHEEP = Mson.getInstance().registerModel(new Identifier("mson", "sheep"), MsonSheep::new);
        ModelKey<MsonSheep<SheepEntity>> SHEEP_WOOL = Mson.getInstance().registerModel(new Identifier("mson", "sheep_wool"), MsonSheep::new);
        ModelKey<MsonEnderman<EndermanEntity>> ENDERMAN = Mson.getInstance().registerModel(new Identifier("mson", "enderman"), MsonEnderman::new);
        MsonImpl.DEBUG = false;

        Mson.getInstance().getEntityRendererRegistry().registerPlayerRenderer("default", r -> new MsonPlayer.Renderer(r, STEVE));
        Mson.getInstance().getEntityRendererRegistry().registerPlayerRenderer("slim", r -> new MsonPlayer.Renderer(r, ALEX));
        Mson.getInstance().getEntityRendererRegistry().registerEntityRenderer(EntityType.CREEPER, r -> new MsonCreeper.Renderer(r, CREEPER));
        Mson.getInstance().getEntityRendererRegistry().registerEntityRenderer(EntityType.PIG, r -> new MsonPig.Renderer(r, PIG));
        Mson.getInstance().getEntityRendererRegistry().registerEntityRenderer(EntityType.COW, r -> new MsonCow.Renderer(r, COW));
        Mson.getInstance().getEntityRendererRegistry().registerEntityRenderer(EntityType.SHEEP, r -> new MsonSheep.Renderer(r, SHEEP, SHEEP_WOOL));
        Mson.getInstance().getEntityRendererRegistry().registerEntityRenderer(EntityType.ENDERMAN, r -> new MsonEnderman.Renderer(r, ENDERMAN));
    }
}
