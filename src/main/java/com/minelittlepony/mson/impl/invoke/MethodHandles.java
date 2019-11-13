package com.minelittlepony.mson.impl.invoke;

import com.minelittlepony.mson.impl.MsonImpl;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiFunction;

public final class MethodHandles {

    private static final Lookup LOOKUP = findTrustedLookup();
    private static final BiFunction<MethodHandle, Object, MethodHandle> BIND_TO = findBindTo();

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

            MethodHandle bindAgumentL = trustedLookup().findSpecial(
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

    static Lookup trustedLookup() {
        return LOOKUP;
    }

    /**
     * Binds a method handle to a specific object instance.
     *
     * IMPORTANT: Instance type checks are disabled.
     */
    public static MethodHandle bind(MethodHandle handle, Object to) {
        return BIND_TO.apply(handle, to);
    }

    public static Class<?> findHiddenInnerClass(Class<?> outerClass, Class<?> expectsToImplement) {
        for (Class<?> c : outerClass.getDeclaredClasses()) {
            if (expectsToImplement.isAssignableFrom(c)) {
                return c;
            }
        }
        throw new RuntimeException("Inner class was missing");
    }

    public static <T> T lookupInvoker(Class<T> ifaceClass, Class<?> owner) {

        try {
            Method ifaceMethod = ifaceClass.getMethods()[0];
            MethodType ifaceMethType = MethodType.methodType(ifaceMethod.getReturnType(), ifaceMethod.getParameterTypes());

            MethodType constrType = MethodType.methodType(void.class, ifaceMethod.getParameterTypes());
            MethodHandle constr = trustedLookup().findConstructor(owner, constrType);

            CallSite site = LambdaMetafactory.metafactory(trustedLookup(), ifaceMethod.getName(), ifaceMethType, ifaceMethType, constr, ifaceMethType);

            return (T)site.dynamicInvoker().invoke();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;

    }
}
