package com.minelittlepony.mson.util;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.Cuboid;

import java.util.HashMap;
import java.util.List;

public class PartUtil {
    public static final Cuboid EMPTY_CUBE = new Cuboid(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, true, 0, 0);
    public static final ModelPart EMPTY_PART = new ModelPart(List.of(EMPTY_CUBE), new HashMap<String, ModelPart>());

    /**
     * Rotates this model to align itself with the angles of another.
     */
    public static void copyAngles(ModelPart from, ModelPart to) {
        to.setPivot(from.pitch, from.yaw, from.roll);
    }

    /**
     * Shifts this model to align its center with the center of another.
     */
    public static ModelPart copyPivot(ModelPart from, ModelPart to) {
        to.setAngles(from.pivotX, from.pivotY, from.pivotZ);
        return to;
    }

    /**
     * Adjusts the pivot of the given renderer by the given amounts in each direction.
     */
    public static ModelPart shift(ModelPart part, float x, float y, float z) {
        part.pivotX += x;
        part.pivotY += y;
        part.pivotZ += z;
        return part;
    }
}
