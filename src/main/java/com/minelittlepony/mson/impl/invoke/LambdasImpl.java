package com.minelittlepony.mson.impl.invoke;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

import com.minelittlepony.mson.api.mixin.Lambdas;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

final class LambdasImpl implements Lambdas {

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

    private Class<?> remapClass(Class<?> clazz) {

        Class<?> componentType = MethodHandles.getRawClass(clazz);

        Class<?> mappedComponentType = classRemappings.getOrDefault(componentType, componentType);

        if (mappedComponentType == componentType) {
            return clazz;
        }

        return MethodHandles.changeArrayType(clazz, mappedComponentType);
    }

    @Override
    public <T> T lookupFactory(Class<T> ifaceClass, Class<?> owner, Class<?> definitionClass) {
        try {
            Method idefMethod = definitionClass.getMethods()[0];
            Method ifaceMethod = ifaceClass.getMethods()[0];

            MethodType constrType = MethodType.methodType(void.class, remapClasses(idefMethod.getParameterTypes()));

            MethodType implType = MethodType.methodType(ifaceMethod.getReturnType(), ifaceMethod.getParameterTypes());

            MethodHandle constr = MethodHandles.LOOKUP.findConstructor(owner, constrType);

            CallSite site = LambdaMetafactory.metafactory(
                    MethodHandles.LOOKUP.in(ifaceClass),
                    idefMethod.getName(),
                    MethodType.methodType(ifaceClass),
                    implType,
                    constr,
                    constrType.changeReturnType(owner));

            return (T)site.dynamicInvoker().invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
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

    @Override
    public <Owner, Type> BiConsumer<Owner, ? extends Type> lookupSetter(Class<Owner> owner, Class<Type> fieldType, String fieldName) {
        try {
            MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();

            Class<?> actualFieldType = remapClass(fieldType);
            fieldName =  resolver.mapFieldName("named",
                    resolver.unmapClassName("named", owner.getCanonicalName()),
                    fieldName,
                    resolver.unmapClassName("named", actualFieldType.getCanonicalName()));

            MethodHandle setter = MethodHandles.LOOKUP.findSetter(owner, fieldName, actualFieldType);

            final CallSite site = LambdaMetafactory.metafactory(
                    MethodHandles.LOOKUP.in(owner),
                    "accept",
                    MethodType.methodType(BiConsumer.class, MethodHandle.class),
                    setter.type().erase(),
                    java.lang.invoke.MethodHandles.exactInvoker(setter.type()),
                    setter.type()
            );

            return (BiConsumer<Owner, Type>)site.getTarget().invokeExact(setter);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
