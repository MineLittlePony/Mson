package com.minelittlepony.mson.api;


import net.minecraft.client.model.ModelPart;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.mson.impl.key.ReflectedModelKey;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface InstanceCreator<T> {
    InstanceCreator<ModelPart> DEFAULT = InstanceCreator.ofFunction(ModelPart.class, Function.identity());

    @SuppressWarnings("unchecked")
    static <T> InstanceCreator<T> ofPart() {
        return (InstanceCreator<T>)DEFAULT;
    }

    public static <T> InstanceCreator<T> byName(String className) {
        return ReflectedModelKey.byName(className);
    }

    public static <T> InstanceCreator<T> ofType(Class<T> type) {
        return ReflectedModelKey.byType(type);
    }

    public static <T> InstanceCreator<T> ofFunction(Class<T> type, Function<ModelPart, T> function) {
        return new ReflectedModelKey<>(Optional.empty(), Optional.of(function), type);
    }

    public static <T> InstanceCreator<T> ofFactory(Class<T> type, Function<ModelContext, T> factory) {
        return new ReflectedModelKey<>(Optional.of(factory), Optional.empty(), type);
    }

    public static <T> InstanceCreator<T> ofSupplier(Class<T> type, Supplier<T> supplier) {
        return new ReflectedModelKey<>(Optional.of(ctx -> supplier.get()), Optional.of(tree -> supplier.get()), type);
    }

    @Nullable
    Class<T> type();

    boolean isCompatible(Class<?> toType);

    default boolean isCompatible(InstanceCreator<?> toType) {
        return isCompatible(toType.type());
    }

    T createInstance(ModelContext context);

    T createInstance(ModelContext context, Function<ModelContext, ModelPart> converter);
}
