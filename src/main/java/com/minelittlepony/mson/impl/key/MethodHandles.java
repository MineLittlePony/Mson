package com.minelittlepony.mson.impl.key;

import com.minelittlepony.mson.impl.MsonImpl;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;

final class MethodHandles {
    private static final Lookup LOOKUP = findTrustedLookup();

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

    public static <T> T lookupConstructor(Class<T> ifaceClass, Class<?> owner, Class<?>... parameters) {
        try {
            MethodType constrType = MethodType.methodType(void.class, parameters);
            MethodType callType = constrType.changeReturnType(owner);

            MethodHandle constr = LOOKUP.findConstructor(owner, constrType);

            CallSite site = LambdaMetafactory.metafactory(
                    LOOKUP.in(owner),
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
