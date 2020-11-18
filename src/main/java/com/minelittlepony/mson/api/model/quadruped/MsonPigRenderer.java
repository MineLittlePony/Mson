package com.minelittlepony.mson.api.model.quadruped;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PigEntityRenderer;
import net.minecraft.client.render.entity.model.PigEntityModel;
import net.minecraft.entity.passive.PigEntity;

import com.minelittlepony.mson.api.ModelKey;

public class MsonPigRenderer extends PigEntityRenderer {
    public MsonPigRenderer(EntityRendererFactory.Context context, ModelKey<PigEntityModel<PigEntity>> key) {
        super(context);
        this.model = key.createModel();
    }
}
