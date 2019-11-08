package com.minelittlepony.mson.api.model.entities;

import net.minecraft.client.model.Cuboid;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.MsonModel;

public class MsonPlayer<T extends LivingEntity>
    extends PlayerEntityModel<T>
    implements MsonModel {

    private Cuboid cape;
    private Cuboid ears;

    public MsonPlayer() {
        this(false);
    }

    public MsonPlayer(boolean isAlex) {
        super(0, isAlex);
    }

    @Override
    public void init(ModelContext context) {
        head = context.findByName("head");
        headwear = context.findByName("helmet");
        body = context.findByName("torso");
        rightArm = context.findByName("right_arm");
        leftArm = context.findByName("left_arm");
        rightLeg = context.findByName("right_leg");
        leftLeg = context.findByName("left_leg");

        context.findByName("left_sleeve", leftArmOverlay);
        context.findByName("right_sleeve", rightArmOverlay);

        context.findByName("left_pant_leg", leftLegOverlay);
        context.findByName("right_pant_leg", rightLegOverlay);

        context.findByName("jacket", bodyOverlay);
        cape = context.findByName("cape");
        ears = context.findByName("ears");
    }

    @Override
    public void renderEars(float scale) {
        ears.copyRotation(head);
        ears.rotationPointX = 0;
        ears.rotationPointY = 0;
        ears.render(scale);
    }

    @Override
    public void renderCape(float scale) {
        cape.render(scale);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        cape.visible = visible;
        ears.visible = visible;
     }
}
