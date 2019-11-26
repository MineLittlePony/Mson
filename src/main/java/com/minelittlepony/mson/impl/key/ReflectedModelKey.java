package com.minelittlepony.mson.impl.key;

import net.minecraft.util.Identifier;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.impl.invoke.MethodHandles;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ReflectedModelKey<T extends MsonModel> extends AbstractModelKeyImpl<T> {

    private static final Map<String, ReflectedModelKey<?>> keyCache = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends MsonModel> ReflectedModelKey<T> fromJson(JsonObject json) {
        if (!json.has("implementation")) {
            throw new JsonParseException("Slot requires an implementation");
        }

        synchronized (keyCache) {
            return (ReflectedModelKey<T>)keyCache.computeIfAbsent(json.get("implementation").getAsString(), ReflectedModelKey::new);
        }
    }

    private final Factory<T> factory;

    private ReflectedModelKey(String className) {
        id = new Identifier("dynamic", className.replaceAll("[\\.\\$]", "/").toLowerCase());
        factory = lookupFactory(className);
    }

    @SuppressWarnings("unchecked")
    private Factory<T> lookupFactory(String className) {
        try {
            Class<T> implementation = (Class<T>)Class.forName(className, false, ReflectedModelKey.class.getClassLoader());

            if (!MsonModel.class.isAssignableFrom(implementation)) {
                throw new JsonParseException("Slot implementation does not implement MsonModel");
            }

            try {
                final Supplier<T> supplier = MethodHandles.lambdas().lookupFactoryInvoker(Supplier.class, implementation);
                return ctx -> supplier.get();
            } catch (Exception e) {
                return MethodHandles.lambdas().lookupFactoryInvoker(Factory.class, implementation);
            }
        } catch (Exception e) {
            throw new JsonParseException("Exception getting handle for implementation " + className, e);
        }
    }

    public T createModel(ModelContext context) {
        return factory.create(context);
    }

    @Override
    public T createModel() {
        return createModel(null);
    }

    interface Factory<T extends MsonModel> {
        T create(ModelContext context);
    }
}
