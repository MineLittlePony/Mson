package com.minelittlepony.mson.api.model.quadruped;

import net.minecraft.client.render.entity.model.QuadrupedEntityModel;
import net.minecraft.entity.LivingEntity;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.mixin.Trait;

@Trait
public class MsonQuadruped<T extends LivingEntity>
    extends QuadrupedEntityModel<T>
    implements MsonModel {

    public MsonQuadruped() {
        super(0, 0, false, 0, 0, 0, 0, 0);
    }

    @Override
    public void init(ModelContext context) {
        head = context.findByName("head");
        torso = context.findByName("torso");
        backRightLeg = context.findByName("back_right_leg");
        backLeftLeg = context.findByName("back_left_leg");
        frontRightLeg = context.findByName("front_right_leg");
        frontLeftLeg = context.findByName("front_left_leg");
    }
}