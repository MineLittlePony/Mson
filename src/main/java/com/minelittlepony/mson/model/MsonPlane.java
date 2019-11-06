package com.minelittlepony.mson.model;

import net.minecraft.client.model.Cuboid;
import net.minecraft.client.model.Quad;
import net.minecraft.client.model.Vertex;
import net.minecraft.client.render.BufferBuilder;

import javax.annotation.Nonnull;

public class MsonPlane extends MsonBox {

    private Quad quad;

    public MsonPlane(Cuboid renderer,
            int textureX, int textureY,
            float x, float y, float z,
            int dx, int dy, int dz,
            float scale,
            boolean mirrorX, boolean mirrorY, boolean mirrorZ,
            Face face) {
        super(renderer, textureX, textureY, x, y, z, dx, dy, dz, scale, mirrorX);

        float xMax = x + dx + scale;
        float yMax = y + dy + scale;
        float zMax = z + dz + scale;

        x -= scale;
        y -= scale;
        z -= scale;

        if (mirrorX) {
            float v = xMax;
            xMax = x;
            x = v;
        }

        if (mirrorY) {
            float v = yMax;
            yMax = y;
            y = v;
        }

        if (mirrorZ) {
            float v = zMax;
            zMax = z;
            z = v;
        }

        // w:west e:east d:down u:up s:south n:north
        Vertex wds = vert(x, y, z, 0, 0);
        Vertex eds = vert(xMax, y, z, 0, 8);
        Vertex eus = vert(xMax, yMax, z, 8, 8);
        Vertex wus = vert(x, yMax, z, 8, 0);
        Vertex wdn = vert(x, y, zMax, 0, 0);
        Vertex edn = vert(xMax, y, zMax, 0, 8);
        Vertex eun = vert(xMax, yMax, zMax, 8, 8);
        Vertex wun = vert(x, yMax, zMax, 8, 0);

        if (face == Face.EAST) {
            quad = quad(textureX, dz, textureY, dy, edn, eds, eus, eun);
        }
        if (face == Face.WEST) {
            quad = quad(textureX, dz, textureY, dy, wds, wdn, wun, wus);
        }
        if (face == Face.UP) {
            quad = quad(textureX, dx, textureY, dz, edn, wdn, wds, eds);
        }
        if (face == Face.DOWN) {
            quad = quad(textureX, dx, textureY, dz, eus, wus, wun, eun);
        }
        if (face == Face.SOUTH) {
            quad = quad(textureX, dx, textureY, dy, eds, wds, wus, eus);
        }
        if (face == Face.NORTH) {
            quad = quad(textureX, dx, textureY, dy, wdn, edn, eun, wun);
        }

        if (mirrorX || mirrorY || mirrorZ) {
            quad.flip();
        }
    }

    @Override
    public void render(@Nonnull BufferBuilder buffer, float scale) {
        if (!hidden) {
            quad.render(buffer, scale);
        }
    }
}
