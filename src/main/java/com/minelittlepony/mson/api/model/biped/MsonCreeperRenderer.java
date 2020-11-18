package com.minelittlepony.mson.api.model.biped;

import net.minecraft.client.render.entity.CreeperEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.entity.mob.CreeperEntity;
import com.minelittlepony.mson.api.ModelKey;

public class MsonCreeperRenderer extends CreeperEntityRenderer {
    public MsonCreeperRenderer(EntityRendererFactory.Context context, ModelKey<CreeperEntityModel<CreeperEntity>> key) {
        super(context);
        this.model = key.createModel();
    }
}
