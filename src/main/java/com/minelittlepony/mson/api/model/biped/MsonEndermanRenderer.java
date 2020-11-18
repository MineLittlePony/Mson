package com.minelittlepony.mson.api.model.biped;

import net.minecraft.client.render.entity.EndermanEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EndermanEntityModel;
import net.minecraft.entity.mob.EndermanEntity;

import com.minelittlepony.mson.api.ModelKey;

public class MsonEndermanRenderer extends EndermanEntityRenderer {
    public MsonEndermanRenderer(EntityRendererFactory.Context context, ModelKey<EndermanEntityModel<EndermanEntity>> key) {
        super(context);
        this.model = key.createModel();
    }
}
