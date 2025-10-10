package com.minelittlepony.mson.api.model;

import org.joml.Quaternionf;
import org.joml.Vector3fc;

public interface Rect {
    Vector3fc getNormal();

    Vert getVertex(int index);

    void setVertex(int index, Vert value);

    Rect setVertices(boolean reflect, Vert...vertices);

    int vertexCount();

    Vert[] getVertices();

    default Rect rotate(float x, float y, float z) {
        return rotate(new Quaternionf().rotateXYZ(x, y, z));
    }

    default Rect rotate(Quaternionf rotation) {
        for (int i = 0; i < vertexCount(); i++) {
            setVertex(i, getVertex(i).rotate(rotation));
        }
        return this;
    }
}
