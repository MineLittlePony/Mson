package com.minelittlepony.mson.api.model;

import net.minecraft.client.util.math.Vector3f;

public interface Rect {
    Vector3f getNormal();

    Vert getVertex(int index);

    void setVertex(int index, Vert value);

    int vertexCount();
}
