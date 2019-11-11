package com.minelittlepony.mson.api.model.entities;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.mixin.Trait;

@Trait
public class MsonBiped<T extends LivingEntity>
    extends BipedEntityModel<T>
    implements MsonModel {

    @Override
    public void init(ModelContext context) {
        head = context.findByName("head");
        headwear = context.findByName("helmet");
        body = context.findByName("torso");
        rightArm = context.findByName("right_arm");
        leftArm = context.findByName("left_arm");
        rightLeg = context.findByName("right_leg");
        leftLeg = context.findByName("left_leg");
    }
}
