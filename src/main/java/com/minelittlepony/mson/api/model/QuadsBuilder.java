package com.minelittlepony.mson.api.model;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import com.minelittlepony.mson.api.model.Face.Axis;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A builder for creating box quads.
 */
public interface QuadsBuilder {
    static Identifier CONE = new Identifier("mson", "cone");
    static Identifier PLANE = new Identifier("mson", "plane");
    static Identifier CUBE = new Identifier("mson", "cube");

    static int[][] FACE_VERTEX_OFFSETS = new int[][] {
        {}, //Face.NONE
        { 2, 3, 7, 6, 0, 2 }, //Face.UP
        { 5, 4, 0, 1, 0, 2 }, //Face.DOWN
        { 0, 4, 7, 3, 2, 1 }, //Face.WEST
        { 5, 1, 2, 6, 2, 1 }, //Face.EAST
        { 1, 0, 3, 2, 0, 1 }, //Face.NORTH
        { 4, 5, 7, 5, 0, 1 }  //Face.SOUTH
    };
    static int[][] VERTEX_MATRIX = {
        { 0, 0, 0, 0, 0 },
        { 1, 0, 0, 0, 8 },
        { 1, 1, 0, 8, 8 },
        { 0, 1, 0, 0, 8 },
        { 0, 0, 1, 0, 0 },
        { 1, 0, 1, 0, 8 },
        { 1, 1, 1, 8, 8 },
        { 0, 1, 1, 8, 0 }
    };

    static QuadsBuilder BOX = of(CUBE, cone(0)::build);

    /**
     * Otherwise known as a truncated square pyramid.
     *
     * This produces a square polygon with tapered sides ending in a flat top.
     */
    static QuadsBuilder cone(float tipInset) {
        return of(CONE, (ctx, buffer) -> {
            BoxParameters pars = ctx.parameters;
            float xMax = pars.position[0] + pars.size[0] + pars.dilation[0];
            float yMax = pars.position[1] + pars.size[1] + pars.dilation[1];
            float zMax = pars.position[2] + pars.size[2] + pars.dilation[2];

            float xMin = pars.position[0] - pars.dilation[0];
            float yMin = pars.position[1] - pars.dilation[1];
            float zMin = pars.position[2] - pars.dilation[2];

            if (pars.mirror[0]) {
                float v = xMax;
                xMax = xMin;
                xMin = v;
            }

            float tipXmin = xMin + pars.size[0] * tipInset;
            float tipZmin = zMin + pars.size[2] * tipInset;
            float tipXMax = xMax - pars.size[0] * tipInset;
            float tipZMax = zMax - pars.size[2] * tipInset;

            Vert _0 = ctx.vert(tipXmin, yMin, tipZmin, 0, 0);
            Vert _1 = ctx.vert(tipXMax, yMin, tipZmin, 0, 8);
            Vert _2 = ctx.vert(xMax,    yMax, zMin,    8, 8);
            Vert _3 = ctx.vert(xMin,    yMax, zMin,    8, 0);
            Vert _4 = ctx.vert(tipXmin, yMin, tipZMax, 0, 0);
            Vert _5 = ctx.vert(tipXMax, yMin, tipZMax, 0, 8);
            Vert _6 = ctx.vert(xMax,    yMax, zMax,    8, 8);
            Vert _7 = ctx.vert(xMin,    yMax, zMax,    8, 0);

            float u = pars.uv.u();
            float v = pars.uv.v();

            float dX = pars.size[0];
            float dY = pars.size[1];
            float dZ = pars.size[2];

            float j = u;
            float k = u + dZ;
            float l = u + dZ + dX;
            float n = u + dZ + dX + dZ;
            float p = v;
            float q = v + dZ;

            buffer.quad(Direction.DOWN,  k, p, dX,  dZ, _5, _4, _0, _1);
            buffer.quad(Direction.UP,    l, q, dX, -dZ, _2, _3, _7, _6);
            buffer.quad(Direction.WEST,  j, q, dZ,  dY, _0, _4, _7, _3);
            buffer.quad(Direction.NORTH, k, q, dX,  dY, _1, _0, _3, _2);
            buffer.quad(Direction.EAST,  l, q, dZ,  dY, _5, _1, _2, _6);
            buffer.quad(Direction.SOUTH, n, q, dX,  dY, _4, _5, _6, _7);
        });
    }

