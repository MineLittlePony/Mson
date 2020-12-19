package com.minelittlepony.mson.api.model;

import net.minecraft.util.math.Vec3f;

public interface Rect {
    Vec3f getNormal();

    Vert getVertex(int index);

    void setVertex(int index, Vert value);

    Rect setVertices(boolean reflect, Vert...vertices);

    int vertexCount();
}
