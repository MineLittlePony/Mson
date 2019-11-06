package com.minelittlepony.mson.model;

public enum Face {
    UP(Axis.Y, -1),
    DOWN(Axis.Y, 1),
    WEST(Axis.X, -1),
    EAST(Axis.X, 1),
    NORTH(Axis.Z, -1),
    SOUTH(Axis.Z,  1);

    private final int direction;
    private final Axis axis;

    Face(Axis axis, int direction) {
        this.direction = direction;
        this.axis = axis;
    }

    public float[] transformPosition(float[] position, float scale) {

        float[] result = new float[position.length];
        System.arraycopy(position, 0, result, 0, position.length);

        if (axis == Axis.Y) {
            result[2] += direction * (scale * 2);
        }

        return result;
    }

    public Axis getAxis() {
        return axis;
    }

    public enum Axis {
        X(-1,  0,  1),
        Y(0, -1,  1),
        Z(0,  1, -1);

        private int widthIndex;
        private int heightIndex;
        private int depthIndex;

        Axis(int w, int h, int d) {
            widthIndex = w;
            heightIndex = h;
            depthIndex = d;
        }

        public int getWidth(int[] dimensions) {
            if (widthIndex < 0) {
                return 0;
            }
            return dimensions[widthIndex];
        }

        public int getHeight(int[] dimensions) {
            if (heightIndex < 0) {
                return 0;
            }
            return dimensions[heightIndex];
        }

        public int getDeptch(int[] dimensions) {
            if (depthIndex < 0) {
                return 0;
            }
            return dimensions[depthIndex];
        }
    }
}
