package com.minelittlepony.mson.api.mixin;

/**
 * A factory for generating lambdas that call methods.
 */
public interface Lambdas {

    /**
     * Adds the specific class-to-class mapping.
     */
    Lambdas remap(Class<?> from, Class<?> to);

    /**
     * Generates an instance of the specified class to invoke a matching constructor.
     *
     * The interface must have one class, with parameters matching a constructor
     * on the target class, and a return type compatible with owner type.
     */
    <T> T lookupGenericFactory(Class<T> ifaceClass, Class<?> owner, Class<?> definitionClss);
}
