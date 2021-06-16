package com.minelittlepony.mson.impl;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.client.render.entity.model.EndermanEntityModel;
import net.minecraft.client.render.entity.model.PigEntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.util.Identifier;

import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.Mson;
import com.minelittlepony.mson.api.model.biped.MsonCreeperRenderer;
import com.minelittlepony.mson.api.model.biped.MsonEndermanRenderer;
import com.minelittlepony.mson.api.model.biped.MsonPlayer;
import com.minelittlepony.mson.api.model.quadruped.MsonCowRenderer;
import com.minelittlepony.mson.api.model.quadruped.MsonPigRenderer;

public final class Test {

    public static void init() {
        /*MsonImpl.DEBUG = true;
        ModelKey<MsonPlayer<AbstractClientPlayerEntity>> STEVE = Mson.getInstance().registerModel(new Identifier("mson", "steve"), MsonPlayer::new);
        ModelKey<MsonPlayer<AbstractClientPlayerEntity>> ALEX = Mson.getInstance().registerModel(new Identifier("mson", "alex"), MsonPlayer::new);
        MsonImpl.DEBUG = false;*/

        ModelKey<CreeperEntityModel<CreeperEntity>> CREEPER = Mson.getInstance().registerModel(new Identifier("mson_test", "creeper"), CreeperEntityModel::new);
        ModelKey<PigEntityModel<PigEntity>> PIG = Mson.getInstance().registerModel(new Identifier("mson_test", "pig"), PigEntityModel::new);
        ModelKey<CowEntityModel<CowEntity>> COW = Mson.getInstance().registerModel(new Identifier("mson_test", "cow"), CowEntityModel::new);
        ModelKey<EndermanEntityModel<EndermanEntity>> ENDERMAN = Mson.getInstance().registerModel(new Identifier("mson_test", "enderman"), EndermanEntityModel::new);

        ModelKey<MsonPlayer<AbstractClientPlayerEntity>> RAYMAN = Mson.getInstance().registerModel(new Identifier("mson_test", "rayman"), MsonPlayer::new);
        //ModelKey<MsonPlayer<AbstractClientPlayerEntity>> PLANE = Mson.getInstance().registerModel(new Identifier("mson_test", "plane"), MsonPlayer::new);

        Mson.getInstance().getEntityRendererRegistry().registerPlayerRenderer("default", r -> new MsonPlayer.Renderer(r, RAYMAN));
        Mson.getInstance().getEntityRendererRegistry().registerPlayerRenderer("slim", r -> new MsonPlayer.Renderer(r, RAYMAN));
        Mson.getInstance().getEntityRendererRegistry().registerEntityRenderer(EntityType.CREEPER, r -> new MsonCreeperRenderer(r, CREEPER));
        Mson.getInstance().getEntityRendererRegistry().registerEntityRenderer(EntityType.PIG, r -> new MsonPigRenderer(r, PIG));
        Mson.getInstance().getEntityRendererRegistry().registerEntityRenderer(EntityType.COW, r -> new MsonCowRenderer(r, COW));
        Mson.getInstance().getEntityRendererRegistry().registerEntityRenderer(EntityType.ENDERMAN, r -> new MsonEndermanRenderer(r, ENDERMAN));
    }
}
