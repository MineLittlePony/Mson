package com.minelittlepony.mson.api.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.*;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.model.Face.Axis;
import com.minelittlepony.mson.impl.invoke.MethodHandles;
import com.minelittlepony.mson.util.Qbit;

import java.util.List;
import java.util.Optional;

/**
 * A builder for building boxes.
 *
 * Holds all the parameters so we don't have to shove them into a Box sub-class.
 *
 */
public final class BoxBuilder {

    private static final RectFactory RECT_FACTORY = MethodHandles.lookupInvoker(RectFactory.class, MethodHandles.findHiddenInnerClass(ModelPart.class, Rect.class));
    private static final VertFactory VERT_FACTORY = MethodHandles.lookupInvoker(VertFactory.class, MethodHandles.findHiddenInnerClass(ModelPart.class, Vert.class));

    public final MsonPart part;

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
        this.part = (MsonPart)context.getContext();

        stretch = context.getScale();

        u = part.getTextureOffsetU();
        v = part.getTextureOffsetV();

        mirrorX = part.getMirrorX();
        mirrorY = part.getMirrorY();
        mirrorZ = part.getMirrorZ();
    }

    public BoxBuilder pos(float... pos) {
        x = pos[0] + part.getModelOffsetX();
        y = pos[1] + part.getModelOffsetY();
        z = pos[2] + part.getModelOffsetZ();
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
    public Vert vert(float x, float y, float z, int u, int v) {
        return VERT_FACTORY.create(x, y, z, u, v);
    }

    /**
     * Creates a new quad with the given spatial vertices.
     */
    public Rect quad(
            int x, int width,
            int y, int height, Vert ...vertices) {
        return RECT_FACTORY.create(vertices,
                x,         y,
                x + width, y + height,
                u, v);
    }

    public Cuboid build() {
        return new Cuboid(
                u, v,
                x, y, z,
                dx, dy, dz,
                stretch, stretch, stretch,
                part.getMirrorX(),
                part.getTextureOffsetU(), part.getTextureOffsetV());
    }

    public Cuboid build(QuadsBuilder builder) {
        Cuboid box = build();
        ((PolygonsSetter)box).setPolygons(builder.build(this));
        return box;
    }

    @FunctionalInterface
    interface VertFactory {
        Vert create(float x, float y, float z, float u, float v);
    }

    @FunctionalInterface
    interface RectFactory {
        Rect create(Vert[] vertices, float u1, float v1, float u2, float v2, float squishU, float squishV);
    }

    public interface PolygonsSetter {
        void setPolygons(Rect[] quads);
    }

    public interface ContentAccessor {
        List<Cuboid> cubes();

        List<ModelPart> children();
    }
}
