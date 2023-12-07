package com.minelittlepony.mson.api.model;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import com.minelittlepony.mson.api.model.Face.Axis;

import java.util.Set;
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
        { 4, 5, 6, 7, 0, 1 }  //Face.SOUTH
    };
    static int[][] VERTEX_MATRIX = {
        { 0, 0, 0, 0, 0 },
        { 1, 0, 0, 0, 8 },
        { 1, 1, 0, 8, 8 },
        { 0, 1, 0, 8, 0 },
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
        return of(CONE, (pars, ctx, buffer) -> {
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

            float tipXmin = xMin + ctx.parameters.size[0] * tipInset;
            float tipZmin = zMin + ctx.parameters.size[2] * tipInset;
            float tipXMax = xMax - ctx.parameters.size[0] * tipInset;
            float tipZMax = zMax - ctx.parameters.size[2] * tipInset;

            Vert _0 = ctx.vert(tipXmin, yMin, tipZmin, 0, 0);
            Vert _1 = ctx.vert(tipXMax, yMin, tipZmin, 0, 8);
            Vert _2 = ctx.vert(xMax,    yMax, zMin,    8, 8);
            Vert _3 = ctx.vert(xMin,    yMax, zMin,    8, 0);
            Vert _4 = ctx.vert(tipXmin, yMin, tipZMax, 0, 0);
            Vert _5 = ctx.vert(tipXMax, yMin, tipZMax, 0, 8);
            Vert _6 = ctx.vert(xMax,    yMax, zMax,    8, 8);
            Vert _7 = ctx.vert(xMin,    yMax, zMax,    8, 0);

            float u = ctx.parameters.uv.u();
            float v = ctx.parameters.uv.v();

            float dX = ctx.parameters.size[0];
            float dY = ctx.parameters.size[1];
            float dZ = ctx.parameters.size[2];

            //        | up    | down |
            // | west | north | east | south |

            float col1 = u;
            float col2 = col1 + dZ;
            float col3 = col2 + dX;
            float col4 = col3 + dZ;

            float row1 = v;
            float row2 = row1 + dZ;

            buffer.quad(Direction.UP,    col3, row2, dX, -dZ, _2, _3, _7, _6);
            buffer.quad(Direction.DOWN,  col2, row1, dX,  dZ, _5, _4, _0, _1);
            buffer.quad(Direction.WEST,  col1, row2, dZ,  dY, _0, _4, _7, _3);
            buffer.quad(Direction.EAST,  col3, row2, dZ,  dY, _5, _1, _2, _6);
            buffer.quad(Direction.NORTH, col2, row2, dX,  dY, _1, _0, _3, _2);
            buffer.quad(Direction.SOUTH, col4, row2, dX,  dY, _4, _5, _6, _7);
        });
    }

    /**
     * Creates a single, flat plane aligned to the given face.
     */
    static QuadsBuilder plane(Face face) {
        return of(PLANE, (pars, ctx, buffer) -> {
            final float[][] positionMatrix = {
                    pars.position,
                    { pars.position[0] + pars.size[0], pars.position[1] + pars.size[1], pars.position[2] + pars.size[2] }
            };
            final int[] vertexIndices = FACE_VERTEX_OFFSETS[face.ordinal()];
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
        }, ctx -> {
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

            pars = new BoxParameters(
                    new float[] { xMin, yMin, zMin },
                    new float[] { xMax - xMin, yMax - yMin, zMax - zMin },
                    new float[3],
                    pars.uv
            );
            if (pars.mirror[0]) pars.flip(Axis.X);
            if (pars.mirror[1]) pars.flip(Axis.Y);
            if (pars.mirror[2]) pars.flip(Axis.Z);
            pars.uv = new Texture(
                    (int)((pars.uv.u() - pars.getBoxFrameUOffset(face.getNormal()))),
                    (int)((pars.uv.v() - pars.getBoxFrameVOffset(face.getNormal()))),
                    pars.uv.width(), pars.uv.height()
            );

            return pars;
        }, ctx -> Set.of(face.getNormal()));
    }

    /**
     * Builds the quads array using the provided box builder.
     */
    void build(BoxParameters params, BoxBuilder ctx, QuadBuffer buffer);

    Identifier getId();

    default Set<Direction> getFaces(BoxBuilder ctx) {
        return Set.of();
    }

    default BoxParameters getBoxParameters(BoxBuilder ctx) {
        return ctx.parameters;
    }

    static QuadsBuilder of(Identifier id,
            QuadGenerator constructor,
            Function<BoxBuilder, BoxParameters> parameters,
            Function<BoxBuilder, Set<Direction>> faces) {
        return new QuadsBuilder() {
            @Override
            public void build(BoxParameters params, BoxBuilder ctx, QuadBuffer buffer) {
                constructor.accept(params, ctx, buffer);
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

    static QuadsBuilder of(Identifier id, QuadGenerator constructor) {
        return of(id, constructor, ctx -> ctx.parameters, ctx -> BoxBuilder.ALL_DIRECTIONS);
    }

    interface QuadGenerator {
        void accept(BoxParameters params, BoxBuilder builder, QuadBuffer buffer);
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
