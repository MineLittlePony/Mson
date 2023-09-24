package com.minelittlepony.mson.impl.key;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.lang.invoke.MethodHandles.Lookup;

final class MethodHandles {
    private static final Lookup LOOKUP = java.lang.invoke.MethodHandles.lookup();

    public static <Ctx, T> Optional<MethodHandle> findConstructor(Class<T> owner, Class<?>... parameters) {
        try {
            MethodType constrType = MethodType.methodType(void.class, parameters);

            // privateLookupIn (since Java9) lets us access private methods and fields as if we were inside the same class.
            Lookup lookup = java.lang.invoke.MethodHandles.privateLookupIn(owner, LOOKUP);
            return Optional.ofNullable(lookup.findConstructor(owner, constrType));
        } catch (Throwable e) {}
        return Optional.empty();
    }

    public static <T> Optional<Supplier<T>> createInstanceSupplier(Class<T> owner) {
        return findConstructor(owner).map(constr -> () -> {
            try {
                return (T)constr.invoke();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static <Ctx, T> Optional<Function<Ctx, T>> createInstanceFactory(Class<T> owner, Class<Ctx> firstParam) {
        return findConstructor(owner, firstParam).map(constr -> ctx -> {
            try {
                return (T)constr.invoke(ctx);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }
}
