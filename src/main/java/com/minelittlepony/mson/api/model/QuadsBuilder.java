package com.minelittlepony.mson.api.model;

import net.minecraft.client.model.Quad;
import net.minecraft.client.model.Vertex;

public interface QuadsBuilder {

    /**
     * Otherwise known as a truncated square pyramid.
     *
     * This produces a square polygon with tapered sides ending in a flat top.
     */
    static QuadsBuilder squareFrustrum(float tipInset) {
        return box -> {
            float xMax = box.xMin + box.dx + box.scale;
            float yMax = box.yMin + box.dy + box.scale;
            float zMax = box.zMin + box.dz + box.scale;

            float x = box.xMin - box.scale;
            float y = box.yMin - box.scale;
            float z = box.zMin - box.scale;

            if (box.cuboid.getMirrorX()) {
                float v = xMax;
                xMax = x;
                x = v;
            }

            float tipXmin = x + box.dx * tipInset;
            float tipZmin = z + box.dz * tipInset;
            float tipXMax = xMax - box.dx * tipInset;
            float tipZMax = zMax - box.dz * tipInset;

            // w:west e:east d:down u:up s:south n:north
            Vertex wds = box.vert(tipXmin, y,    tipZmin, 0, 0);
            Vertex eds = box.vert(tipXMax, y,    tipZmin, 0, 8);
            Vertex eus = box.vert(xMax,    yMax, z,       8, 8);
            Vertex wus = box.vert(x,       yMax, z,       8, 0);
            Vertex wdn = box.vert(tipXmin, y,    tipZMax, 0, 0);
            Vertex edn = box.vert(tipXMax, y,    tipZMax, 0, 8);
            Vertex eun = box.vert(xMax,    yMax, zMax,    8, 8);
            Vertex wun = box.vert(x,       yMax, zMax,    8, 0);

            Quad[] quads = new Quad[] {
                box.quad(box.texU + box.dz + box.dx,          box.dz, box.texV + box.dz,  box.dy, edn, eds, eus, eun),
                box.quad(box.texU,                            box.dz, box.texV + box.dz,  box.dy, wds, wdn, wun, wus),
                box.quad(box.texU + box.dz,                   box.dx, box.texV,           box.dz, edn, wdn, wds, eds),
                box.quad(box.texU + box.dz + box.dx,          box.dx, box.texV + box.dz, -box.dz, eus, wus, wun, eun),
                box.quad(box.texU + box.dz,                   box.dx, box.texV + box.dz,  box.dy, eds, wds, wus, eus),
                box.quad(box.texU + box.dz + box.dx + box.dz, box.dx, box.texV + box.dz,  box.dy, wdn, edn, eun, wun)
            };

            if (box.cuboid.getMirrorX()) {
                for (Quad i : quads) {
                    i.flip();
                }
            }

            return quads;
        };
    }

    /**
     * Creates a single, flat plane aligned to the given face.
     */
    static QuadsBuilder plane(Face face) {
        return box -> {
            float xMax = box.xMin + box.dx + box.scale;
            float yMax = box.yMin + box.dy + box.scale;
            float zMax = box.zMin + box.dz + box.scale;

            float x = box.xMin - box.scale;
            float y = box.yMin - box.scale;
            float z = box.zMin - box.scale;

            if (box.cuboid.getMirrorX()) {
                float v = xMax;
                xMax = x;
                x = v;
            }

            if (box.cuboid.getMirrorY()) {
                float v = yMax;
                yMax = y;
                y = v;
            }

            if (box.cuboid.getMirrorZ()) {
                float v = zMax;
                zMax = z;
                z = v;
            }

            // w:west e:east d:down u:up s:south n:north
            Vertex wds = box.vert(x, y, z, 0, 0);
            Vertex eds = box.vert(xMax, y, z, 0, 8);
            Vertex eus = box.vert(xMax, yMax, z, 8, 8);
            Vertex wus = box.vert(x, yMax, z, 8, 0);
            Vertex wdn = box.vert(x, y, zMax, 0, 0);
            Vertex edn = box.vert(xMax, y, zMax, 0, 8);
            Vertex eun = box.vert(xMax, yMax, zMax, 8, 8);
            Vertex wun = box.vert(x, yMax, zMax, 8, 0);

            Quad[] quads = new Quad[1];

            if (face == Face.EAST) {
                quads[0] = box.quad(box.texU, box.dz, box.texV, box.dy, edn, eds, eus, eun);
            }
            if (face == Face.WEST) {
                quads[0] = box.quad(box.texU, box.dz, box.texV, box.dy, wds, wdn, wun, wus);
            }
            if (face == Face.UP) {
                quads[0] = box.quad(box.texU, box.dx, box.texV, box.dz, edn, wdn, wds, eds);
            }
            if (face == Face.DOWN) {
                quads[0] = box.quad(box.texU, box.dx, box.texV, box.dz, eus, wus, wun, eun);
            }
            if (face == Face.SOUTH) {
                quads[0] = box.quad(box.texU, box.dx, box.texV, box.dy, eds, wds, wus, eus);
            }
            if (face == Face.NORTH) {
                quads[0] = box.quad(box.texU, box.dx, box.texV, box.dy, wdn, edn, eun, wun);
            }

            if (box.cuboid.getMirrorX() || box.cuboid.getMirrorY() || box.cuboid.getMirrorZ()) {
                quads[0].flip();
            }

            return quads;
        };
    }

    Quad[] build(BoxBuilder box);
}
