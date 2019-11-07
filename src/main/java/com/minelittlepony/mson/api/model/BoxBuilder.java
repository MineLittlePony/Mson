package com.minelittlepony.mson.api.model;

import net.minecraft.client.model.Box;
import net.minecraft.client.model.Cuboid;
import net.minecraft.client.model.Quad;
import net.minecraft.client.model.Vertex;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.model.Face.Axis;

public final class BoxBuilder {

    public final MsonCuboid cuboid;

    public float xMin;
    public float yMin;
    public float zMin;

    public int dx;
    public int dy;
    public int dz;

    public int texU;
    public int texV;

    public float scale;

    public boolean mirrorX;
    public boolean mirrorY;
    public boolean mirrorZ;

    public BoxBuilder(ModelContext context) {
        this.cuboid = (MsonCuboid)context.getContext();

        scale = context.getScale();

        texU = cuboid.getTextureOffsetU();
        texV = cuboid.getTextureOffsetV();

        mirrorX = cuboid.getMirrorX();
        mirrorY = cuboid.getMirrorY();
        mirrorZ = cuboid.getMirrorZ();
    }

    public BoxBuilder pos(float... pos) {
        xMin = pos[0] + cuboid.getModelOffsetX();
        yMin = pos[1] + cuboid.getModelOffsetY();
        zMin = pos[2] + cuboid.getModelOffsetZ();
        return this;
    }

    public BoxBuilder size(int... size) {
        dx = size[0];
        dy = size[1];
        dz = size[2];
        return this;
    }

    public BoxBuilder size(Axis axis, int...dimension) {
        return size(
                axis.getWidth(dimension),
                axis.getHeight(dimension),
                axis.getDeptch(dimension));
    }

    public BoxBuilder stretch(float stretch) {
        scale =+ stretch;
        return this;
    }

    public BoxBuilder mirror(Axis axis, boolean mirror) {
        if (axis == Axis.X) {
            mirrorX = mirror;
        }
        if (axis == Axis.Y) {
            mirrorY = mirror;
        }
        if (axis == Axis.Z) {
            mirrorZ = mirror;
        }
        return this;
    }

    /**
     * Creates a new vertex mapping the given (x, y, z) coordinates to a texture offset.
     */
    public Vertex vert(float x, float y, float z, int texX, int texY) {
        return new Vertex(x, y, z, texX, texY);
    }

    /**
     * Creates a new quad with the given spatial vertices.
     */
    public Quad quad(
            int startX, int width,
            int startY, int height, Vertex ...verts) {
        return new Quad(verts,
                startX,         startY,
                startX + width, startY + height,
                texU, texV);
    }

    public Box build() {
        return new Box((Cuboid)cuboid,
                texU, texV,
                xMin, yMin, zMin,
                dx, dy, dz,
                scale, cuboid.getMirrorX());
    }

    public Box build(QuadsBuilder builder) {
        Box box = build();
        ((PolygonsSetter)box).setPolygons(builder.build(this));
        return box;
    }

    public interface PolygonsSetter {
        void setPolygons(Quad[] quads);
    }
}
