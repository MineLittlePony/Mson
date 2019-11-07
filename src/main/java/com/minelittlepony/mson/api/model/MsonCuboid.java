package com.minelittlepony.mson.api.model;

import net.minecraft.client.model.Cuboid;

public interface MsonCuboid {

    /**
     * Sets the texture offset
     */
    default MsonCuboid tex(int x, int y) {
        return (MsonCuboid)((Cuboid)this).setTextureOffset(x, y);
    }

    /**
     * Sets the texture size for this renderer.
     */
    default MsonCuboid size(int w, int h) {
        return (MsonCuboid)((Cuboid)this).setTextureSize(w, h);
    }

    /**
     * Sets this renderer's rotation angles.
     */
    default MsonCuboid rotate(float pitch, float yaw, float roll) {
        ((Cuboid)this).pitch = pitch;
        ((Cuboid)this).yaw = yaw;
        ((Cuboid)this).roll = roll;
        return this;
    }

    /**
     * Positions a given model in space by setting its offset values divided
     * by 16 to account for scaling applied inside the model.
     */
    default MsonCuboid at(float x, float y, float z) {
        ((Cuboid)this).x = x / 16;
        ((Cuboid)this).y = y / 16;
        ((Cuboid)this).z = z / 16;
        return this;
    }

    /**
     * Sets the rotation point.
     */
    default MsonCuboid around(float x, float y, float z) {
        ((Cuboid)this).setRotationPoint(x, y, z);
        return this;
    }

    /**
     * Rotates this model to align itself with the angles of another.
     */
    default void rotateTo(Cuboid other) {
        rotate(other.pitch, other.yaw, other.roll);
    }

    /**
     * Shifts this model to align its center with the center of another.
     */
    default MsonCuboid rotateAt(Cuboid other) {
        return around(other.rotationPointX, other.rotationPointY, other.rotationPointZ);
    }

    /**
     * Sets an offset to be used on all shapes and children created through this renderer.
     */
    default MsonCuboid offset(float x, float y, float z) {
        return this;
    }

    /**
     * Adjusts the rotation center of the given renderer by the given amounts in each direction.
     */
    default MsonCuboid shift(float x, float y, float z) {
        ((Cuboid)this).rotationPointX += x;
        ((Cuboid)this).rotationPointY += y;
        ((Cuboid)this).rotationPointZ += z;
        return this;
    }

    /**
     * Sets whether certain dimensions are mirrored.
     */
    default MsonCuboid mirror(boolean x, boolean y, boolean z) {
        ((Cuboid)this).mirror = x;

        return this;
    }

    int getTextureOffsetU();

    int getTextureOffsetV();

    default float getModelOffsetX() {
        return 0;
    }

    default float getModelOffsetY() {
        return 0;
    }

    default float getModelOffsetZ() {
        return 0;
    }

    boolean getMirrorX();

    default boolean getMirrorY() {
        return false;
    }

    default boolean getMirrorZ() {
        return false;
    }
}
