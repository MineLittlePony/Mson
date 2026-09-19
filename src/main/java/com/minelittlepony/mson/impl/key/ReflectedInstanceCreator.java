package com.minelittlepony.mson.impl.key;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Util;

import com.google.gson.JsonParseException;
import com.minelittlepony.mson.api.InstanceCreator;
import com.minelittlepony.mson.api.ModelView;
import com.minelittlepony.mson.impl.MsonImpl;

import java.util.function.Function;

@Deprecated
public final class ReflectedInstanceCreator {
    @Deprecated
    private static final Function<String, InstanceCreator<?>> NAME_LOOKUP = Util.memoize(className -> {
        try {
            return byType(Class.forName(className, false, InstanceCreatorImpl.class.getClassLoader()));
        } catch (Exception e) {
            throw new JsonParseException("Exception getting handle for implementation " + className, e);
        }
    });
    @Deprecated
    private static final Function<Class<Object>, InstanceCreator<Object>> TYPE_LOOKUP = Util.memoize(type -> {
        MsonImpl.LOGGER.warn("Specifying slot implementation by class name is being phased out. Register your slot with Mson.registerSlotType to continue using it by id. Type: " + type.getCanonicalName());
        if (ModelPart.class.isAssignableFrom(type)) {
            return InstanceCreator.ofPart();
        }

        var supplier = MethodHandles.createInstanceSupplier(type);
        var key = new InstanceCreatorImpl<>(
                MethodHandles.createInstanceFactory(type, ModelView.class).or(() -> supplier.map(c -> _ -> c.get())),
                MethodHandles.createInstanceFactory(type, ModelPart.class).or(() -> supplier.map(c -> _ -> c.get())),
                type
        );
        if (key.contextFactory().isEmpty() && key.partFactory().isEmpty()) {
            throw new RuntimeException("Could not locate constructors for type " + type);
        }
        return key;
    });

    @Deprecated
    @SuppressWarnings("unchecked")
    public static <T> InstanceCreator<T> byName(String className) {
        return (InstanceCreator<T>)NAME_LOOKUP.apply(className);
    }

    @Deprecated
    @SuppressWarnings("unchecked")
    public static <T> InstanceCreator<T> byType(Class<T> type) {
        return (InstanceCreator<T>)TYPE_LOOKUP.apply((Class<Object>)type);
    }

}
