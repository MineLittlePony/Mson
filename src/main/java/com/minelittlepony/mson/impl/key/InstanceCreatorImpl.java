package com.minelittlepony.mson.impl.key;

import net.minecraft.client.model.geom.ModelPart;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonParseException;
import com.minelittlepony.mson.api.InstanceCreator;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.ModelView;
import com.minelittlepony.mson.api.MsonModel;

import java.util.Optional;
import java.util.function.Function;

public record InstanceCreatorImpl<T> (
        Optional<Function<ModelView, T>> contextFactory,
        Optional<Function<ModelPart, T>> partFactory,
        @Nullable Class<T> type) implements InstanceCreator<T> {
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
