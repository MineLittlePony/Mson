package com.minelittlepony.mson.api;


import net.minecraft.client.model.geom.ModelPart;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.mson.impl.key.InstanceCreatorImpl;
import com.minelittlepony.mson.impl.key.ReflectedInstanceCreator;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface InstanceCreator<T> {
    InstanceCreator<ModelPart> DEFAULT = InstanceCreator.ofFunction(ModelPart.class, Function.identity());

    @SuppressWarnings("unchecked")
    static <T> InstanceCreator<T> ofPart() {
        return (InstanceCreator<T>)DEFAULT;
    }

    @Deprecated
    public static <T> InstanceCreator<T> byName(String className) {
        return ReflectedInstanceCreator.byName(className);
    }

    public static <T> InstanceCreator<T> ofFunction(Class<T> type, Function<ModelPart, T> function) {
        return new InstanceCreatorImpl<>(Optional.empty(), Optional.of(function), type);
    }

    public static <T> InstanceCreator<T> ofFactory(Class<T> type, Function<ModelView, T> factory) {
        return new InstanceCreatorImpl<>(Optional.of(factory), Optional.empty(), type);
    }

    public static <T> InstanceCreator<T> ofSupplier(Class<T> type, Supplier<T> supplier) {
        return new InstanceCreatorImpl<>(Optional.of(_ -> supplier.get()), Optional.of(_ -> supplier.get()), type);
    }

    @Nullable
    default Class<T> type() {
        return null;
    }

    static boolean isCompatible(Class<?> fromType, Class<?> toType) {
        return fromType != null && toType != null && (toType == fromType || toType.isAssignableFrom(fromType));
    }

    default boolean isCompatible(Class<?> toType) {
        return isCompatible(type(), toType);
    }

    default boolean isCompatible(InstanceCreator<?> toType) {
        return this == toType || isCompatible(toType.type());
    }

    default T createInstance(ModelContext context) {
        return createInstance(context, ModelContext::toTree);
    }

    T createInstance(ModelContext context, Function<ModelContext, ModelPart> converter);
}
