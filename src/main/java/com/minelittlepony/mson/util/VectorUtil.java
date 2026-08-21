package com.minelittlepony.mson.util;

public interface VectorUtil {

    static float[] create(ElementFunc func) {
        return new float[] {
                func.get(0), func.get(1),func.get(2)
        };
    }

    static float[] copy(float[] source, float[] target) {
        System.arraycopy(source, 0, target, 0, 3);
        return target;
    }

    static float[] applyIfPresent(float[] source, float[] target, VecFunc func) {
        if (target == null) {
            return create(i -> source[i]);
        }
        return apply(source, target, func);
    }

    static float[] apply(float[] source, float[] target, VecFunc func) {
        target[0] = func.apply(target[0], source[0]);
        target[1] = func.apply(target[1], source[1]);
        target[2] = func.apply(target[2], source[2]);
        return target;
    }

    interface VecFunc {
        static VecFunc SUM = (a, b) -> a + b;
        static VecFunc MIN = Math::min;
        static VecFunc MAX = Math::max;

        float apply(float a, float b);
    }

    interface ElementFunc {
        float get(int index);
    }
}
