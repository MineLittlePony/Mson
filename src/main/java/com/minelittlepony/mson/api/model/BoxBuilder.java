package com.minelittlepony.mson.api.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.model.Face.Axis;
import com.minelittlepony.mson.util.TriState;

import java.util.Optional;
import java.util.function.Function;

/**
 * A builder for building boxes.
 *
 * Holds all the parameters so we don't have to shove them into a Box sub-class.
 *
 */
public final class BoxBuilder {

    public final PartBuilder parent;

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

    public boolean[] mirror = new boolean[3];

    public CoordinateFixture fixture = CoordinateFixture.unfixed();

    public BoxBuilder(PartBuilder parent) {
        this.parent = parent;
    }
    public BoxBuilder(ModelContext context) {
        this((PartBuilder)context.getContext());

        stretchX = context.getScale();
        stretchY = context.getScale();
        stretchZ = context.getScale();

        u = parent.texture.getU();
        v = parent.texture.getV();

        System.arraycopy(parent.mirror, 0, mirror, 0, 3);
    }

    public BoxBuilder fix(CoordinateFixture fixture) {
        this.fixture = fixture;
        return this;
    }

    public BoxBuilder pos(float... pos) {
        x = pos[0] + parent.offset[0];
        y = pos[1] + parent.offset[1];
        z = pos[2] + parent.offset[2];
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
        this.mirror[0] = axis.getWidth().getBoolean(mirror);
        this.mirror[1] = axis.getHeight().getBoolean(mirror);
        this.mirror[2] = axis.getDeptch().getBoolean(mirror);
        return this;
    }

    public BoxBuilder mirror(Axis axis, TriState mirror) {
        if (mirror.isKnown()) {
            if (axis == Axis.X) {
                this.mirror[0] = mirror.toBoolean();
            }
            if (axis == Axis.Y) {
                this.mirror[1] = mirror.toBoolean();
            }
            if (axis == Axis.Z) {
                this.mirror[2] = mirror.toBoolean();
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
        return quad(u, v, w, h, direction, mirror[0], vertices);
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
                parent.texture.getWidth(), parent.texture.getHeight(),
                mirror,
                direction);
    }

    public Cuboid build() {
        return new Cuboid(
                u, v,
                parent.offset[0] + x, parent.offset[1] + y, parent.offset[2] + z,
                dx, dy, dz,
                stretchX, stretchY, stretchZ,
                mirror[0],
                parent.texture.getWidth(), parent.texture.getHeight());
    }

    public Cuboid build(QuadsBuilder builder) {
        Cuboid box = build();
        ((Cube)box).setSides(builder.build(this));
        return box;
    }

    public interface RenderLayerSetter {
        Function<Identifier, RenderLayer> getRenderLayerFactory();

        void setRenderLayerFactory(Function<Identifier, RenderLayer> supplier);
    }
}
