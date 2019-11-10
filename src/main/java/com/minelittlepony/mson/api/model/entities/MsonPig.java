package com.minelittlepony.mson.api.model.entities;

import net.minecraft.client.render.entity.model.PigEntityModel;
import net.minecraft.entity.LivingEntity;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.MsonModel;

public class MsonPig<T extends LivingEntity>
    extends PigEntityModel<T>
    implements MsonModel {

    @Override
    public void init(ModelContext context) {
        head = context.findByName("head");
        body = context.findByName("torso");
        leg1 = context.findByName("back_right_leg");
        leg2 = context.findByName("back_left_leg");
        leg3 = context.findByName("front_left_leg");
        leg4 = context.findByName("front_right_leg");
    }
}
