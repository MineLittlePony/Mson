package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.Cuboid;
import net.minecraft.client.model.Model;

import com.minelittlepony.mson.api.model.MsonCuboid;

class MsonCuboidImpl extends Cuboid implements MsonCuboid {

    private int textureOffsetU;
    private int textureOffsetV;

    private float modelOffsetX;
    private float modelOffsetY;
    private float modelOffsetZ;

    private boolean mirrorY;
    private boolean mirrorZ;

    MsonCuboidImpl(Model model, String name) {
        super(model, name);
    }

    @Override
    public MsonCuboidImpl setTextureOffset(int u, int v) {
        this.textureOffsetU = u;
        this.textureOffsetV = v;
        super.setTextureOffset(u, v);
        return this;
    }

    @Override
    public MsonCuboid offset(float x, float y, float z) {
        modelOffsetX = x;
        modelOffsetY = y;
        modelOffsetZ = z;
        return  this;
    }

    @Override
    public MsonCuboid mirror(boolean x, boolean y, boolean z) {
        MsonCuboid.super.mirror(x, y, z);
        mirrorY = y;
        mirrorZ = z;

        return this;
    }

    @Override
    public MsonCuboidImpl addBox(String partName, float offX, float offY, float offZ, int width, int height, int depth, float unknown, int texX, int texY) {
        partName = name + "." + partName;

        setTextureOffset(texX, texY);
        addBox(offX, offY, offZ, width, height, depth);
        boxes.get(boxes.size() - 1).setName(partName);

        return  this;
    }

    @Override
    public MsonCuboidImpl addBox(float offX, float offY, float offZ, int width, int height, int depth) {
        addBox(offX, offY, offZ, width, height, depth, 0);
        return  this;
    }

    @Override
    public MsonCuboidImpl addBox(float offX, float offY, float offZ, int width, int height, int depth, boolean mirrored) {
        addBox(offX, offY, offZ, width, height, depth, 0, mirrored);
        return this;
    }

    @Override
    public void addBox(float offX, float offY, float offZ, int width, int height, int depth, float scaleFactor) {
        addBox(offX, offY, offZ, width, height, depth, scaleFactor, mirror);
    }

    @Override
    public void addBox(float offX, float offY, float offZ, int width, int height, int depth, float scaleFactor, boolean mirrored) {
        boxes.add(createBox(offX, offY, offZ, width, height, depth, scaleFactor, mirrored));
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
