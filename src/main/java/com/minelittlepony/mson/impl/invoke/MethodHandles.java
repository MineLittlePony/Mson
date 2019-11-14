package com.minelittlepony.mson.impl.invoke;

import com.minelittlepony.mson.api.mixin.Lambdas;
import com.minelittlepony.mson.impl.MsonImpl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.function.BiFunction;

public final class MethodHandles {

    static final Lookup LOOKUP = findTrustedLookup();
    static final BiFunction<MethodHandle, Object, MethodHandle> BIND_TO = findBindTo();

    private static Lookup findTrustedLookup() {
        try {
            Field IMPL_LOOKUP = java.lang.invoke.MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            IMPL_LOOKUP.setAccessible(true);
            return (Lookup)IMPL_LOOKUP.get(null);
        } catch (Exception e) {
            MsonImpl.LOGGER.error("Could not obtain elevated lookup privileges. All lookups will be performed as a filthy casual.", e);
            return java.lang.invoke.MethodHandles.lookup();
        }
    }

    private static BiFunction<MethodHandle, Object, MethodHandle> findBindTo() {
        try {
            Class<?> BoundMethodHandle = Class.forName("java.lang.invoke.BoundMethodHandle");

            MethodHandle bindAgumentL = LOOKUP.findSpecial(
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
        } catch (Exception e) {
            MsonImpl.LOGGER.error("Could not obtain elevated bindTo privileges. All binds will follow casting restrictions..", e);
        }
        return MethodHandle::bindTo;
    }

    public static Lambdas lambdas() {
        return new LambdasImpl();
    }

    public static Class<?> createArrayClass(Class<?> componentType) {
        return Array.newInstance(componentType, 0).getClass();
    }

    public static Class<?> getRawClass(Class<?> arrayClass) {
        if (arrayClass.isArray()) {
            return getRawClass(arrayClass.getComponentType());
        }
        return arrayClass;
    }

    public static Class<?> changeArrayType(Class<?> arrayClass, Class<?> componentType) {
        if (arrayClass.isArray()) {
            return createArrayClass(changeArrayType(arrayClass.getComponentType(), componentType));
        }
        return componentType;
    }

    /**
     * Finds the class object for a specific inner class implementing a specific interface.
     *
     * This is intended to work together with a mixin that adds an interface to the targetted class.
     */
    public static Class<?> findHiddenInnerClass(Class<?> outerClass, Class<?> expectsToImplement) {
        for (Class<?> c : outerClass.getDeclaredClasses()) {
            if (expectsToImplement.isAssignableFrom(c)) {
                return c;
            }
        }
        throw new RuntimeException("Inner class was missing");
    }
}
