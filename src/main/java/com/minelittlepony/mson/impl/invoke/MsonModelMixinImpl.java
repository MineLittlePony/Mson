package com.minelittlepony.mson.impl.invoke;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.mixin.Extends;
import com.minelittlepony.mson.api.mixin.Trait;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MsonModelMixinImpl {

    private static final Map<Class<?>, MethodHandle> requesterLookupCache = new HashMap<>();
    private static final Map<Class<?>, MethodHandle> requestedLookupCache = new HashMap<>();

    public static MsonModel getSuper(MsonModel instance) {
        MethodHandle handle = MethodHandles.bind(requesterLookupCache.computeIfAbsent(instance.getClass(), MsonModelMixinImpl::constructSuper), instance);

        return context -> {
            try {
                handle.invoke(context);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static MethodHandle constructSuper(Class<?> requestingClass) {
        try {
            Extends extend = Objects.requireNonNull(requestingClass.getAnnotation(Extends.class), "Mixin model must have a target");

            Objects.requireNonNull(extend.value().getAnnotation(Trait.class), "Requested parent class was not a trait. It cannot be extended in this way.");

            if (!extend.force()) {
                checkInheritance(requestingClass, extend.value());
            }
            return requestedLookupCache.computeIfAbsent(extend.value(), MsonModelMixinImpl::constructHandle);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static MethodHandle constructHandle(Class<?> requestedClass) {
        try {
            return MethodHandles.trustedLookup().findSpecial(
                    requestedClass,
                    "init",
                    MethodType.methodType(void.class, ModelContext.class),
                    requestedClass
            );
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkInheritance(Class<?> requestingClass, Class<?> requestedClass) {

        if (requestingClass.isAssignableFrom(requestedClass)) {
            throw new AssertionError(String.format("%s cannot extend a class (%s) that is already in its hierarchy",
                    requestingClass.getCanonicalName(),
                    requestedClass.getCanonicalName()
            ));
        }

        if (!requestedClass.getSuperclass().isAssignableFrom(requestingClass)) {
            throw new AssertionError(String.format("Requesting class (%s) and requested class (%s) are too far apart",
                    requestingClass.getCanonicalName(),
                    requestedClass.getCanonicalName()
            ));
        }

        if (requestedClass.getDeclaredFields().length > 0) {
            throw new AssertionError(String.format("Requested class (%s) contains fields. You can only force-extend empty shell classes.",
                    requestedClass.getCanonicalName()
            ));
        }
    }
}
