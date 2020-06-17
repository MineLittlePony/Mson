package com.minelittlepony.mson.api.model;

public interface Cube {
    void setSides(Rect[] sides);

    Rect getSide(int index);

    void setSide(int index, Rect value);

    int sideCount();
}
