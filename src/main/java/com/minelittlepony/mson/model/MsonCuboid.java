package com.minelittlepony.mson.model;

import net.minecraft.client.model.Cuboid;
import net.minecraft.client.model.Model;

public class MsonCuboid extends Cuboid {

    protected final Model baseModel;

    private int textureOffsetX;
    private int textureOffsetY;

    protected float modelOffsetX;
    protected float modelOffsetY;
    protected float modelOffsetZ;

    public boolean mirrorY;
    public boolean mirrorZ;

    public MsonCuboid(Model model, String name) {
        super(model, name);
        baseModel = model;
    }

    public MsonCuboid(Model model) {
        super(model);
        baseModel = model;
    }

    public MsonCuboid(Model model, int texX, int texY) {
        super(model, texX, texY);
        baseModel = model;
    }

    @Override
    public MsonCuboid setTextureOffset(int x, int y) {
        this.textureOffsetX = x;
        this.textureOffsetY = y;
        super.setTextureOffset(x, y);
        return  this;
    }

    /**
     * Sets the texture offset
     */
    public MsonCuboid tex(int x, int y) {
        return setTextureOffset(x, y);
    }

    /**
     * Sets the texture size for this renderer.
     */
    public MsonCuboid size(int w, int h) {
        return (MsonCuboid)setTextureSize(w, h);
    }

    /**
     * Positions this model in space.
     */
    public MsonCuboid at(float x, float y, float z) {
        return at(this, x, y, z);
    }

    /**
     * Sets an offset to be used on all shapes and children created through this renderer.
     */
    public MsonCuboid offset(float x, float y, float z) {
        modelOffsetX = x;
        modelOffsetY = y;
        modelOffsetZ = z;
        return  this;
    }

    /**
     * Adjusts the rotation center of the given renderer by the given amounts in each direction.
     */
    public static void shiftRotationPoint(Cuboid renderer, float x, float y, float z) {
        renderer.rotationPointX += x;
        renderer.rotationPointY += y;
        renderer.rotationPointZ += z;
    }

    /**
     * Sets this renderer's rotation angles.
     */
    public MsonCuboid rotate(float x, float y, float z) {
        pitch = x;
        yaw = y;
        roll = z;
        return  this;
    }

    /**
     * Positions a given model in space by setting its offset values divided
     * by 16 to account for scaling applied inside the model.
     */
    public static <T extends Cuboid> T at(T renderer, float x, float y, float z) {
        renderer.x = x / 16;
        renderer.y = y / 16;
        renderer.z = z / 16;
        return renderer;
    }

    /**
     * Rotates this model to align itself with the angles of another.
     */
    public void rotateTo(Cuboid other) {
        rotate(other.pitch, other.yaw, other.roll);
    }

    /**
     * Shifts this model to align its center with the center of another.
     */
    public MsonCuboid rotateAt(Cuboid other) {
        return around(other.rotationPointX, other.rotationPointY, other.rotationPointZ);
    }

    /**
     * Sets the rotation point.
     */
    public MsonCuboid around(float x, float y, float z) {
        setRotationPoint(x, y, z);
        return  this;
    }

    public MsonPlane createPlane(float x, float y, float z, int width, int height, int depth, float scale, Face face) {
        return new MsonPlane(this,
            textureOffsetX, textureOffsetY,
            modelOffsetX + x, modelOffsetY + y, modelOffsetZ + z,
            width, height, depth,
            scale,
            mirror, mirrorY, mirrorZ,
            face
        );
    }

    private MsonCuboid addPlane(float x, float y, float z, int width, int height, int depth, float scale, Face face) {
        boxes.add(createPlane(x, y, z, width, height, depth, scale, face));
        return  this;
    }

    public MsonCuboid top(float x, float y, float z, int width, int depth, float scale) {
        return addPlane(x, y, z, width, 0, depth, scale, Face.UP);
    }

    public MsonCuboid bottom(float x, float y, float z, int width, int depth, float scale) {
        return addPlane(x, y, z, width, 0, depth, scale, Face.DOWN);
    }

    public MsonCuboid west(float x, float y, float z, int height, int depth, float scale) {
        return addPlane(x, y, z, 0, height, depth, scale, Face.WEST);
    }

    public MsonCuboid east(float x, float y, float z, int height, int depth, float scale) {
        return addPlane(x, y, z, 0, height, depth, scale, Face.EAST);
    }

    public MsonCuboid north(float x, float y, float z, int width, int height, float scale) {
        return addPlane(x, y, z - scale * 2, width, height, 0, scale, Face.NORTH);
    }

    public MsonCuboid south(float x, float y, float z, int width, int height, float scale) {
        return addPlane(x, y, z + scale * 2, width, height, 0, scale, Face.SOUTH);
    }

    @Override
    public MsonCuboid addBox(String partName, float offX, float offY, float offZ, int width, int height, int depth, float unknown, int texX, int texY) {
        partName = name + "." + partName;

        setTextureOffset(texX, texY);
        addBox(offX, offY, offZ, width, height, depth);
        boxes.get(boxes.size() - 1).setName(partName);

        return  this;
    }

    @Override
    public MsonCuboid addBox(float offX, float offY, float offZ, int width, int height, int depth) {
        addBox(offX, offY, offZ, width, height, depth, 0);
        return  this;
    }

    @Override
    public MsonCuboid addBox(float offX, float offY, float offZ, int width, int height, int depth, boolean mirrored) {
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

    public MsonBox createBox(float offX, float offY, float offZ, int width, int height, int depth, float scaleFactor, boolean mirrored) {
        return new MsonBox(this, textureOffsetX, textureOffsetY, modelOffsetX + offX, modelOffsetY + offY, modelOffsetZ + offZ, width, height, depth, scaleFactor, mirrored);
    }
}
