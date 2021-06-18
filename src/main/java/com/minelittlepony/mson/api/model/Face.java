package com.minelittlepony.mson.api.model;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public enum Face {
    NONE (Axis.Y, Direction.UP),
    UP   (Axis.Y, Direction.DOWN),
    DOWN (Axis.Y, Direction.UP),
    WEST (Axis.X, Direction.WEST),
    EAST (Axis.X, Direction.EAST),
    NORTH(Axis.Z, Direction.NORTH),
    SOUTH(Axis.Z, Direction.SOUTH);

    private final Axis axis;
    private final Direction lighting;

    public static final Set<Face> VALUES = ImmutableSet.copyOf(values());
    private static final Map<String, Face> REGISTRY = new HashMap<>();

    static {
        VALUES.forEach(f -> REGISTRY.put(f.name(), f));
    }

    Face(Axis axis, Direction lighting) {
        this.axis = axis;
        this.lighting = lighting;
    }

    public Direction getLighting() {
        return lighting;
    }

    public float applyFixtures(float stretch) {
        return (getAxis() == Axis.Y ? -1 : 1) * stretch;
    }

    public Axis getAxis() {
        return axis;
    }

    public boolean isInside(float[] position, float[] dimensions, Vec3d vertex) {
        float x = position[0];
        float y = position[1];
        float z = position[2];

        float dx = getAxis().getWidth().getFloat(dimensions);
        float dy = getAxis().getHeight().getFloat(dimensions);
        float dz = getAxis().getDepth().getFloat(dimensions);

        return isBetween(vertex.x, x, x + dx)
            && isBetween(vertex.y, y, y + dy)
            && isBetween(vertex.z, z, z + dz);
    }

    private static boolean isBetween(double value, double min, double max) {
        return value >= min && value <= max;
    }

    public Stream<Corner> getVertices(float[] position, float[] dimensions, Axis axis, float stretch) {

        Vec3d min = new Vec3d(position[0], position[1], position[2]);
        Vec3d max = new Vec3d(
                getAxis().getWidth().getFloat(dimensions),
                getAxis().getHeight().getFloat(dimensions),
                getAxis().getDepth().getFloat(dimensions)
        );

        Vec3d str = stretch == 0 ? Vec3d.ZERO : new Vec3d(
                (axis == Axis.X ? stretch : 0),
                (axis == Axis.Y ? stretch : 0),
                (axis == Axis.Z ? stretch : 0)
        );
        Vec3d stretchedMin = stretch == 0 ? min : min.subtract(str);
        Vec3d stretchedMax = stretch == 0 ? max : max.add(str.multiply(2));

        return Arrays.stream(Corner.CORNERS).map(corner -> {
            Vec3d cornerVec = min.add(max.multiply(corner));
            Vec3d stretched = stretch == 0 ? cornerVec : stretchedMin.add(stretchedMax.multiply(corner));
            return new Corner(cornerVec, stretched);
        }).distinct();
    }

    public static Face of(String s) {
        return REGISTRY.getOrDefault(Strings.nullToEmpty(s).toUpperCase(), Face.NONE);
    }

    public enum Axis {
        X(-1,  1,  0),
        Y( 0, -1,  1),
        Z( 0,  1, -1);

        private Parameter widthIndex;
        private Parameter heightIndex;
        private Parameter depthIndex;

        Axis(int w, int h, int d) {
            widthIndex = new Parameter(w);
            heightIndex = new Parameter(h);
            depthIndex = new Parameter(d);
        }

        public Parameter getWidth() {
           return widthIndex;
        }

        public Parameter getHeight() {
            return heightIndex;
        }

        public Parameter getDepth() {
            return depthIndex;
        }

        public static final class Parameter {
            private final int index;

            private Parameter(int index) {
                this.index = index;
            }

            public float getFloat(float[] dimensions) {
                if (index < 0) {
                    return 0;
                }
                return dimensions[index];
            }

            public boolean getBoolean(boolean[] dimensions) {
                if (index < 0) {
                    return false;
                }
                return dimensions[index];
            }
        }
    }
}
