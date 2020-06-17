package com.minelittlepony.mson.api.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.model.Face.Axis;
import com.minelittlepony.mson.util.TriState;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * A builder for building boxes.
 *
 * Holds all the parameters so we don't have to shove them into a Box sub-class.
 *
 */
public final class BoxBuilder {
    public final MsonPart part;

    public float x;
    public float y;
    public float z;

    public float dx;
    public float dy;
    public float dz;

    public int u;
    public int v;

    public float stretchX;
    public float stretchY;
    public float stretchZ;

    public boolean mirrorX;
    public boolean mirrorY;
    public boolean mirrorZ;

    public CoordinateFixture fixture = CoordinateFixture.unfixed();

    public BoxBuilder(ModelPart part) {
        this.part = (MsonPart)part;
    }
    public BoxBuilder(ModelContext context) {
        this((ModelPart)context.getContext());

        stretchX = context.getScale();
        stretchY = context.getScale();
        stretchZ = context.getScale();

        u = part.getTexture().getU();
        v = part.getTexture().getV();

        mirrorX = part.getMirrorX();
        mirrorY = part.getMirrorY();
        mirrorZ = part.getMirrorZ();
    }

    public BoxBuilder fix(CoordinateFixture fixture) {
        this.fixture = fixture;
        return this;
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

    public BoxBuilder size(float... size) {
        dx = size[0];
        dy = size[1];
        dz = size[2];
        return this;
    }

    public BoxBuilder size(Axis axis, float...dimension) {
        return size(
                axis.getWidth().getFloat(dimension),
                axis.getHeight().getFloat(dimension),
                axis.getDeptch().getFloat(dimension)
        );
    }

    public BoxBuilder stretch(float... stretch) {
        this.stretchX += stretch[0];
        this.stretchY += stretch[1];
        this.stretchZ += stretch[2];
        return this;
    }

    public BoxBuilder mirror(Axis axis, boolean... mirror) {
        mirrorX = axis.getWidth().getBoolean(mirror);
        mirrorY = axis.getHeight().getBoolean(mirror);
        mirrorZ = axis.getDeptch().getBoolean(mirror);
        return this;
    }

    public BoxBuilder mirror(Axis axis, TriState mirror) {
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
        return (Vert)new ModelPart.Vertex(x, y, z, u, v);
    }

    /**
     * Creates a new quad with the given spatial vertices.
     */
    public Rect quad(
            float u, float v,
            float w, float h,
            Direction direction,
            Vert ...vertices) {
        return quad(u, v, w, h, direction, part.getMirrorX(), vertices);
    }

    /**
     * Creates a new quad with the given spatial vertices.
     */
    public Rect quad(
            float u, float v,
            float w, float h,
            Direction direction,
            boolean mirror,
            Vert ...vertices) {

        ModelPart.Vertex[] verts = new ModelPart.Vertex[vertices.length];
        System.arraycopy(vertices, 0, verts, 0, vertices.length);

        return (Rect)new ModelPart.Quad(
                verts,
                u,         v,
                u + w, v + h,
                part.getTexture().getWidth(), part.getTexture().getHeight(),
                mirror,
                direction);
    }

    public Cuboid build() {
        return new Cuboid(
                u, v,
                part.getModelOffsetX() + x, part.getModelOffsetY() + y, part.getModelOffsetZ() + z,
                dx, dy, dz,
                stretchX, stretchY, stretchZ,
                part.getMirrorX(),
                part.getTexture().getWidth(), part.getTexture().getHeight());
    }

    public Cuboid build(QuadsBuilder builder) {
        Cuboid box = build();
        ((Cube)box).setSides(builder.build(this));
        return box;
    }

    public interface ContentAccessor {
        List<Cuboid> cubes();

        List<ModelPart> children();
    }

    public interface RenderLayerSetter {
        Function<Identifier, RenderLayer> getRenderLayerFactory();

        void setRenderLayerFactory(Function<Identifier, RenderLayer> supplier);
    }
}
