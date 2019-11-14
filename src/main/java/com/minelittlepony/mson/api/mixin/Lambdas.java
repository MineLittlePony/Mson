package com.minelittlepony.mson.api.mixin;

import java.util.function.BiConsumer;

/**
 * A factory for generating lambdas that call methods.
 */
public interface Lambdas {

    /**
     * Adds the specific class-to-class mapping.
     */
    Lambdas remap(Class<?> from, Class<?> to);

    /**
     * Generates an instance of the specifid class to invoke a matching constructor.
     *
     * The interface must have one class, with parameters matching a constructor
     * on the target class, and a return type compatible with owner type.
     */
    <T> T lookupFactoryInvoker(Class<T> ifaceClass, Class<?> owner);

    /**
     * Creates a setter.
     */
    <Owner, Type> BiConsumer<Owner, Type> lookupSetter(Class<Owner> owner, Class<Type> fieldType, String fieldName);
}
