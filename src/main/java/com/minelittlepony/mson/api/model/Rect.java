package com.minelittlepony.mson.api.model;

import net.minecraft.util.math.Direction;

public interface Rect {
    @FunctionalInterface
    interface ConstrDefinition {
        Rect create(Vert[] vertices,
                float u1, float v1,
                float u2, float v2,
                float squishU, float squishV,
                boolean flip,
                Direction direction);
    }

    @FunctionalInterface
    interface Factory {
        Rect create(Object object,
                float u1, float v1,
                float u2, float v2,
                float squishU, float squishV,
                boolean flip,
                Direction direction);
    }
}
