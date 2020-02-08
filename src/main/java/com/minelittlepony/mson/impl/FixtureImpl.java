package com.minelittlepony.mson.impl;

import com.minelittlepony.mson.api.model.CoordinateFixture;
import com.minelittlepony.mson.api.model.Face.Axis;

public class FixtureImpl implements CoordinateFixture {

    public static final CoordinateFixture NULL = new FixtureImpl();

    protected boolean isFixed(Axis axis, float x, float y, float z) {
        return false;
    }

    @Override
    public float stretchCoordinate(Axis axis, float x, float y, float z, float stretch) {
        return getValue(axis, x, y, z) + (isFixed(axis, x, y, z) ? -stretch : stretch);
    }

    private float getValue(Axis axis, float x, float y, float z) {
        switch (axis) {
            case X: return x;
            case Y: return y;
            case Z: return z;
            default: return 0;
        }
    }
}
