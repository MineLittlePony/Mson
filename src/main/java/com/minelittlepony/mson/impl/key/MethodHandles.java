package com.minelittlepony.mson.impl.key;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
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

    // TODO: callsite generation is broken in 1.17 (possibly due to obf)
    @Deprecated
    public static <T> T lookupConstructor(Class<T> ifaceClass, Class<?> owner, Class<?>... parameters) {
        try {
            MethodType constrType = MethodType.methodType(void.class, parameters);
            MethodType callType = constrType.changeReturnType(owner);

            // privateLookupIn (since Java9) lets us access private methods and fields as if we were inside the same class.
            Lookup lookup = java.lang.invoke.MethodHandles.privateLookupIn(owner, LOOKUP);
            MethodHandle constr = lookup.findConstructor(owner, constrType);

            CallSite site = LambdaMetafactory.metafactory(
                    lookup,
                    ifaceClass.getMethods()[0].getName(), // name of the call site's method
                    MethodType.methodType(ifaceClass),    // signature of call site's method
                    callType.erase(),                     // signature of the method to implement
                    constr,                               // the method handle to invoke
                    callType);                            // the runtime signature to enforce

            /*
             * Supplier<IFace<args*, T> callSite = () -> {
             *     return args* -> {
             *          return new <T>(*args);
             *     };
             * };
             * return callSite.get();
             */
            return (T)site.getTarget().invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
