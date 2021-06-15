package com.minelittlepony.mson.impl.key;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.function.Function;
import java.util.function.Supplier;
import java.lang.invoke.MethodHandles.Lookup;

final class MethodHandles {
    private static final Lookup LOOKUP = java.lang.invoke.MethodHandles.lookup();

    public static <Ctx, T> MethodHandle findConstructor(Class<T> owner, Class<?>... parameters) {
        try {
            MethodType constrType = MethodType.methodType(void.class, parameters);

            // privateLookupIn (since Java9) lets us access private methods and fields as if we were inside the same class.
            Lookup lookup = java.lang.invoke.MethodHandles.privateLookupIn(owner, LOOKUP);
            return lookup.findConstructor(owner, constrType);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Supplier<T> createInstanceSupplier(Class<T> owner) {
        final MethodHandle constr = findConstructor(owner);
        return () -> {
            try {
                return (T)constr.invoke();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <Ctx, T> Function<Ctx, T> createInstanceFactory(Class<T> owner, Class<Ctx> firstParam) {
        final MethodHandle constr = findConstructor(owner, firstParam);
        return ctx -> {
            try {
                return (T)constr.invoke(ctx);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }
}
