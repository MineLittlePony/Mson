package com.minelittlepony.mson.api.model.entities;

import net.minecraft.client.render.entity.CowEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.CowEntity;
import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.mixin.Extends;
import com.minelittlepony.mson.api.mixin.MixedMsonModel;

@Extends(MsonQuadruped.class)
public class MsonCow<T extends LivingEntity>
    extends CowEntityModel<T>
    implements MixedMsonModel {
    public static class Renderer extends CowEntityRenderer {
        public Renderer(EntityRenderDispatcher dispatcher, ModelKey<MsonCow<CowEntity>> key) {
            super(dispatcher);
            this.model = key.createModel();
        }
    }
}
