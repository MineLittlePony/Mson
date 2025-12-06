package com.minelittlepony.mson.api.model;

import net.minecraft.world.phys.Vec3;

public record Corner(
        /**
         * The untransformed (original) position of the corner vertex.
         */
        Vec3 normal,
        /**
         * The transformed (effective) position of the corner vertex after applying dilation.
         */
        Vec3 stretched) {
    public static final Vec3[] CORNERS = new Vec3[] {
        Vec3.ZERO,
        new Vec3(0, 0, 1),
        new Vec3(0, 1, 0),
        new Vec3(0, 1, 1),
        new Vec3(1, 0, 0),
        new Vec3(1, 0, 1),
        new Vec3(1, 1, 0),
        new Vec3(1, 1, 1)
    };
}
