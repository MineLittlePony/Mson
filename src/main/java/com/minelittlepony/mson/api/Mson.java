package com.minelittlepony.mson.api;

import net.minecraft.client.model.Model;
import net.minecraft.util.Identifier;

import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.impl.MsonImpl;

public interface Mson {

    static Mson getInstance() {
        return MsonImpl.instance();
    }

    <T extends Model & MsonModel> ModelKey<T> registerModel(Identifier id, Class<T> implementation);

    void registerComponentType(Identifier id, JsonContext.Constructor<?> constructor);
}
