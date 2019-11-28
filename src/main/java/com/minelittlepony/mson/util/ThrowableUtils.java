package com.minelittlepony.mson.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class ThrowableUtils {
    public static Throwable getRootCause(Throwable throwable) {
        Throwable cause = ExceptionUtils.getRootCause(throwable);
        // thank you, apache, for your stupid design behaviour
        return cause == null ? throwable : cause;
    }
}
