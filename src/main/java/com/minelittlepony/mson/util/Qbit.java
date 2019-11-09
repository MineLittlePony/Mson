package com.minelittlepony.mson.util;

/**
 * It's like a boolean, but three.
 *
 * yes/no/neither - https://en.wikipedia.org/wiki/Qubit
 */
public enum Qbit {
    TRUE,
    FALSE,
    UNKNOWN;

    /**
     * Returns true if this qbit has a value.
     */
    public boolean isKnown() {
        return this != UNKNOWN;
    }

    /**
     * Returns the boolean representation of this qbit.
     */
    public boolean toBoolean() {
        return this == TRUE;
    }

    /**
     * Return a qbit with the equivalent boolean representation.
     */
    public static Qbit of(boolean value) {
        return value ? TRUE : FALSE;
    }
}
