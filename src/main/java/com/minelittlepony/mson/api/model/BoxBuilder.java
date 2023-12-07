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

    public BoxParameters parameters = new BoxParameters();

    public CoordinateFixture fixture = CoordinateFixture.unfixed();

    public QuadsBuilder quads = QuadsBuilder.BOX;

    public BoxBuilder(PartBuilder parent) {
        this.parent = parent;
    }

    public BoxBuilder(ModelContext context) {
        this.parent = context.<PartBuilder>getThis();

        dilate(context.getLocals().getDilation());
        tex(parent.texture);

        System.arraycopy(parent.mirror, 0, parameters.mirror, 0, 3);
    }

    public BoxBuilder fix(CoordinateFixture fixture) {
        this.fixture = fixture;
        return this;
    }

    public BoxBuilder pos(float... pos) {
        System.arraycopy(pos, 0, parameters.position, 0, 3);
        return this;
    }

    public BoxBuilder tex(Texture tex) {
        parameters.uv = tex;
        return this;
    }

    public BoxBuilder size(float... size) {
        System.arraycopy(size, 0, parameters.size, 0, 3);
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
        parameters.dilation[0] += dilate[0];
        parameters.dilation[1] += dilate[1];
        parameters.dilation[2] += dilate[2];
        return this;
    }

    public BoxBuilder mirror(Axis axis, boolean... mirror) {
        parameters.mirror[0] = axis.getWidth().getBoolean(mirror);
        parameters.mirror[1] = axis.getHeight().getBoolean(mirror);
        parameters.mirror[2] = axis.getDepth().getBoolean(mirror);
        return this;
    }

    public BoxBuilder mirror(Axis axis, Optional<Boolean> mirror) {
        mirror.ifPresent(m -> parameters.mirror[axis.ordinal()] = m);
        return this;
    }

    public Vert vert(float x, float y, float z, int u, int v) {
        return (Vert)new ModelPart.Vertex(x, y, z, u, v);
    }

    public Vert vert(int[] parameters, float[][] positionMatrix) {
        return (Vert)new ModelPart.Vertex(
                positionMatrix[parameters[0]][0], positionMatrix[parameters[1]][1], positionMatrix[parameters[2]][2],
                parameters[3], parameters[4]
        );
    }

    public BoxBuilder quads(QuadsBuilder quads) {
        this.quads = quads;
        return this;
    }

    public Cuboid build() {
        if (quads.getId() == QuadsBuilder.CUBE) {
            return quads.getBoxParameters(this).build(parent, quads.getFaces(this));
        }

        BoxParameters pars = quads.getBoxParameters(this);
        Cuboid box = pars.build(parent, quads.getFaces(this));
        ((Cube)box).setSides(collectQuads(pars).stream().map(Quad::rect).toArray(Rect[]::new));
        return box;
    }

    public List<Quad> collectQuads() {
        return collectQuads(this.quads.getBoxParameters(this));
    }

    private List<Quad> collectQuads(BoxParameters pars) {
        List<Quad> quads = new ArrayList<>();
        this.quads.build(pars, this, new QuadsBuilder.QuadBuffer() {
            private final ModelPart.Vertex emptyVertex = new ModelPart.Vertex(0, 0, 0, 0, 0);
            private final ModelPart.Vertex[] defaultVertices = {emptyVertex, emptyVertex, emptyVertex, emptyVertex};

            @Override
            public boolean getDefaultMirror() {
                return parameters.mirror[0];
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

                quads.add(new Quad(rect, direction));
            }
        });
        return quads;
    }

    public interface RenderLayerSetter {
        Function<Identifier, RenderLayer> getRenderLayerFactory();

        void setRenderLayerFactory(Function<Identifier, RenderLayer> supplier);
    }

    public record Quad(Rect rect, Direction direction) {}
}
