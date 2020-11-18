package com.minelittlepony.mson.api.model.quadruped;

import net.minecraft.client.render.entity.CowEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.entity.passive.CowEntity;
import com.minelittlepony.mson.api.ModelKey;

public class MsonCowRenderer extends CowEntityRenderer {
    public MsonCowRenderer(EntityRendererFactory.Context context, ModelKey<CowEntityModel<CowEntity>> key) {
        super(context);
        this.model = key.createModel();
    }
}
