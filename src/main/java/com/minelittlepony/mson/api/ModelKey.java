package com.minelittlepony.mson.api;

import net.minecraft.client.model.Model;
import net.minecraft.util.Identifier;

public interface ModelKey<T extends Model & MsonModel> {

    /**
     * Gets the unique id used to register this model key.
     */
    Identifier getId();

    /**
     * Creates an instance of the underlying type.
     */
    T createModel();
}
