package com.minelittlepony.mson.api.model;

import net.minecraft.client.model.ModelPart;
import com.minelittlepony.mson.util.PartUtil;

/**
 * Rather us PartUtil
 */
@Deprecated
public interface MsonPart {

    static MsonPart of(ModelPart part) {
        return (MsonPart)(Object)part;
    }

    /**
     * Sets this renderer's rotation angles.
     */
    default MsonPart rotate(float pitch, float yaw, float roll) {
        ((ModelPart)(Object)this).setAngles(pitch, yaw, roll);
        return this;
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

    /**
     * Rotates this model to align itself with the angles of another.
     */
    default void rotateTo(ModelPart other) {
        PartUtil.copyAngles(other, (ModelPart)(Object)this);
    }

    /**
     * Shifts this model to align its center with the center of another.
     */
    default MsonPart rotateAt(ModelPart other) {
        PartUtil.copyPivot(other, (ModelPart)(Object)this);
        return this;
    }

    /**
     * Adjusts the rotation center of the given renderer by the given amounts in each direction.
     */
    default MsonPart shift(float x, float y, float z) {
        PartUtil.shift((ModelPart)(Object)this, x, y, z);
        return this;
    }
}
