package com.minelittlepony.mson.api.model;

public interface Rect {
    void invertNormals();

    @FunctionalInterface
    interface Factory {
        Rect create(Vert[] vertices, float u1, float v1, float u2, float v2, float squishU, float squishV);
    }
}
