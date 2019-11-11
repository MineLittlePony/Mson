package com.minelittlepony.mson.impl.invoke;

import com.minelittlepony.mson.impl.MsonImpl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.util.function.BiFunction;

final class MethodHandles {

    private static final Lookup lookup = findTrustedLookup();
    private static final BiFunction<MethodHandle, Object, MethodHandle> bindTo = findBindTo();

    private static Lookup findTrustedLookup() {
        try {
            Field IMPL_LOOKUP = java.lang.invoke.MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            IMPL_LOOKUP.setAccessible(true);
            return (Lookup)IMPL_LOOKUP.get(null);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            MsonImpl.LOGGER.error("Could not obtain elevated lookup privileges. All lookups will be performed as a filthy casual.", e);
            return java.lang.invoke.MethodHandles.lookup();
        }
    }

    private static BiFunction<MethodHandle, Object, MethodHandle> findBindTo() {
        try {
            Class<?> BoundMethodHandle = Class.forName("java.lang.invoke.BoundMethodHandle");

            MethodHandle bindAgumentL = MethodHandles.trustedLookup().findSpecial(
                    MethodHandle.class,
                    "bindArgumentL",
                    MethodType.methodType(BoundMethodHandle, int.class, Object.class),
                    MethodHandle.class
            );

            return (handle, object) -> {
                try {
                    MethodHandle bound = bindAgumentL.bindTo(handle);
                    return (MethodHandle)bound.invoke(0, object);
                } catch (Throwable e) {
                    MsonImpl.LOGGER.error("bindTo operation failed. This should not happen.", e);
                    return handle;
                }
            };
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            MsonImpl.LOGGER.error("Could not obtain elevated bindTo privileges. All binds will follow casting restrictions..", e);
        }
        return MethodHandle::bindTo;
    }

    public static Lookup trustedLookup() {
        return lookup;
    }

    /**
     * Binds a method handle to a specific object instance.
     *
     * IMPORTANT: Instance type checks are disabled.
     */
    public static MethodHandle bind(MethodHandle handle, Object to) {
        return bindTo.apply(handle, to);
    }
}
