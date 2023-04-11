package com.minelittlepony.mson.api.model;

import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

public interface Vert {
    Vec3f getPos();

    float getU();

    float getV();

    default Vert rotate(float x, float y, float z) {
        return rotate(Quaternion.fromEulerXyz(x, y, z));
    }

    default Vert rotate(Quaternion rotation) {
        getPos().rotate(rotation);
        return this;
    }
}
