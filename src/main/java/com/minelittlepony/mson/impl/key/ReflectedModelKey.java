package com.minelittlepony.mson.impl.key;

import net.minecraft.client.model.ModelPart;
import net.minecraft.util.Util;

import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonParseException;
import com.minelittlepony.mson.api.InstanceCreator;
import com.minelittlepony.mson.api.ModelContext;

import java.util.function.Function;
import java.util.function.Supplier;

@Deprecated
public record ReflectedModelKey<T> (
        @Nullable Function<ModelContext, T> contextFactory,
        @Nullable Function<ModelPart, T> partFactory,
        Class<T> type) implements InstanceCreator<T> {
    private static final Function<String, InstanceCreator<?>> NAME_LOOKUP = Util.memoize(className -> {
        if (className.endsWith("ModelPart")) {
            return InstanceCreator.ofPart();
        }
        try {
            return byType(Class.forName(className, false, ReflectedModelKey.class.getClassLoader()));
        } catch (Exception e) {
            throw new JsonParseException("Exception getting handle for implementation " + className, e);
        }
    });
    private static final Function<Class<Object>, InstanceCreator<Object>> TYPE_LOOKUP = Util.memoize(type -> {
        if (ModelPart.class.equals(type)) {
            return InstanceCreator.ofPart();
        }

        Supplier<Object> supplier = null;

        Function<ModelPart, Object> treeFactory = null;
        Function<ModelContext, Object> contextFactory = null;

        try {
            supplier = MethodHandles.createInstanceSupplier(type);
        } catch (Error | Exception ignored) { }

        try {
            treeFactory = MethodHandles.createInstanceFactory(type, ModelPart.class);
        } catch (Error | Exception ignored) { }

        try {
            contextFactory = MethodHandles.createInstanceFactory(type, ModelContext.class);
        } catch (Error | Exception ignored) { }

        final Supplier<Object> supplierCopy = supplier;
        var key = new ReflectedModelKey<Object>(
                contextFactory == null ? supplierCopy == null ? null : ctx -> supplierCopy.get() : contextFactory,
                treeFactory == null ? supplierCopy == null ? null : tree -> supplierCopy.get() : treeFactory,
                type
        );
        if (key.contextFactory == null && key.partFactory == null) {
            throw new RuntimeException("Could not locate constructors for type " + type);
        }
        return key;
    });

    @SuppressWarnings("unchecked")
    public static <T> InstanceCreator<T> byName(String className) {
        return (InstanceCreator<T>)NAME_LOOKUP.apply(className);
    }

    @SuppressWarnings("unchecked")
    public static <T> InstanceCreator<T> byType(Class<T> type) {
        return (InstanceCreator<T>)TYPE_LOOKUP.apply((Class<Object>)type);
    }

    @Override
    public boolean isCompatible(Class<?> toType) {
        return type != null && toType != null && toType.isAssignableFrom(type);
    }

    @Override
    public T createInstance(ModelContext context) {
        if (contextFactory == null) {
            throw new NotImplementedException("The generated lamba cannot be used with a model context");
        }
        return contextFactory.apply(context);
    }

    @Override
    public T createInstance(ModelPart tree) {
        if (partFactory == null) {
            throw new NotImplementedException("The generated lamba does not support conversion from a pre-imported model tree");
        }
        return partFactory.apply(tree);
    }

    @Override
    public T createInstance(ModelContext context, Function<ModelContext, ModelPart> converter) {
        if (partFactory != null) {
            return createInstance(converter.apply(context));
        }
        return createInstance(context);
    }
}
