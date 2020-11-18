package com.minelittlepony.mson.impl.invoke;

import com.minelittlepony.mson.api.mixin.Lambdas;
import com.minelittlepony.mson.impl.MsonImpl;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class MethodHandles {

    static final Lookup LOOKUP = findTrustedLookup();

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

    public static Lambdas lambdas() {
        return new LambdasImpl();
    }

    private static final class LambdasImpl implements Lambdas {

        private final Map<Class<?>, Class<?>> classRemappings = new HashMap<>();

        @Override
        public LambdasImpl remap(Class<?> from, Class<?> to) {
            classRemappings.put(from, to);
            return this;
        }

        private Class<?>[] remapClasses(Class<?>...classes) {
            if (classRemappings.isEmpty()) {
                return classes;
            }
            return Arrays.stream(classes).map(this::remapClass).toArray(i -> new Class<?>[i]);
        }

        private static Class<?> getRawClass(Class<?> arrayClass) {
            if (arrayClass.isArray()) {
                return getRawClass(arrayClass.getComponentType());
            }
            return arrayClass;
        }

        private Class<?> remapClass(Class<?> clazz) {

            Class<?> componentType = getRawClass(clazz);

            Class<?> mappedComponentType = classRemappings.getOrDefault(componentType, componentType);

            if (mappedComponentType == componentType) {
                return clazz;
            }

            return changeArrayType(clazz, mappedComponentType);
        }

        private Class<?> changeArrayType(Class<?> arrayClass, Class<?> componentType) {
            if (arrayClass.isArray()) {
                return createArrayClass(changeArrayType(arrayClass.getComponentType(), componentType));
            }
            return componentType;
        }

        private Class<?> createArrayClass(Class<?> componentType) {
            return Array.newInstance(componentType, 0).getClass();
        }

        @Override
        public <T> T lookupGenericFactory(Class<T> ifaceClass, Class<?> owner, Class<?> definitionClass) {
            try {
                Method ifaceMethod = definitionClass.getMethods()[0];
                MethodType constrType = MethodType.methodType(void.class, remapClasses(ifaceMethod.getParameterTypes()));
                MethodType callType = constrType.changeReturnType(owner);

                MethodHandle constr = MethodHandles.LOOKUP.findConstructor(owner, constrType);

                CallSite site = LambdaMetafactory.metafactory(
                        MethodHandles.LOOKUP.in(owner),
                        ifaceMethod.getName(),
                        MethodType.methodType(ifaceClass),
                        callType.erase(),
                        constr,
                        callType);

                return (T)site.dynamicInvoker().invoke();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
