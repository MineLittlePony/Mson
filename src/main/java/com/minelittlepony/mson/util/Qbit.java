package com.minelittlepony.mson.util;

public enum Qbit {
    TRUE,
    FALSE,
    UNKNOWN;

    public boolean isKnown() {
        return this != UNKNOWN;
    }

    public boolean toBoolean() {
        return this == TRUE;
    }

    public static Qbit of(boolean value) {
        return value ? TRUE : FALSE;
    }
}
