package com.minelittlepony.mson.api.model.biped;

import net.minecraft.client.render.entity.EndermanEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.model.EndermanEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EndermanEntity;

import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.mixin.Extends;
import com.minelittlepony.mson.api.mixin.MixedMsonModel;

@Extends(MsonBiped.class)
public class MsonEnderman<T extends LivingEntity>
    extends EndermanEntityModel<T>
    implements MixedMsonModel {

    public MsonEnderman() {
        super(0);
    }

    public static class Renderer extends EndermanEntityRenderer {
        public Renderer(EntityRenderDispatcher dispatcher, ModelKey<MsonEnderman<EndermanEntity>> key) {
            super(dispatcher);
            this.model = key.createModel();
        }
    }
}
