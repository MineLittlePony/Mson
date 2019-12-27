package com.minelittlepony.mson.api;

import net.minecraft.util.Identifier;

import java.util.function.Supplier;

/**
 * Handle for a registered entity model.
 */
public interface ModelKey<T extends MsonModel> {

    /**
     * Gets the unique id used to register this model key.
     */
    Identifier getId();

    /**
     * Creates an instance of the underlying type.
     */
    <V extends T> V createModel();

    <V extends T> V createModel(Supplier<V> supplier);
}
