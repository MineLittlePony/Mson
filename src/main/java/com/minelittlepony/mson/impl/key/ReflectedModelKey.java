package com.minelittlepony.mson.impl.key;

import net.minecraft.util.Identifier;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.MsonModel;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class ReflectedModelKey<T extends MsonModel> extends AbstractModelKeyImpl<T> {
    public static <T extends MsonModel> ModelKey<T> fromJson(JsonObject json) {
        if (!json.has("implementation")) {
            throw new JsonParseException("Slot requires an implementation");
        }
        return new ReflectedModelKey<>(json.get("implementation").getAsString());
    }

    private final MethodHandle handle;

    @SuppressWarnings("unchecked")
    private ReflectedModelKey(String className) {

        id = new Identifier("dynamic", className);

        try {
            Class<T> implementation = (Class<T>)Class.forName(className, false, ReflectedModelKey.class.getClassLoader());

            if (!MsonModel.class.isAssignableFrom(implementation)) {
                throw new JsonParseException("Slot implementation does not implement MsonModel");
            }

            handle = MethodHandles.publicLookup().findConstructor(implementation, MethodType.methodType(implementation));
        } catch (Throwable e) {
            throw new JsonParseException("Unknown implementation", e);
        }
    }

    @Override
    public T createModel() {
        try {
            return (T) handle.invoke();
        } catch (Throwable e) {
            throw new RuntimeException(String.format("Could not instantiate implementation `%s`", id), e);
        }
    }
}
