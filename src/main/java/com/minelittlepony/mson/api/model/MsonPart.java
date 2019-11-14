package com.minelittlepony.mson.api.model;

import net.minecraft.client.model.ModelPart;

public interface MsonPart {

    /**
     * Sets the cuboid's texture parameters.
     */
    default MsonPart tex(Texture tex) {
        ((ModelPart)this).setTextureOffset(tex.getU(), tex.getV());
        ((ModelPart)this).setTextureSize(tex.getWidth(), tex.getHeight());
        return this;
    }

    /**
     * Sets this renderer's rotation angles.
     */
    default MsonPart rotate(float pitch, float yaw, float roll) {
        ((ModelPart)this).pitch = pitch;
        ((ModelPart)this).yaw = yaw;
        ((ModelPart)this).roll = roll;
        return this;
    }

    /**
     * Sets whether this part is hidden or not.
     */
    default MsonPart setHidden(boolean hidden) {
        return this;
    }

    /**
     * Sets the rotation point.
     */
    default MsonPart around(float x, float y, float z) {
        ((ModelPart)this).setPivot(x, y, z);
        return this;
    }

    /**
     * Rotates this model to align itself with the angles of another.
     */
    default void rotateTo(ModelPart other) {
        rotate(other.pitch, other.yaw, other.roll);
    }

    /**
     * Shifts this model to align its center with the center of another.
     */
    default MsonPart rotateAt(ModelPart other) {
        return around(other.pivotX, other.pivotY, other.pivotZ);
    }

    /**
     * Adjusts the rotation center of the given renderer by the given amounts in each direction.
     */
    default MsonPart shift(float x, float y, float z) {
        ((ModelPart)this).pivotX += x;
        ((ModelPart)this).pivotY += y;
        ((ModelPart)this).pivotZ += z;
        return this;
    }

    /**
     * Sets whether certain dimensions are mirrored.
     */
    default MsonPart mirror(boolean x, boolean y, boolean z) {
        ((ModelPart)this).mirror = x;
        return this;
    }

    default Texture getTexture() {
        return (Texture)this;
    }

    default boolean getMirrorX() {
        return ((ModelPart)this).mirror;
    }

    default boolean getMirrorY() {
        return false;
    }

    default boolean getMirrorZ() {
        return false;
    }
}
