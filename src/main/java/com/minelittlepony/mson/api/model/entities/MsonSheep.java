package com.minelittlepony.mson.api.model.entities;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.SheepEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.SheepEntityModel;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.mixin.Extends;
import com.minelittlepony.mson.api.mixin.MixedMsonModel;
import com.mojang.blaze3d.platform.GlStateManager;

@Extends(MsonQuadruped.class)
public class MsonSheep<T extends SheepEntity>
    extends SheepEntityModel<T>
    implements MixedMsonModel {
    public static class Renderer extends SheepEntityRenderer {
        private static final Identifier WOOL_SKIN = new Identifier("textures/entity/sheep/sheep_fur.png");

        public Renderer(EntityRenderDispatcher dispatcher,
                ModelKey<MsonSheep<SheepEntity>> body,
                ModelKey<MsonSheep<SheepEntity>> wool) {
            super(dispatcher);
            model = body.createModel();
            features.clear();
            addFeature(new WoolFeature(this, wool));
        }

        protected class WoolFeature extends FeatureRenderer<SheepEntity, SheepEntityModel<SheepEntity>> {

            private final SheepEntityModel<SheepEntity> model;

            public WoolFeature(Renderer context, ModelKey<MsonSheep<SheepEntity>> key) {
               super(context);
               model = key.createModel();
            }

            @Override
            public void render(SheepEntity entity, float move, float swing, float time, float ticks, float headYaw, float headPitch, float scale) {
               if (!entity.isSheared() && !entity.isInvisible()) {
                  bindTexture(WOOL_SKIN);
                  if (entity.hasCustomName() && "jeb_".equals(entity.getName().asString())) {
                     int age = entity.age / 25 + entity.getEntityId();
                     int totalColours = DyeColor.values().length;

                     float[] from = SheepEntity.getRgbColor(DyeColor.byId(age % totalColours));
                     float[] to = SheepEntity.getRgbColor(DyeColor.byId((age + 1) % totalColours));

                     float diff = (entity.age % 25 + time) / 25F;

                     GlStateManager.color3f(
                             from[0] * (1 - diff) + to[0] * diff,
                             from[1] * (1 - diff) + to[1] * diff,
                             from[2] * (1 - diff) + to[2] * diff
                     );
                  } else {
                     float[] colour = SheepEntity.getRgbColor(entity.getColor());
                     GlStateManager.color3f(colour[0], colour[1], colour[2]);
                  }

                  getModel().copyStateTo(model);
                  model.animateModel(entity, move, swing, time);
                  model.render(entity, move, swing, ticks, headYaw, headPitch, scale);
               }
            }

            @Override
            public boolean hasHurtOverlay() {
               return true;
            }
         }
    }
}
