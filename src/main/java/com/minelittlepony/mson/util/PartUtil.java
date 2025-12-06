package com.minelittlepony.mson.util;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelPart.Cube;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PartUtil {
    public static final Cube EMPTY_CUBE = new Cube(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, true, 0, 0, Set.of());
    public static final ModelPart EMPTY_PART = new ModelPart(List.of(EMPTY_CUBE), Map.of());

    /**
     * Rotates this model to align itself with the angles of another.
     */
    public static void copyAngles(ModelPart from, ModelPart to) {
        to.setRotation(from.xRot, from.yRot, from.zRot);
    }

    /**
     * Shifts this model to align its center with the center of another.
     */
    public static ModelPart copyPivot(ModelPart from, ModelPart to) {
        to.setPos(from.x, from.y, from.z);
        return to;
    }

    /**
     * Adjusts the pivot of the given renderer by the given amounts in each direction.
     */
    public static ModelPart shift(ModelPart part, float x, float y, float z) {
        part.x += x;
        part.y += y;
        part.z += z;
        return part;
    }
}
