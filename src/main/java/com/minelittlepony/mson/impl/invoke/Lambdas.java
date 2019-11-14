package com.minelittlepony.mson.impl.invoke;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public class Lambdas {
    public static <T> T lookupFactoryInvoker(Class<T> ifaceClass, Class<?> owner) {
        try {
            Method ifaceMethod = ifaceClass.getMethods()[0];
            MethodType ifaceMethType = MethodType.methodType(ifaceMethod.getReturnType(), ifaceMethod.getParameterTypes());

            MethodType constrType = MethodType.methodType(void.class, ifaceMethod.getParameterTypes());
            MethodHandle constr = MethodHandles.trustedLookup().findConstructor(owner, constrType);

            CallSite site = LambdaMetafactory.metafactory(MethodHandles.trustedLookup(), ifaceMethod.getName(), ifaceMethType, ifaceMethType, constr, ifaceMethType);

            return (T)site.dynamicInvoker().invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T lookupSetter(Class<T> ifaceClass, Class<?> owner, String fieldName, String fieldType) {
        try {
            Method ifaceMethod = ifaceClass.getMethods()[0];

            MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();

            fieldName = resolver.mapFieldName("yarn",
                    resolver.unmapClassName("yarn", owner.getCanonicalName()),
                    fieldName,
                    fieldType);

            MethodType ifaceMethType = MethodType.methodType(ifaceMethod.getReturnType(), ifaceMethod.getParameterTypes());

            MethodHandle constr = MethodHandles.trustedLookup().findSetter(owner, fieldName, owner.getDeclaredField(fieldName).getType());

            CallSite site = LambdaMetafactory.metafactory(MethodHandles.trustedLookup(), ifaceMethod.getName(), ifaceMethType, ifaceMethType, constr, ifaceMethType);

            return (T)site.dynamicInvoker().invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
