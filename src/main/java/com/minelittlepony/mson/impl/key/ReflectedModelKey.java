package com.minelittlepony.mson.impl.key;

import net.minecraft.util.Identifier;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.MsonModel;

public final class ReflectedModelKey<T extends MsonModel> extends AbstractModelKeyImpl<T> {
    public static <T extends MsonModel> ModelKey<T> fromJson(JsonObject json) {
        if (!json.has("implementation")) {
            throw new JsonParseException("Slot requires an implementation");
        }
        return new ReflectedModelKey<>(json.get("implementation").getAsString());
    }

    private final Class<T> implementation;

    @SuppressWarnings("unchecked")
    private ReflectedModelKey(String className) {
        try {
            implementation = (Class<T>)Class.forName(className, false, ReflectedModelKey.class.getClassLoader());

            if (!MsonModel.class.isAssignableFrom(implementation)) {
                throw new JsonParseException("Slot implementation does not implement MsonModel");
            }
        } catch (ClassNotFoundException e) {
            throw new JsonParseException("Unknown implementation", e);
        }

        id = new Identifier("dynamic", String.valueOf(implementation.getCanonicalName().hashCode()));
    }

    @Override
    public T createModel() {
        try {
            return implementation.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(String.format("Could not instantiate implementation `%s`", implementation), e);
        }
    }
}
