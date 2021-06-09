package com.minelittlepony.mson.impl.key;

import net.minecraft.client.model.ModelPart;
import net.minecraft.util.Identifier;

import org.apache.commons.lang3.NotImplementedException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.json.JsonContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ReflectedModelKey<T> extends AbstractModelKeyImpl<T> {

    private static final Map<String, ReflectedModelKey<?>> keyCache = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> ReflectedModelKey<T> fromJson(JsonObject json) {
        if (!json.has("implementation")) {
            throw new JsonParseException("Slot requires an implementation");
        }

        synchronized (keyCache) {
            return (ReflectedModelKey<T>)keyCache.computeIfAbsent(json.get("implementation").getAsString(), ReflectedModelKey::new);
        }
    }

    private final Function<ModelContext, T> factory;
    private Class<T> type;

    private ReflectedModelKey(String className) {
        id = new Identifier("dynamic", className.replaceAll("[\\.\\$]", "/").toLowerCase());
        factory = lookupFactory(className);
    }

    public boolean isCompatible(Class<?> toType) {
        return type != null && toType != null && toType.isAssignableFrom(type);
    }

    @SuppressWarnings("unchecked")
    private Function<ModelContext, T> lookupFactory(String className) {
        try {
            type = (Class<T>)Class.forName(className, false, ReflectedModelKey.class.getClassLoader());

            if (!MsonModel.class.isAssignableFrom(type)) {
                throw new JsonParseException("Slot implementation does not implement MsonModel");
            }

            try {
                final Function<ModelPart, T> function = MethodHandles.createInstanceFactory(type, ModelPart.class);
                return ctx -> {
                    Map<String, ModelPart> tree = new HashMap<>();
                    ctx.getTree(tree);
                    return function.apply(new ModelPart(new ArrayList<>(), tree));
                };
            } catch (Error | Exception e) {
                try {
                    return MethodHandles.createInstanceFactory(type, ModelContext.class);
                } catch (Error | Exception ee) {
                    final Supplier<T> supplier = MethodHandles.createInstanceSupplier(type);
                    return ctx -> supplier.get();
                }
            }
        } catch (Exception e) {
            throw new JsonParseException("Exception getting handle for implementation " + className, e);
        }
    }

    public T createModel(ModelContext context) {
        return factory.apply(context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends T> V createModel() {
        return (V)createModel((ModelContext)null);
    }

    @Override
    public <V extends T> V createModel(MsonModel.Factory<V> supplier) {
        return null;
    }

    @Override
    public JsonContext getModelData() {
        throw new NotImplementedException("getModelData");
    }
}
