package com.minelittlepony.mson.api.model.entities;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.mixin.Extends;
import com.minelittlepony.mson.api.mixin.MixedMsonModel;

@Extends(MsonBiped.class)
public class MsonPlayer<T extends LivingEntity>
    extends PlayerEntityModel<T>
    implements MixedMsonModel {

    private ModelPart cape;
    private ModelPart ears;

    public MsonPlayer() {
        this(false);
    }

    public MsonPlayer(boolean isAlex) {
        super(0, isAlex);
    }

    @Override
    public void init(ModelContext context) {
        MixedMsonModel.super.init(context);

        context.findByName("left_sleeve", leftArmOverlay);
        context.findByName("right_sleeve", rightArmOverlay);

        context.findByName("left_pant_leg", leftLegOverlay);
        context.findByName("right_pant_leg", rightLegOverlay);

        context.findByName("jacket", bodyOverlay);
        cape = context.findByName("cape");
        ears = context.findByName("ears");
    }

    @Override
    public void renderEars(MatrixStack renderMatrix, VertexConsumer vertexBuffer, int i, int j) {
        ears.copyPositionAndRotation(head);
        ears.pivotX = 0;
        ears.pivotY = 0;
        ears.render(renderMatrix, vertexBuffer, i, j, null);
    }

    @Override
    public void renderCape(MatrixStack renderMatrix, VertexConsumer vertexBuffer, int i, int j) {
        cape.render(renderMatrix, vertexBuffer, i, j, null);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        cape.visible = visible;
        ears.visible = visible;
    }

    public static class Renderer extends PlayerEntityRenderer {
        public Renderer(EntityRenderDispatcher dispatcher, ModelKey<MsonPlayer<AbstractClientPlayerEntity>> key) {
            super(dispatcher);
            this.model = key.createModel();
        }
    }
}
