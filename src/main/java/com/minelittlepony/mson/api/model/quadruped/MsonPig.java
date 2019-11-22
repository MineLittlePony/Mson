package com.minelittlepony.mson.api.model.quadruped;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PigEntityRenderer;
import net.minecraft.client.render.entity.model.PigEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.PigEntity;

import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.mixin.Extends;
import com.minelittlepony.mson.api.mixin.MixedMsonModel;

@Extends(MsonQuadruped.class)
public class MsonPig<T extends LivingEntity>
    extends PigEntityModel<T>
    implements MixedMsonModel {
    public static class Renderer extends PigEntityRenderer {
        public Renderer(EntityRenderDispatcher dispatcher, ModelKey<MsonPig<PigEntity>> key) {
            super(dispatcher);
            this.model = key.createModel();
        }
    }
}
