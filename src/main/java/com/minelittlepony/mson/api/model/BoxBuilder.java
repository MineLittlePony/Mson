package com.minelittlepony.mson.api.model;

import net.minecraft.client.model.Box;
import net.minecraft.client.model.Cuboid;
import net.minecraft.client.model.Quad;
import net.minecraft.client.model.Vertex;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.model.Face.Axis;
import com.minelittlepony.mson.util.Qbit;

import java.util.Optional;

/**
 * A builder for building boxes.
 *
 * Holds all the parameters so we don't have to shove them into a Box sub-class.
 *
 */
public final class BoxBuilder {

    public final MsonCuboid cuboid;

    public float x;
    public float y;
    public float z;

    public int dx;
    public int dy;
    public int dz;

    public int u;
    public int v;

    public float stretch;

    public boolean mirrorX;
    public boolean mirrorY;
    public boolean mirrorZ;

    public BoxBuilder(ModelContext context) {
        this.cuboid = (MsonCuboid)context.getContext();

        stretch = context.getScale();

        u = cuboid.getTextureOffsetU();
        v = cuboid.getTextureOffsetV();

        mirrorX = cuboid.getMirrorX();
        mirrorY = cuboid.getMirrorY();
        mirrorZ = cuboid.getMirrorZ();
    }

    public BoxBuilder pos(float... pos) {
        x = pos[0] + cuboid.getModelOffsetX();
        y = pos[1] + cuboid.getModelOffsetY();
        z = pos[2] + cuboid.getModelOffsetZ();
        return this;
    }

    public BoxBuilder tex(Optional<Texture> tex) {
        tex.ifPresent(t -> {
            u = t.getU();
            v = t.getV();
        });
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
        this.stretch =+ stretch;
        return this;
    }

    public BoxBuilder mirror(Axis axis, Qbit mirror) {
        if (mirror.isKnown()) {
            if (axis == Axis.X) {
                mirrorX = mirror.toBoolean();
            }
            if (axis == Axis.Y) {
                mirrorY = mirror.toBoolean();
            }
            if (axis == Axis.Z) {
                mirrorZ = mirror.toBoolean();
            }
        }
        return this;
    }

    /**
     * Creates a new vertex mapping the given (x, y, z) coordinates to a texture offset.
     */
    public Vertex vert(float x, float y, float z, int u, int v) {
        return new Vertex(x, y, z, u, v);
    }

    /**
     * Creates a new quad with the given spatial vertices.
     */
    public Quad quad(
            int x, int width,
            int y, int height, Vertex ...vertices) {
        return new Quad(vertices,
                x,         y,
                x + width, y + height,
                u, v);
    }

    public Box build() {
        return new Box((Cuboid)cuboid,
                u, v,
                x, y, z,
                dx, dy, dz,
                stretch, cuboid.getMirrorX());
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
