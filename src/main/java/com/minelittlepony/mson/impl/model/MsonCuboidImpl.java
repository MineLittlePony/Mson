package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

import com.minelittlepony.mson.api.model.MsonPart;

class MsonCuboidImpl extends ModelPart implements MsonPart {

    private float modelOffsetX;
    private float modelOffsetY;
    private float modelOffsetZ;

    private boolean mirrorY;
    private boolean mirrorZ;

    private boolean hidden;

    MsonCuboidImpl(Model model) {
        super(model);
    }

    @Override
    public MsonPart offset(float x, float y, float z) {
        modelOffsetX = x;
        modelOffsetY = y;
        modelOffsetZ = z;
        return  this;
    }

    @Override
    public MsonPart mirror(boolean x, boolean y, boolean z) {
        MsonPart.super.mirror(x, y, z);
        mirrorY = y;
        mirrorZ = z;

        return this;
    }

    @Override
    public MsonPart setHidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    @Override
    public void render(MatrixStack matrix, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
        if (!hidden) {
            super.render(matrix, vertexConsumer, i, j, f, g, h, k);
        }
    }

    @Override
    public float getModelOffsetX() {
        return modelOffsetX;
    }

    @Override
    public float getModelOffsetY() {
        return modelOffsetY;
    }

    @Override
    public float getModelOffsetZ() {
        return modelOffsetZ;
    }

    @Override
    public boolean getMirrorY() {
        return mirrorY;
    }

    @Override
    public boolean getMirrorZ() {
        return mirrorZ;
    }
}