    /**
     * Creates a single, flat plane aligned to the given face.
     */
    static QuadsBuilder plane(Face face) {
        Function<BoxBuilder, BoxParameters> paramatersFunc = ctx -> {
            BoxParameters pars = ctx.parameters;
            float xMax = pars.position[0] + pars.size[0];
            float yMax = pars.position[1] + pars.size[1];
            float zMax = pars.position[2] + pars.size[2];

            xMax = ctx.fixture.stretchCoordinate(Axis.X, xMax, yMax, zMax, pars.dilation[0]);
            yMax = ctx.fixture.stretchCoordinate(Axis.Y, xMax, yMax, zMax, face.applyFixtures(pars.dilation[1]));
            zMax = ctx.fixture.stretchCoordinate(Axis.Z, xMax, yMax, zMax, pars.dilation[2]);

            float xMin = ctx.fixture.stretchCoordinate(Axis.X, pars.position[0], pars.position[1], pars.position[2], -pars.dilation[0]);
            float yMin = ctx.fixture.stretchCoordinate(Axis.Y, pars.position[0], pars.position[1], pars.position[2], face.applyFixtures(-pars.dilation[1]));
            float zMin = ctx.fixture.stretchCoordinate(Axis.Z, pars.position[0], pars.position[1], pars.position[2], -pars.dilation[2]);

            if (pars.mirror[0]) {
                float v = xMax;
                xMax = xMin;
                xMin = v;
            }

            if (pars.mirror[1]) {
                float v = yMax;
                yMax = yMin;
                yMin = v;
            }

            if (pars.mirror[2]) {
                float v = zMax;
                zMax = zMin;
                zMin = v;
            }

            return new BoxParameters(
                    new float[] { xMin, yMin, zMin },
                    new float[] { xMax - xMin, yMax - yMin, zMax - zMin },
                    new float[3],
                    new Texture(
                            (int)(pars.uv.u() - pars.getBoxFrameUOffset(face.getNormal())),
                            (int)(pars.uv.v() - pars.getBoxFrameVOffset(face.getNormal())),
                            0, 0)
            );
        };

        return of(PLANE, (ctx, buffer) -> {
            BoxParameters pars = paramatersFunc.apply(ctx);
            float[][] positionMatrix = {
                    pars.position,
                    { pars.position[0] + pars.size[0], pars.position[1] + pars.size[1], pars.position[2] + pars.size[2] }
            };
            int[] vertexIndices = FACE_VERTEX_OFFSETS[face.ordinal()];
            buffer.quad(face.getNormal(),
                    ctx.parameters.uv.u(), ctx.parameters.uv.v(),
                    ctx.parameters.size[vertexIndices[4]],
                    ctx.parameters.size[vertexIndices[5]],
                    ctx.parameters.mirror[face.getAxis().ordinal()],
                    ctx.vert(VERTEX_MATRIX[vertexIndices[0]], positionMatrix),
                    ctx.vert(VERTEX_MATRIX[vertexIndices[1]], positionMatrix),
                    ctx.vert(VERTEX_MATRIX[vertexIndices[2]], positionMatrix),
                    ctx.vert(VERTEX_MATRIX[vertexIndices[3]], positionMatrix)
            );
        }, paramatersFunc, ctx -> Set.of(face.getNormal()));
    }

    /**
     * Builds the quads array using the provided box builder.
     */
    void build(BoxBuilder ctx, QuadBuffer buffer);

    Identifier getId();

    default Set<Direction> getFaces(BoxBuilder ctx) {
        return Set.of();
    }

    default BoxParameters getBoxParameters(BoxBuilder ctx) {
        return ctx.parameters;
    }

    static QuadsBuilder of(Identifier id,
            BiConsumer<BoxBuilder, QuadBuffer> constructor,
            Function<BoxBuilder, BoxParameters> parameters,
            Function<BoxBuilder, Set<Direction>> faces) {
        return new QuadsBuilder() {
            @Override
            public void build(BoxBuilder ctx, QuadBuffer buffer) {
                constructor.accept(ctx, buffer);
            }

            @Override
            public Identifier getId() {
                return id;
            }

            @Override
            public Set<Direction> getFaces(BoxBuilder ctx) {
                return faces.apply(ctx);
            }

            @Override
            public BoxParameters getBoxParameters(BoxBuilder ctx) {
                return parameters.apply(ctx);
            }
        };
    }

    static QuadsBuilder of(Identifier id, BiConsumer<BoxBuilder, QuadBuffer> constructor) {
        return of(id, constructor, ctx -> ctx.parameters, ctx -> BoxBuilder.ALL_DIRECTIONS);
    }

    interface QuadBuffer {

        boolean getDefaultMirror();

        default void quad(Direction direction, float u, float v, float w, float h, boolean mirror, boolean remap, Vert ...vertices) {
            quad(u, v, w, h, direction, mirror, remap, null, vertices);
        }

        default void quad(Direction direction, float u, float v, float w, float h, boolean mirror, Vert ...vertices) {
            quad(direction, u, v, w, h, mirror, true, vertices);
        }

        default void quad(Direction direction, float u, float v, float w, float h, Vert ...vertices) {
            quad(direction, u, v, w, h, getDefaultMirror(), vertices);
        }

        void quad(float u, float v, float w, float h, Direction direction, boolean mirror, boolean remap, @Nullable Quaternionf rotation, Vert ...vertices);

    }
}
