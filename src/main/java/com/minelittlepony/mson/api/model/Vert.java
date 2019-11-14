package com.minelittlepony.mson.api.model;

public interface Vert {
    @FunctionalInterface
    interface Factory {
        Vert create(float x, float y, float z, float u, float v);
    }
}
