package com.minelittlepony.mson.api.model;

import net.minecraft.util.math.Vec3d;

public class Corner {

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

    public final Vec3d normal;
    public final Vec3d stretched;

    public Corner(Vec3d normal, Vec3d stretched) {
        this.normal = normal;
        this.stretched = stretched;
    }

    @Override
    public boolean equals(Object o) {
       return this == o
               || (o instanceof Corner && ((Corner)o).normal.equals(normal) && ((Corner)o).stretched.equals(stretched))
               || (o instanceof Vec3d && ((Vec3d)o).equals(normal));
    }

    @Override
    public int hashCode() {
       return normal.hashCode();
    }

    public String toString() {
       return normal.toString();
    }
}
