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
    UP   (Axis.Y, Direction.UP),
    DOWN (Axis.Y, Direction.DOWN),
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

    public Direction getNormal() {
        return axis == Axis.Y ? lighting.getOpposite() : lighting;
    }

    public float applyFixtures(float stretch) {
        return (getAxis() == Axis.Y ? -1 : 1) * stretch;
    }

    /**
     * The perpendicular axis of the plane parallel to this face.
     */
    public Axis getAxis() {
        return axis;
    }

    /**
     * Determines whether a vertex intersects with a bounded plane oriented parallel to this face.
     *
     * @param position   The 3D position of the plane
     * @param dimensions The 2D dimensions (width + height) of the plane
     * @param vertex     The vertex to check.
     *
     * @return True if the vertex is within the plane's bounds.
     */
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

    /**
     * Generates the corner vertices that make up a square plane aligned with this face.
     * Can apply an optional dilation along any one of the primary axis.
     *
     * @param position   The 3D position of the plane.
     * @param dimensions The 2D dimensions (width + height) of the plane.
     * @param axis       The axis of dilation
     * @param dilate     The amount of dilation to be applied
     *
     * @return A corner holding both the original and post-dilation vectors of the vertex.
     */
    public Stream<Corner> getVertices(float[] position, float[] dimensions, Axis axis, float dilate) {

        Vec3d min = new Vec3d(position[0], position[1], position[2]);
        Vec3d max = new Vec3d(
                getAxis().getWidth().getFloat(dimensions),
                getAxis().getHeight().getFloat(dimensions),
                getAxis().getDepth().getFloat(dimensions)
        );

        Vec3d str = dilate == 0 ? Vec3d.ZERO : new Vec3d(
                (axis == Axis.X ? dilate : 0),
                (axis == Axis.Y ? dilate : 0),
                (axis == Axis.Z ? dilate : 0)
        );
        Vec3d stretchedMin = dilate == 0 ? min : min.subtract(str);
        Vec3d stretchedMax = dilate == 0 ? max : max.add(str.multiply(2));

        return Arrays.stream(Corner.CORNERS).map(corner -> {
            Vec3d cornerVec = min.add(max.multiply(corner));
            Vec3d stretched = dilate == 0 ? cornerVec : stretchedMin.add(stretchedMax.multiply(corner));
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

        private final Parameter widthIndex;
        private final Parameter heightIndex;
        private final Parameter depthIndex;

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
