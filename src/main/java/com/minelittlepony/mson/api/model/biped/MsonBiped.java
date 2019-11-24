package com.minelittlepony.mson.api.model.biped;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.mixin.Trait;

@Trait
public class MsonBiped<T extends LivingEntity>
    extends BipedEntityModel<T>
    implements MsonModel {

    public MsonBiped() {
        super(0);
    }

    @Override
    public void init(ModelContext context) {
        head = context.findByName("head");
        helmet = context.findByName("helmet");
        torso = context.findByName("torso");
        rightArm = context.findByName("right_arm");
        leftArm = context.findByName("left_arm");
        rightLeg = context.findByName("right_leg");
        leftLeg = context.findByName("left_leg");
    }
}