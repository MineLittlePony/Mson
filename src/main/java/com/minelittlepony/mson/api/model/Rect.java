package com.minelittlepony.mson.api.model;

import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

public interface Rect {
    Vec3f getNormal();

    Vert getVertex(int index);

    void setVertex(int index, Vert value);

    Rect setVertices(boolean reflect, Vert...vertices);

    int vertexCount();

    default Rect rotate(float x, float y, float z) {
        return rotate(Quaternion.fromEulerXyz(x, y, z));
    }

    default Rect rotate(Quaternion rotation) {
        for (int i = 0; i < vertexCount(); i++) {
            getVertex(i).rotate(rotation);
        }
        return this;
    }
}
