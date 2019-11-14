package com.minelittlepony.mson.api.model;

import net.minecraft.util.math.Direction;

public interface Rect {
    @FunctionalInterface
    interface Factory {
        Rect create(Vert[] vertices,
                float u1, float v1,
                float u2, float v2,
                float squishU, float squishV,
                boolean mirror,
                Direction direction);
    }
}
