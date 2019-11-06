package com.minelittlepony.mson.model;

import net.minecraft.client.model.Box;
import net.minecraft.client.model.Cuboid;
import net.minecraft.client.model.Quad;
import net.minecraft.client.model.Vertex;
import net.minecraft.client.render.BufferBuilder;

public class MsonBox extends Box {

    protected float texU;
    protected float texV;

    public boolean hidden = false;

    public MsonBox(Cuboid renderer,
            int texU, int texV,
            float x, float y, float z,
            int dx, int dy, int dz,
            float scale,
            boolean mirrorX) {
        super(renderer, texU, texV, x, y, z, dx, dy, dz, scale, mirrorX);
        this.texU = texU;
        this.texV = texV;
    }

    /**
     * Creates a new vertex mapping the given (x, y, z) coordinates to a texture offset.
     */
    protected Vertex vert(float x, float y, float z, int texX, int texY) {
        return new Vertex(x, y, z, texX, texY);
    }

    /**
     * Creates a new quad with the given spacial vertices.
     */
    protected Quad quad(int startX, int width, int startY, int height, Vertex ...verts) {
        return new Quad(verts,
                startX,         startY,
                startX + width, startY + height,
                texU, texV);
    }

    @Override
    public void render(BufferBuilder buffer, float scale) {
        if (!hidden) {
            super.render(buffer, scale);
        }
    }
}