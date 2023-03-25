package com.minelittlepony.mson.api.model;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface Vert {
    Vector3f getPos();

    float getU();

    float getV();

    default Vert rotate(float x, float y, float z) {
        return rotate(new Quaternionf().rotateXYZ(x, y, z));
    }

    default Vert rotate(Quaternionf rotation) {
        getPos().rotate(rotation);
        return this;
    }
}
