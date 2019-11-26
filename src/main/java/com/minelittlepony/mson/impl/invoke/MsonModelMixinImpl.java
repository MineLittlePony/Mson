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
        MethodHandle handle = MethodHandles.BIND_TO.apply(requesterLookupCache.computeIfAbsent(instance.getClass(), MsonModelMixinImpl::constructSuper), instance);

        return context -> {
            try {
                handle.invoke(context);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static MethodHandle constructSuper(Class<?> requestingClass) {
        return requestedLookupCache.computeIfAbsent(TypeExtension.get(requestingClass).value(), MsonModelMixinImpl::constructHandle);
    }

    private static MethodHandle constructHandle(Class<?> requestedClass) {
        try {
            return MethodHandles.LOOKUP.findSpecial(
                    requestedClass,
                    "init",
                    MethodType.methodType(void.class, ModelContext.class),
                    requestedClass
            );
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static final class TypeExtension {
        static TypeExtension get(Class<?> requestingClass) {
            if (requestingClass == Object.class || requestingClass.getSuperclass() == Object.class) {
                throw new NullPointerException("Mixin model must have a target");
            }

            Extends extend = requestingClass.getAnnotation(Extends.class);

            if (extend != null) {
                return new TypeExtension(requestingClass, extend);
            }

            return get(requestingClass.getSuperclass());
        }

        final Extends extend;

        private TypeExtension(Class<?> declaringType, Extends extend) {
            this.extend = extend;

            Objects.requireNonNull(extend.value().getAnnotation(Trait.class), "Requested parent class was not a trait. It cannot be extended in this way.");

            if (!extend.force()) {
                checkInheritance(declaringType);
            }
        }

        Class<? extends MsonModel> value() {
            return extend.value();
        }

        private void checkInheritance(Class<?> declaringType) {

            if (declaringType.isAssignableFrom(value())) {
                throw new AssertionError(String.format("%s cannot extend a class (%s) that is already in its hierarchy",
                        declaringType.getCanonicalName(),
                        value().getCanonicalName()
                ));
            }

            if (!value().getSuperclass().isAssignableFrom(declaringType)) {
                throw new AssertionError(String.format("Requesting class (%s) and requested class (%s) are too far apart",
                        declaringType.getCanonicalName(),
                        value().getCanonicalName()
                ));
            }

            if (value().getDeclaredFields().length > 0) {
                throw new AssertionError(String.format("Requested class (%s) contains fields. You can only force-extend empty shell classes.",
                        value().getCanonicalName()
                ));
            }
        }
    }
}
