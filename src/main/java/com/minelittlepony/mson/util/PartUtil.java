package com.minelittlepony.mson.util;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.Cuboid;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PartUtil {
    public static final Cuboid EMPTY_CUBE = new Cuboid(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, true, 0, 0, Set.of());
    public static final ModelPart EMPTY_PART = new ModelPart(List.of(EMPTY_CUBE), Map.of());

    /**
     * Rotates this model to align itself with the angles of another.
     */
    public static void copyAngles(ModelPart from, ModelPart to) {
        to.setAngles(from.pitch, from.yaw, from.roll);
    }

    /**
     * Shifts this model to align its center with the center of another.
     */
    public static ModelPart copyPivot(ModelPart from, ModelPart to) {
        to.setOrigin(from.originX, from.originY, from.originZ);
        return to;
    }

    /**
     * Adjusts the pivot of the given renderer by the given amounts in each direction.
     */
    public static ModelPart shift(ModelPart part, float x, float y, float z) {
        part.originX += x;
        part.originY += y;
        part.originZ += z;
        return part;
    }
}
