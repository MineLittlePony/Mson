package com.minelittlepony.mson.impl;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.util.Identifier;

import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.Mson;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.Face;
import com.minelittlepony.mson.api.model.QuadsBuilder;
import com.minelittlepony.mson.api.model.Face.Axis;
import com.minelittlepony.mson.api.model.biped.MsonCreeper;
import com.minelittlepony.mson.api.model.biped.MsonEnderman;
import com.minelittlepony.mson.api.model.biped.MsonPlayer;
import com.minelittlepony.mson.api.model.quadruped.MsonCow;
import com.minelittlepony.mson.api.model.quadruped.MsonPig;
import com.minelittlepony.mson.api.model.quadruped.MsonSheep;

public final class Test {

    public static void init() {
        MsonImpl.LOGGER.info("Loading box builder class");
        new BoxBuilder(new ModelPart(new Model(null) {
            @Override
            public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
            }
        })).size(Axis.X, 20, 20, 20)
            .build(QuadsBuilder.plane(Face.UP));
        MsonImpl.LOGGER.info("Loaded box builder class");

        /*MsonImpl.DEBUG = true;
        ModelKey<MsonPlayer<AbstractClientPlayerEntity>> STEVE = Mson.getInstance().registerModel(new Identifier("mson", "steve"), MsonPlayer::new);
        ModelKey<MsonPlayer<AbstractClientPlayerEntity>> ALEX = Mson.getInstance().registerModel(new Identifier("mson", "alex"), MsonPlayer::new);
        MsonImpl.DEBUG = false;*/

        ModelKey<MsonCreeper<CreeperEntity>> CREEPER = Mson.getInstance().registerModel(new Identifier("mson_test", "creeper"), MsonCreeper::new);
        ModelKey<MsonPig<PigEntity>> PIG = Mson.getInstance().registerModel(new Identifier("mson_test", "pig"), MsonPig::new);
        ModelKey<MsonCow<CowEntity>> COW = Mson.getInstance().registerModel(new Identifier("mson_test", "cow"), MsonCow::new);
        ModelKey<MsonSheep<SheepEntity>> SHEEP = Mson.getInstance().registerModel(new Identifier("mson_test", "sheep"), MsonSheep::new);
        ModelKey<MsonSheep<SheepEntity>> SHEEP_WOOL = Mson.getInstance().registerModel(new Identifier("mson_test", "sheep_wool"), MsonSheep::new);
        ModelKey<MsonEnderman<EndermanEntity>> ENDERMAN = Mson.getInstance().registerModel(new Identifier("mson_test", "enderman"), MsonEnderman::new);

        //ModelKey<MsonPlayer<AbstractClientPlayerEntity>> RAYMAN = Mson.getInstance().registerModel(new Identifier("mson_test", "rayman"), MsonPlayer::new);
        ModelKey<MsonPlayer<AbstractClientPlayerEntity>> PLANE = Mson.getInstance().registerModel(new Identifier("mson_test", "plane"), MsonPlayer::new);

        Mson.getInstance().getEntityRendererRegistry().registerPlayerRenderer("default", r -> new MsonPlayer.Renderer(r, PLANE));
        Mson.getInstance().getEntityRendererRegistry().registerPlayerRenderer("slim", r -> new MsonPlayer.Renderer(r, PLANE));
        Mson.getInstance().getEntityRendererRegistry().registerEntityRenderer(EntityType.CREEPER, r -> new MsonCreeper.Renderer(r, CREEPER));
        Mson.getInstance().getEntityRendererRegistry().registerEntityRenderer(EntityType.PIG, r -> new MsonPig.Renderer(r, PIG));
        Mson.getInstance().getEntityRendererRegistry().registerEntityRenderer(EntityType.COW, r -> new MsonCow.Renderer(r, COW));
        Mson.getInstance().getEntityRendererRegistry().registerEntityRenderer(EntityType.SHEEP, r -> new MsonSheep.Renderer(r, SHEEP, SHEEP_WOOL));
        Mson.getInstance().getEntityRendererRegistry().registerEntityRenderer(EntityType.ENDERMAN, r -> new MsonEnderman.Renderer(r, ENDERMAN));
    }
}
