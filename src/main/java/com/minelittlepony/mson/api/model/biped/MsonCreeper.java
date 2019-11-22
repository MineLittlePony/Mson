package com.minelittlepony.mson.api.model.biped;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.CreeperEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.math.MathHelper;

import com.google.common.collect.ImmutableList;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.MsonModel;

public class MsonCreeper<T extends Entity>
    extends CreeperEntityModel<T>
    implements MsonModel {

    private ModelPart head;
    private ModelPart helmet;
    private ModelPart torso;

    private ModelPart backLeftLeg;
    private ModelPart backRightLeg;
    private ModelPart frontLeftLeg;
    private ModelPart frontRightLeg;

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
    public Iterable<ModelPart> getParts() {
       return ImmutableList.of(head, torso, helmet, backLeftLeg, backRightLeg, frontLeftLeg, frontRightLeg);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch) {
        this.head.yaw = headYaw * 0.017453292F;
        this.head.pitch = headPitch * 0.017453292F;
        helmet.copyPositionAndRotation(head);
        this.backLeftLeg.pitch = MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance;
        this.backRightLeg.pitch = MathHelper.cos(limbAngle * 0.6662F + 3.1415927F) * 1.4F * limbDistance;
        this.frontLeftLeg.pitch = MathHelper.cos(limbAngle * 0.6662F + 3.1415927F) * 1.4F * limbDistance;
        this.frontRightLeg.pitch = MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance;
     }

    public static class Renderer extends CreeperEntityRenderer {
        public Renderer(EntityRenderDispatcher dispatcher, ModelKey<MsonCreeper<CreeperEntity>> key) {
            super(dispatcher);
            this.model = key.createModel();
        }
    }
}
