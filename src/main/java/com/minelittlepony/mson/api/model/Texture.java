package com.minelittlepony.mson.api.model;

/**
 * Represents the texture configuration fields for a model.
 */
public record Texture(
        /**
         * The texture horizontal x-position. (U)
         */
        int u,
        /**
         * The texture vertical y-position. (V)
         */
        int v,
        /**
         * Pixel width of the texture file.
         */
        int width,
        /**
         * Pixel height of the texture file.
         */
        int height
        ) {
    /**
     * A blank texture. Contains the default parameters to be used when no other exist to override them.
     */
    public static final Texture EMPTY = new Texture(0, 0, 64, 32);
}
