package com.minelittlepony.mson.api.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.model.Face.Axis;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * A builder for building boxes.
 *
 * Holds all the parameters so we don't have to shove them into a Box sub-class.
 */
public final class BoxBuilder {
    public static final Set<Direction> ALL_DIRECTIONS = EnumSet.allOf(Direction.class);

    public final PartBuilder parent;

    public float[] pos = new float[3];
    public float[] size = new float[3];
    public float[] dilate = new float[3];

    public int u, v;

    public boolean[] mirror = new boolean[3];

    public CoordinateFixture fixture = CoordinateFixture.unfixed();

    @Nullable
    public QuadsBuilder quads;

    public BoxBuilder(PartBuilder parent) {
        this.parent = parent;
    }

    public BoxBuilder(ModelContext context) {
        this.parent = context.<PartBuilder>getThis();

        dilate(context.getLocals().getDilation());

        u = parent.texture.u();
        v = parent.texture.v();

        System.arraycopy(parent.mirror, 0, mirror, 0, 3);
    }

    public BoxBuilder fix(CoordinateFixture fixture) {
        this.fixture = fixture;
        return this;
    }

    public BoxBuilder pos(float... pos) {
        System.arraycopy(pos, 0, this.pos, 0, 3);
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

    public Vert vert(float x, float y, float z, int u, int v) {
        return (Vert)new ModelPart.Vertex(x, y, z, u, v);
    }

    public BoxBuilder quads(QuadsBuilder quads) {
        this.quads = quads;
        return this;
    }

    public Cuboid build(Set<Direction> enabledSides) {
        return new Cuboid(
                u, v,
                pos[0], pos[1], pos[2],
                size[0], size[1], size[2],
                dilate[0], dilate[1], dilate[2],
                mirror[0],
                parent.texture.width(), parent.texture.height(),
                enabledSides
        );
    }

    public Cuboid build() {
        if (quads.getId() == QuadsBuilder.CUBE) {
            return build(ALL_DIRECTIONS);
        }

        Cuboid box = build(Set.of());
        ((Cube)box).setSides(collectQuads().toArray(Rect[]::new));
        return box;
    }

    public List<Rect> collectQuads() {
        List<Rect> quads = new ArrayList<>();
        this.quads.build(this, new QuadsBuilder.QuadBuffer() {
            private final ModelPart.Vertex emptyVertex = new ModelPart.Vertex(0, 0, 0, 0, 0);
            private final ModelPart.Vertex[] defaultVertices = {emptyVertex, emptyVertex, emptyVertex, emptyVertex};

            @Override
            public boolean getDefaultMirror() {
                return mirror[0];
            }

            @Override
            public void quad(float u, float v, float w, float h, Direction direction, boolean mirror, boolean remap, @Nullable Quaternionf rotation, Vert... vertices) {
                ModelPart.Vertex[] verts = new ModelPart.Vertex[vertices.length];
                System.arraycopy(vertices, 0, verts, 0, vertices.length);

                Rect rect = (Rect)new ModelPart.Quad(
                        remap ? verts : defaultVertices,
                        u,         v,
                        u + w, v + h,
                        parent.texture.width(), parent.texture.height(),
                        mirror,
                        direction);
                if (!remap) {
                    rect.setVertices(mirror, vertices);
                }
                if (rotation != null) {
                    rect.rotate(rotation);
                }

                quads.add(rect);
            }
        });
        return quads;
    }

    public interface RenderLayerSetter {
        Function<Identifier, RenderLayer> getRenderLayerFactory();

        void setRenderLayerFactory(Function<Identifier, RenderLayer> supplier);
    }
}
