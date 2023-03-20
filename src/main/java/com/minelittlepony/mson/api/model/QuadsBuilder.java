package com.minelittlepony.mson.api.model;

import net.minecraft.util.math.Direction;

import com.minelittlepony.mson.api.model.Face.Axis;

/**
 * A builder for creating box quads.
 */
public interface QuadsBuilder {

    /**
     * Otherwise known as a truncated square pyramid.
     *
     * This produces a square polygon with tapered sides ending in a flat top.
     */
    static QuadsBuilder cone(float tipInset) {
        return ctx -> {
            float xMax = ctx.pos[0] + ctx.size[0] + ctx.dilate[0];
            float yMax = ctx.pos[1] + ctx.size[1] + ctx.dilate[1];
            float zMax = ctx.pos[2] + ctx.size[2] + ctx.dilate[2];

            float xMin = ctx.pos[0] - ctx.dilate[0];
            float yMin = ctx.pos[1] - ctx.dilate[1];
            float zMin = ctx.pos[2] - ctx.dilate[2];

            if (ctx.mirror[0]) {
                float v = xMax;
                xMax = xMin;
                xMin = v;
            }

            float tipXmin = xMin + ctx.size[0] * tipInset;
            float tipZmin = zMin + ctx.size[2] * tipInset;
            float tipXMax = xMax - ctx.size[0] * tipInset;
            float tipZMax = zMax - ctx.size[2] * tipInset;

            // w:west e:east d:down u:up s:south n:north
            Vert wds = ctx.vert(tipXmin, yMin, tipZmin, 0, 0);
            Vert eds = ctx.vert(tipXMax, yMin, tipZmin, 0, 8);
            Vert eus = ctx.vert(xMax,    yMax, zMin,    8, 8);
            Vert wus = ctx.vert(xMin,    yMax, zMin,    8, 0);
            Vert wdn = ctx.vert(tipXmin, yMin, tipZMax, 0, 0);
            Vert edn = ctx.vert(tipXMax, yMin, tipZMax, 0, 8);
            Vert eun = ctx.vert(xMax,    yMax, zMax,    8, 8);
            Vert wun = ctx.vert(xMin,    yMax, zMax,    8, 0);

            return new Rect[] {
                ctx.quad(ctx.u + ctx.size[2] + ctx.size[0],               ctx.size[2], ctx.v + ctx.size[2],  ctx.size[1], Direction.EAST,  edn, eds, eus, eun),
                ctx.quad(ctx.u,                                           ctx.size[2], ctx.v + ctx.size[2],  ctx.size[1], Direction.WEST,  wds, wdn, wun, wus),
                ctx.quad(ctx.u + ctx.size[2],                             ctx.size[0], ctx.v,                ctx.size[2], Direction.DOWN,  edn, wdn, wds, eds),
                ctx.quad(ctx.u + ctx.size[2] + ctx.size[0],               ctx.size[0], ctx.v + ctx.size[2], -ctx.size[2], Direction.UP,    eus, wus, wun, eun),
                ctx.quad(ctx.u + ctx.size[2],                             ctx.size[0], ctx.v + ctx.size[2],  ctx.size[1], Direction.NORTH, eds, wds, wus, eus),
                ctx.quad(ctx.u + ctx.size[2] + ctx.size[0] + ctx.size[2], ctx.size[0], ctx.v + ctx.size[2],  ctx.size[1], Direction.SOUTH, wdn, edn, eun, wun)
            };
        };
    }

    /**
     * Creates a single, flat plane aligned to the given face.
     */
    static QuadsBuilder plane(Face face) {
        return ctx -> {
            float xMax = ctx.pos[0] + ctx.size[0];
            float yMax = ctx.pos[1] + ctx.size[1];
            float zMax = ctx.pos[2] + ctx.size[2];

            xMax = ctx.fixture.stretchCoordinate(Axis.X, xMax, yMax, zMax, ctx.dilate[0]);
            yMax = ctx.fixture.stretchCoordinate(Axis.Y, xMax, yMax, zMax, face.applyFixtures(ctx.dilate[1]));
            zMax = ctx.fixture.stretchCoordinate(Axis.Z, xMax, yMax, zMax, ctx.dilate[2]);

            float xMin = ctx.fixture.stretchCoordinate(Axis.X, ctx.pos[0], ctx.pos[1], ctx.pos[2], -ctx.dilate[0]);
            float yMin = ctx.fixture.stretchCoordinate(Axis.Y, ctx.pos[0], ctx.pos[1], ctx.pos[2], face.applyFixtures(-ctx.dilate[1]));
            float zMin = ctx.fixture.stretchCoordinate(Axis.Z, ctx.pos[0], ctx.pos[1], ctx.pos[2], -ctx.dilate[2]);

            if (ctx.mirror[0]) {
                float v = xMax;
                xMax = xMin;
                xMin = v;
            }

            if (ctx.mirror[1]) {
                float v = yMax;
                yMax = yMin;
                yMin = v;
            }

            if (ctx.mirror[2]) {
                float v = zMax;
                zMax = zMin;
                zMin = v;
            }

            // w:west e:east d:down u:up s:south n:north
            Vert wds = ctx.vert(xMin, yMin, zMin, 0, 0);
            Vert eds = ctx.vert(xMax, yMin, zMin, 0, 8);
            Vert eus = ctx.vert(xMax, yMax, zMin, 8, 8);
            Vert wus = ctx.vert(xMin, yMax, zMin, 8, 0);
            Vert wdn = ctx.vert(xMin, yMin, zMax, 0, 0);
            Vert edn = ctx.vert(xMax, yMin, zMax, 0, 8);
            Vert eun = ctx.vert(xMax, yMax, zMax, 8, 8);
            Vert wun = ctx.vert(xMin, yMax, zMax, 8, 0);

            Rect[] quads = new Rect[1];

            boolean mirror = ctx.mirror[0] || ctx.mirror[1] || ctx.mirror[2];

            Direction lighting = face.getLighting();

            if (mirror && face.getAxis() != Axis.Y) {
                lighting = lighting.getOpposite();
            }

            if (face == Face.EAST) {
                quads[0] = ctx.quad(ctx.u, ctx.v, ctx.size[2], ctx.size[1], lighting, mirror, edn, eds, eus, eun);
            }
            if (face == Face.WEST) {
                quads[0] = ctx.quad(ctx.u, ctx.v, ctx.size[2], ctx.size[1], lighting, mirror, wds, wdn, wun, wus);
            }
            if (face == Face.UP) {
                quads[0] = ctx.quad(ctx.u, ctx.v, ctx.size[0], ctx.size[2], lighting, mirror, eus, wus, wun, eun);
            }
            if (face == Face.DOWN) {
                quads[0] = ctx.quad(ctx.u, ctx.v, ctx.size[0], ctx.size[2], lighting, mirror, edn, wdn, wds, eds);
            }
            if (face == Face.SOUTH) {
                quads[0] = ctx.quad(ctx.u, ctx.v, ctx.size[0], ctx.size[1], lighting, mirror, wdn, edn, eun, wun);
            }
            if (face == Face.NORTH) {
                quads[0] = ctx.quad(ctx.u, ctx.v, ctx.size[0], ctx.size[1], lighting, mirror, eds, wds, wus, eus);
            }

            return quads;
        };
    }

    /**
     * Builds the quads array using the provided box builder.
     */
    Rect[] build(BoxBuilder ctx);
}
