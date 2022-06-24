package com.minelittlepony.mson.api.model;

import net.minecraft.util.math.Vec3d;

public record Corner(
        /**
         * The untransformed (original) position of the corner vertex.
         */
        Vec3d normal,
        /**
         * The transformed (effective) position of the corner vertex after applying dilation.
         */
        Vec3d stretched) {
    public static final Vec3d[] CORNERS = new Vec3d[] {
        Vec3d.ZERO,
        new Vec3d(0, 0, 1),
        new Vec3d(0, 1, 0),
        new Vec3d(0, 1, 1),
        new Vec3d(1, 0, 0),
        new Vec3d(1, 0, 1),
        new Vec3d(1, 1, 0),
        new Vec3d(1, 1, 1)
    };
}
