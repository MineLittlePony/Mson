package com.minelittlepony.mson.api.model;

import com.minelittlepony.mson.api.model.Face.Axis;
import com.minelittlepony.mson.impl.FixtureImpl;

/**
 * Used for stitching multiple planes together, the CoordinateFixuter
 * is what determines which vertices of each planar segment must remain fixed
 * whilst other gets dilated away from the sinde of the conjoined plane.
 *
 * @see FixtureImpl
 *
 */
public interface CoordinateFixture {

    /**
     * Returns the default (unfixed) coordinate fixture.
     *
     * @see FixtureImpl
     */
    static CoordinateFixture unfixed() {
        return FixtureImpl.NULL;
    }

    /**
     * Applies dilation to a particular vertex along a particular axis.
     *
     * @param axis    The axis to dilate along
     * @param x       The vertex X coordinate
     * @param y       The vertex Y coordinate
     * @param z       The vertex Z coordinate
     * @param stretch The amount to dilate by.
     * @return
     */
    float stretchCoordinate(Axis axis, float x, float y, float z, float stretch);
}
