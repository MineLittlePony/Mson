package com.minelittlepony.mson.api.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.model.Face.Axis;
import com.minelittlepony.mson.util.VectorUtil;
import com.mojang.datafixers.util.Pair;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

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

    public ParametersTransformation coordinateSpace = ParametersTransformation.UNIT;

    public BoxBuilder(PartBuilder parent) {
        this.parent = parent;
    }

    public BoxBuilder(ModelContext context) {
        this.parent = context.<PartBuilder>getThis();

        dilate(context.getLocals().getDilation());
        tex(parent.texture);

        System.arraycopy(parent.mirror, 0, parameters.mirror, 0, 3);
    }

    public BoxBuilder coordinateSpace(ParametersTransformation coordinateSpace) {
        this.coordinateSpace = coordinateSpace;
        return this;
    }

    public BoxBuilder fix(CoordinateFixture fixture) {
        this.fixture = fixture;
        return this;
    }

    public BoxBuilder pos(float... pos) {
        VectorUtil.copy(pos, parameters.position);
        return this;
    }

    public BoxBuilder tex(Texture tex) {
        parameters.uv = tex;
        return this;
    }

    public BoxBuilder size(float... size) {
        VectorUtil.copy(size, parameters.size);
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
        VectorUtil.apply(dilate, parameters.dilation, VectorUtil.VecFunc.SUM);
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
        return (Vert)(Object)new ModelPart.Vertex(x, y, z, u, v);
    }

    public Vert vert(int[] parameters, float[][] positionMatrix) {
        return (Vert)(Object)new ModelPart.Vertex(
                positionMatrix[parameters[0]][0], positionMatrix[parameters[1]][1], positionMatrix[parameters[2]][2],
                parameters[3], parameters[4]
        );
    }

    public BoxBuilder quads(QuadsBuilder quads) {
        this.quads = quads;
        return this;
    }

    public ModelPart.Cube build() {
        this.coordinateSpace.getBoxParameters(this);

        if (quads.getId() == QuadsBuilder.CUBE) {
            return quads.getBoxParameters(this).build(parent, quads.getFaces(this));
        }

        BoxParameters pars = quads.getBoxParameters(this);
        ModelPart.Cube box = pars.build(parent, quads.getFaces(this));
        ((Cube)box).setSides(collectQuads(pars).map(Quad::rect).toArray(Rect[]::new));
        return box;
    }

    public Stream<Quad> collectQuads() {
        this.coordinateSpace.getBoxParameters(this);
        return collectQuads(this.quads.getBoxParameters(this));
    }

    private Stream<Quad> collectQuads(BoxParameters pars) {
        List<Quad> quads = new ArrayList<>();
        this.quads.build(pars, this, new QuadsBuilder.QuadBuffer() {
            @Override
            public boolean getDefaultMirror() {
                return parameters.mirror[0];
            }

            @Override
            public void quad(Direction direction, float u, float v, float w, float h, boolean mirror, boolean preserveNormal, @Nullable Quaternionf rotation, Vert... vertices) {
                Rect.remapUVs(vertices, u, v, u + w, v + h, parent.texture.width(), parent.texture.height());
                quad(new Quad(new ModelPart.Polygon(
                        (ModelPart.Vertex[])Rect.copyVertexArray(new ModelPart.Vertex[vertices.length], vertices, mirror, rotation),
                        Rect.mirrorFacing(!preserveNormal && mirror, direction).getUnitVec3f()
                ), direction));
            }

            @Override
            public void quad(Quad quad) {
                quads.add(quad);
            }
        });
        return quads.stream();
    }

    public static BoxBuilder union(ModelContext context, Stream<Pair<Face, BoxBuilder>> boxes, Identifier id) {
        var range = new Object() {
            float[] min;
            float[] max;
        };

        var faces = new HashSet<Direction>();
        var vertices = boxes.map(pair -> {
            faces.add(pair.getFirst().getNormal());
            return pair.getSecond();
        }).map(plane -> {
            float[] min = plane.parameters.position;
            float[] max = VectorUtil.create(i -> min[i] + plane.parameters.size[i]);
            range.min = VectorUtil.applyIfPresent(min, range.min, VectorUtil.VecFunc.MIN);
            range.max = VectorUtil.applyIfPresent(max, range.max, VectorUtil.VecFunc.MAX);
            return plane;
        }).toList();

        return new BoxBuilder(context)
                .pos(range.min)
                .size(range.max[0] - range.min[0], range.max[1] - range.min[1], range.max[2] - range.min[2])
                .quads(new QuadsBuilder() {
            @Override
            public void build(BoxParameters params, BoxBuilder ctx, QuadBuffer buffer) {
                vertices.forEach(box -> {
                    box.coordinateSpace(ctx.coordinateSpace).collectQuads().forEach(buffer::quad);
                });
            }

            @Override
            public Set<Direction> getFaces(BoxBuilder ctx) {
                return faces;
            }

            @Override
            public BoxParameters getBoxParameters(BoxBuilder ctx) {
                return ctx.parameters;
            }

            @Override
            public Identifier getId() {
                return id;
            }
        });
    }

    public interface RenderLayerSetter {
        Function<Identifier, RenderType> getRenderLayerFactory();

        void setRenderLayerFactory(Function<Identifier, RenderType> supplier);
    }

    public record Quad(Rect rect, Direction direction) {}
}
