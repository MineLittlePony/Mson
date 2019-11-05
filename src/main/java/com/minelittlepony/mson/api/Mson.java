package com.minelittlepony.mson.api;

import net.minecraft.util.Identifier;

import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.impl.MsonImpl;

public interface Mson {

    static Mson getRegistry() {
        return MsonImpl.INSTANCE;
    }

    <T extends Model> ModelKey<T> registerModel(Identifier id, Class<T> implementation);

    void registerComponentType(Identifier id, JsonContext.Constructor<?> constructor);
}
