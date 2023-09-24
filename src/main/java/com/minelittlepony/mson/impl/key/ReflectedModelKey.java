package com.minelittlepony.mson.impl.key;

import net.minecraft.client.model.ModelPart;
import net.minecraft.util.Util;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonParseException;
import com.minelittlepony.mson.api.InstanceCreator;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.ModelView;
import com.minelittlepony.mson.api.MsonModel;

import java.util.Optional;
import java.util.function.Function;

public record ReflectedModelKey<T> (
        Optional<Function<ModelContext, T>> contextFactory,
        Optional<Function<ModelPart, T>> partFactory,
        @Nullable Class<T> type) implements InstanceCreator<T> {
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

        var supplier = MethodHandles.createInstanceSupplier(type);
        var key = new ReflectedModelKey<>(
                MethodHandles.createInstanceFactory(type, ModelContext.class).or(() -> supplier.map(c -> ctx -> c.get())),
                MethodHandles.createInstanceFactory(type, ModelPart.class).or(() -> supplier.map(c -> tree -> c.get())),
                type
        );
        if (key.contextFactory().isEmpty() && key.partFactory().isEmpty()) {
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
        return contextFactory.map(factory -> factory.apply(context))
                .orElseGet(() -> {
            return partFactory.map(factory -> initInstance(factory.apply(context.toTree()), context))
                    .orElseThrow(() -> new JsonParseException("The generated lamba cannot be used with a model context"));
        });
    }

    @Override
    public T createInstance(ModelContext context, Function<ModelContext, ModelPart> converter) {
        return partFactory
                .map(factory -> initInstance(factory.apply(converter.apply(context)), context))
                .orElseGet(() -> createInstance(context));
    }

    private T initInstance(T instance, ModelView view) {
        if (instance instanceof MsonModel model) {
            model.init(view);
        }
        return instance;
    }
}
