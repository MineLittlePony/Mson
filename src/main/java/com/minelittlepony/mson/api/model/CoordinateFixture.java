package com.minelittlepony.mson.api.model;

import com.minelittlepony.mson.api.model.Face.Axis;
import com.minelittlepony.mson.impl.FixtureImpl;

public interface CoordinateFixture {

    static CoordinateFixture unfixed() {
        return FixtureImpl.NULL;
    }

    float stretchCoordinate(Axis axis, float x, float y, float z, float stretch);
}
