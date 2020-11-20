package com.minelittlepony.mson.api.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.Cuboid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface MsonPart {

    public static ModelPart EMPTY_PART = new ModelPart(new ArrayList<>(), new HashMap<String, ModelPart>());
    public static Cuboid EMPTY_CUBE = new Cuboid(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, true, 0, 0);

    public static MsonPart of(ModelPart part) {
        return (MsonPart)(Object)part;
    }

    List<ModelPart.Cuboid> getCubes();

    Map<String, ModelPart> getChildren();

    /**
     * Sets this renderer's rotation angles.
     */
    default MsonPart rotate(float pitch, float yaw, float roll) {
        ((ModelPart)(Object)this).pitch = pitch;
        ((ModelPart)(Object)this).yaw = yaw;
        ((ModelPart)(Object)this).roll = roll;
        return this;
    }

    default MsonPart rotate(float[] rotation) {
        return rotate(rotation[0], rotation[1], rotation[2]);
    }

    /**
     * Sets whether this part is hidden or not.
     */
    default MsonPart setHidden(boolean hidden) {
        ((ModelPart)(Object)this).visible = !hidden;
        return this;
    }

    /**
     * Sets the rotation point.
     */
    default MsonPart around(float x, float y, float z) {
        ((ModelPart)(Object)this).setPivot(x, y, z);
        return this;
    }

    default MsonPart around(float[] pivot) {
        return around(pivot[0], pivot[1], pivot[2]);
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
        ((ModelPart)(Object)this).pivotX += x;
        ((ModelPart)(Object)this).pivotY += y;
        ((ModelPart)(Object)this).pivotZ += z;
        return this;
    }
}
