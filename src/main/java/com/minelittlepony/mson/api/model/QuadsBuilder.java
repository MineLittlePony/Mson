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
            float xMax = ctx.x + ctx.dx + ctx.stretchX;
            float yMax = ctx.y + ctx.dy + ctx.stretchY;
            float zMax = ctx.z + ctx.dz + ctx.stretchZ;

            float xMin = ctx.x - ctx.stretchX;
            float yMin = ctx.y - ctx.stretchY;
            float zMin = ctx.z - ctx.stretchZ;

            if (ctx.mirrorX) {
                float v = xMax;
                xMax = xMin;
                xMin = v;
            }

            float tipXmin = xMin + ctx.dx * tipInset;
            float tipZmin = zMin + ctx.dz * tipInset;
            float tipXMax = xMax - ctx.dx * tipInset;
            float tipZMax = zMax - ctx.dz * tipInset;

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
                ctx.quad(ctx.u + ctx.dz + ctx.dx,          ctx.dz, ctx.v + ctx.dz,  ctx.dy, Direction.EAST,  edn, eds, eus, eun),
                ctx.quad(ctx.u,                            ctx.dz, ctx.v + ctx.dz,  ctx.dy, Direction.WEST,  wds, wdn, wun, wus),
                ctx.quad(ctx.u + ctx.dz,                   ctx.dx, ctx.v,           ctx.dz, Direction.DOWN,  edn, wdn, wds, eds),
                ctx.quad(ctx.u + ctx.dz + ctx.dx,          ctx.dx, ctx.v + ctx.dz, -ctx.dz, Direction.UP,    eus, wus, wun, eun),
                ctx.quad(ctx.u + ctx.dz,                   ctx.dx, ctx.v + ctx.dz,  ctx.dy, Direction.NORTH, eds, wds, wus, eus),
                ctx.quad(ctx.u + ctx.dz + ctx.dx + ctx.dz, ctx.dx, ctx.v + ctx.dz,  ctx.dy, Direction.SOUTH, wdn, edn, eun, wun)
            };
        };
    }

    /**
     * Creates a single, flat plane aligned to the given face.
     */
    static QuadsBuilder plane(Face face) {
        return ctx -> {
            float xMax = ctx.x + ctx.dx;
            float yMax = ctx.y + ctx.dy;
            float zMax = ctx.z + ctx.dz;

            xMax = ctx.fixture.stretchCoordinate(Axis.X, xMax, yMax, zMax, ctx.stretchX);
            yMax = ctx.fixture.stretchCoordinate(Axis.Y, xMax, yMax, zMax, face.applyFixtures(ctx.stretchY));
            zMax = ctx.fixture.stretchCoordinate(Axis.Z, xMax, yMax, zMax, ctx.stretchZ);

            float xMin = ctx.fixture.stretchCoordinate(Axis.X, ctx.x, ctx.y, ctx.z, -ctx.stretchX);
            float yMin = ctx.fixture.stretchCoordinate(Axis.Y, ctx.x, ctx.y, ctx.z, face.applyFixtures(-ctx.stretchY));
            float zMin = ctx.fixture.stretchCoordinate(Axis.Z, ctx.x, ctx.y, ctx.z, -ctx.stretchZ);

            if (ctx.mirrorX) {
                float v = xMax;
                xMax = xMin;
                xMin = v;
            }

            if (ctx.mirrorY) {
                float v = yMax;
                yMax = yMin;
                yMin = v;
            }

            if (ctx.mirrorZ) {
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

            boolean mirror = ctx.mirrorX || ctx.mirrorY || ctx.mirrorZ;

            Direction lighting = face.getLighting();

            if (mirror && face.getAxis() != Axis.Y) {
                lighting = lighting.getOpposite();
            }

            if (face == Face.EAST) {
                quads[0] = ctx.quad(ctx.u, ctx.v, ctx.dz, ctx.dy, lighting, mirror, edn, eds, eus, eun);
            }
            if (face == Face.WEST) {
                quads[0] = ctx.quad(ctx.u, ctx.v, ctx.dz, ctx.dy, lighting, mirror, wds, wdn, wun, wus);
            }
            if (face == Face.UP) {
                quads[0] = ctx.quad(ctx.u, ctx.v, ctx.dx, ctx.dz, lighting, mirror, eus, wus, wun, eun);
            }
            if (face == Face.DOWN) {
                quads[0] = ctx.quad(ctx.u, ctx.v, ctx.dx, ctx.dz, lighting, mirror, edn, wdn, wds, eds);
            }
            if (face == Face.SOUTH) {
                quads[0] = ctx.quad(ctx.u, ctx.v, ctx.dx, ctx.dy, lighting, mirror, wdn, edn, eun, wun);
            }
            if (face == Face.NORTH) {
                quads[0] = ctx.quad(ctx.u, ctx.v, ctx.dx, ctx.dy, lighting, mirror, eds, wds, wus, eus);
            }

            return quads;
        };
    }

    /**
     * Builds the quads array using the provided box builder.
     */
    Rect[] build(BoxBuilder ctx);
}
