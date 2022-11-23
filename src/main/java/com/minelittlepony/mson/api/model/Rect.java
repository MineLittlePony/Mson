package com.minelittlepony.mson.api.model;

import org.joml.Vector3f;

public interface Rect {
    Vector3f getNormal();

    Vert getVertex(int index);

    void setVertex(int index, Vert value);

    Rect setVertices(boolean reflect, Vert...vertices);

    int vertexCount();
}
