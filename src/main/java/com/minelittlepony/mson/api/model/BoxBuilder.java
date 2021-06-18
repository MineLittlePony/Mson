package com.minelittlepony.mson.api.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.model.Face.Axis;

import java.util.Optional;
import java.util.function.Function;

/**
 * A builder for building boxes.
 *
 * Holds all the parameters so we don't have to shove them into a Box sub-class.
 */
public final class BoxBuilder {

    public final PartBuilder parent;

    public float[] pos = new float[3];
    public float[] size = new float[3];
    public float[] dilate = new float[3];

    public int u, v;

    public boolean[] mirror = new boolean[3];

    public CoordinateFixture fixture = CoordinateFixture.unfixed();

    public BoxBuilder(PartBuilder parent) {
        this.parent = parent;
    }
    public BoxBuilder(ModelContext context) {
        this((PartBuilder)context.getContext());

        dilate(context.getLocals().getDilation().getNow(new float[3]));

        u = parent.texture.u();
        v = parent.texture.v();

        System.arraycopy(parent.mirror, 0, mirror, 0, 3);
    }

    public BoxBuilder fix(CoordinateFixture fixture) {
        this.fixture = fixture;
        return this;
    }

    public BoxBuilder pos(float... pos) {
        this.pos[0] = pos[0] + parent.offset[0];
        this.pos[1] = pos[1] + parent.offset[1];
        this.pos[2] = pos[2] + parent.offset[2];
        return this;
    }

    public BoxBuilder tex(Texture tex) {
        u = tex.u();
        v = tex.v();
        return this;
    }

    public BoxBuilder size(float... size) {
        System.arraycopy(size, 0, this.size, 0, 3);
        return this;
    }

    public BoxBuilder size(Axis axis, float...dimension) {
        return size(
                axis.getWidth().getFloat(dimension),
                axis.getHeight().getFloat(dimension),
                axis.getDepth().getFloat(dimension)
        );
    }

    public BoxBuilder dilate(float... dilate) {
        this.dilate[0] += dilate[0];
        this.dilate[1] += dilate[1];
        this.dilate[2] += dilate[2];
        return this;
    }

    public BoxBuilder mirror(Axis axis, boolean... mirror) {
        this.mirror[0] = axis.getWidth().getBoolean(mirror);
        this.mirror[1] = axis.getHeight().getBoolean(mirror);
        this.mirror[2] = axis.getDepth().getBoolean(mirror);
        return this;
    }

    public BoxBuilder mirror(Axis axis, Optional<Boolean> mirror) {
        mirror.ifPresent(m -> this.mirror[axis.ordinal()] = m);
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
                parent.texture.width(), parent.texture.height(),
                mirror,
                direction);
    }

    public Cuboid build() {
        // TODO: Offset is doubled here
        return new Cuboid(
                u, v,
                parent.offset[0] + pos[0], parent.offset[1] + pos[1], parent.offset[2] + pos[2],
                size[0], size[1], size[2],
                dilate[0], dilate[1], dilate[2],
                mirror[0],
                parent.texture.width(), parent.texture.height());
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
