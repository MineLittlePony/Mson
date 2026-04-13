package com.minelittlepony.mson.api.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3fc;
import org.spongepowered.include.com.google.common.base.Preconditions;

public interface Rect {
    default Vector3fc getNormal() {
        return ((ModelPart.Polygon)this).normal();
    }

    default Vert getVertex(int index) {
        return getVertices()[index];
    }

    default void setVertex(int index, Vert value) {
        getVertices()[index] = (ModelPart.Vertex)value;
    }

    @Deprecated
    Rect setVertices(boolean mirror, Vert...vertices);

    default int vertexCount() {
        return getVertices().length;
    }

    default Vert[] getVertices() {
        return ((ModelPart.Polygon)this).vertices();
    }

    default Rect rotate(float x, float y, float z) {
        return rotate(new Quaternionf().rotateXYZ(x, y, z));
    }

    default Rect rotate(Quaternionf rotation) {
        for (int i = 0; i < vertexCount(); i++) {
            setVertex(i, getVertex(i).rotate(rotation));
        }
        return this;
    }

    static Vert[] copyVertexArray(Vert[] output, Vert[] input, boolean mirror, @Nullable Quaternionf rotation) {
        Preconditions.checkArgument(output.length == input.length, "Input and output must have the same number of vertices");

        if (mirror) {
            mirrorVertices(output, input, rotation);
        } else if (rotation == null) {
            System.arraycopy(input, 0, output, 0, output.length);
        } else {
            for (int i = 0; i < input.length; i++) {
                output[i] = input[i].rotate(rotation);
            }
        }

        return output;
    }

    static Vert[] mirrorVertices(Vert[] output, Vert[] input, @Nullable Quaternionf rotation) {
        Preconditions.checkArgument(output.length == input.length, "Input and output must have the same number of vertices");
        int length = input.length;

        for (int i = 0; i < length / 2; ++i) {
            Vert vertex = rotation == null ? input[i] : input[i].rotate(rotation);
            output[i] = rotation == null ? input[length - 1 - i] : input[length - 1 - i].rotate(rotation);
            output[length - 1 - i] = vertex;
        }

        return output;
    }

    static void remapUVs(Vert[] verts, float u0, float v0, float u1, float v1, float xTexSize, float yTexSize) {
        final float us = 0F / xTexSize;
        final float vs = 0F / yTexSize;
        final float[][] uvs = {
                { u1 / xTexSize - us, v0 / yTexSize + vs },
                { u0 / xTexSize + us, v0 / yTexSize + vs },
                { u0 / xTexSize + us, v1 / yTexSize - vs },
                { u1 / xTexSize - us, v1 / yTexSize - vs }
        };
        for (int i = 0; i < Math.min(4, verts.length); i++) {
            verts[i] = verts[i].remap(uvs[i][0], uvs[i][1]);
        }
    }

    static Direction mirrorFacing(boolean mirror, Direction facing) {
        return mirror && facing.getAxis() == Direction.Axis.X ? facing.getOpposite() : facing;
    }
}
