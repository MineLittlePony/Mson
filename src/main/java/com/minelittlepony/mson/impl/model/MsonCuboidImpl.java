package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import com.minelittlepony.mson.api.model.MsonPart;

class MsonCuboidImpl extends ModelPart implements MsonPart {

    private int textureOffsetU;
    private int textureOffsetV;

    private float modelOffsetX;
    private float modelOffsetY;
    private float modelOffsetZ;

    private boolean mirrorY;
    private boolean mirrorZ;

    MsonCuboidImpl(Model model) {
        super(model);
    }

    @Override
    public MsonCuboidImpl setTextureOffset(int u, int v) {
        this.textureOffsetU = u;
        this.textureOffsetV = v;
        super.setTextureOffset(u, v);
        return this;
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
    public ModelPart addCuboid(String name, float x, float y, float z, int width, int height, int depth, float stretch, int u, int v) {
        return super.addCuboid(name, (int)getModelOffsetX() + x, (int)getModelOffsetY() + y, (int)getModelOffsetZ() + z, width, height, depth, stretch, u, v);
     }

    @Override
    public ModelPart addCuboid(float x, float y, float z, float width, float height, float depth) {
        return super.addCuboid(getModelOffsetX() + x, getModelOffsetY() + y, getModelOffsetZ() + z, width, height, depth);
    }

    @Override
    public ModelPart addCuboid(float x, float y, float z, float width, float height, float depth, boolean mirror) {
        return super.addCuboid(getModelOffsetX() + x, getModelOffsetY() + y, getModelOffsetZ() + z, width, height, depth, mirror);
    }

    @Override
    public void addCuboid(float x, float y, float z, float width, float height, float depth, float stretch) {
        super.addCuboid(getModelOffsetX() + x, getModelOffsetY() + y, getModelOffsetZ() + z, width, height, depth, stretch);
    }

    @Override
    public void addCuboid(float x, float y, float z, float width, float height, float depth, float stretchX, float stretchY, float stretchZ) {
        super.addCuboid(getModelOffsetX() + x, getModelOffsetY() + y, getModelOffsetZ() + z, width, height, depth, stretchX, stretchY, stretchZ);
    }

    @Override
    public void addCuboid(float x, float y, float z, float width, float height, float depth, float stretch, boolean mirror) {
        super.addCuboid(getModelOffsetX() + x, getModelOffsetY() + y, getModelOffsetZ() + z, width, height, depth, stretch, mirror);
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
    public int getTextureOffsetU() {
        return textureOffsetU;
    }

    @Override
    public int getTextureOffsetV() {
        return textureOffsetV;
    }

    @Override
    public boolean getMirrorX() {
        return mirror;
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
