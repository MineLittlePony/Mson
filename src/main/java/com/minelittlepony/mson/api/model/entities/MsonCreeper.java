package com.minelittlepony.mson.api.model.entities;

import net.minecraft.client.model.Cuboid;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.MsonModel;
import com.mojang.blaze3d.platform.GlStateManager;

public class MsonCreeper<T extends Entity>
    extends CreeperEntityModel<T>
    implements MsonModel {

    private Cuboid head;
    private Cuboid helmet;
    private Cuboid torso;

    private Cuboid backLeftLeg;
    private Cuboid backRightLeg;
    private Cuboid frontLeftLeg;
    private Cuboid frontRightLeg;

    @Override
    public void init(ModelContext context) {
        head = context.findByName("head");
        helmet = context.findByName("helmet");
        torso = context.findByName("torso");
        backLeftLeg = context.findByName("back_left_leg");
        backRightLeg = context.findByName("back_right_leg");
        frontLeftLeg = context.findByName("front_left_leg");
        frontRightLeg = context.findByName("front_right_leg");
    }

    @Override
    public void render(T entity, float move, float swing, float ticks, float headYaw, float headPitch, float scale) {
       setAngles(entity, move, swing, ticks, headYaw, headPitch, scale);
       head.render(scale);

       GlStateManager.pushMatrix();
       head.applyTransform(scale);
       helmet.render(scale);
       GlStateManager.popMatrix();

       torso.render(scale);
       backLeftLeg.render(scale);
       backRightLeg.render(scale);
       frontLeftLeg.render(scale);
       frontRightLeg.render(scale);
    }

    @Override
    public void setAngles(T entity, float move, float swing, float ticks, float headYaw, float headPitch, float scale) {
       float headRange = 0.017453292F;
       float pi = (float)Math.PI;
       float range = swing * 1.4F;
       float freq = move * 0.6662F;

       head.yaw   = headYaw   * headRange;
       head.pitch = headPitch * headRange;
       helmet.copyRotation(head);

       backLeftLeg.pitch   = range * MathHelper.cos(freq);
       backRightLeg.pitch  = range * MathHelper.cos(freq + pi);
       frontLeftLeg.pitch  = range * MathHelper.cos(freq + pi);
       frontRightLeg.pitch = range * MathHelper.cos(freq);
    }
}
