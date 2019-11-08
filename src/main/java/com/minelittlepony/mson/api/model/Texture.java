package com.minelittlepony.mson.api.model;

/**
 * Represents the texture configuration fields for a model.
 */
public interface Texture {

    /**
     * The texture horizontal x-position. (U)
     */
    int getU();

    /**
     * The texture vertical y-position. (V)
     * @return
     */
    int getV();

    /**
     * Pixel width of the texture file.
     */
    int getWidth();

    /**
     * Pixel height of the texture file.
     */
    int getHeight();
}
