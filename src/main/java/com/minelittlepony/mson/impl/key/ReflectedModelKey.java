package com.minelittlepony.mson.impl.key;

import net.minecraft.util.Identifier;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.mixin.Lambdas;
import com.minelittlepony.mson.impl.invoke.MethodHandles;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
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

    private final Function<ModelContext, T> factory;

    private ReflectedModelKey(String className) {
        id = new Identifier("dynamic", className.replaceAll("[\\.\\$]", "/").toLowerCase());
        factory = lookupFactory(className);
    }

    @SuppressWarnings("unchecked")
    private Function<ModelContext, T> lookupFactory(String className) {
        try {
            Class<T> implementation = (Class<T>)Class.forName(className, false, ReflectedModelKey.class.getClassLoader());

            if (!MsonModel.class.isAssignableFrom(implementation)) {
                throw new JsonParseException("Slot implementation does not implement MsonModel");
            }

            Lambdas lambdas = MethodHandles.lambdas().remap(MsonModel.class, implementation);

            try {
                final Supplier<T> supplier = lambdas.lookupGenericFactory(Supplier.class, implementation, Construct.class);
                return ctx -> supplier.get();
            } catch (Error | Exception e) {
                return lambdas.lookupGenericFactory(Function.class, implementation, Factory.class);
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
    public <V extends T> V createModel(Supplier<V> supplier) {
        return null;
    }

    @FunctionalInterface
    public interface Construct {
        MsonModel get();
    }

    @FunctionalInterface
    public interface Factory {
        MsonModel apply(ModelContext context);
    }

}
