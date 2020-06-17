package com.minelittlepony.mson.api;

import net.minecraft.util.Identifier;

import com.minelittlepony.mson.api.json.JsonContext;

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
     * Creates a new model instance using the constructor referenced when registering this key.
     *
     * @throws IllegalStateException if called before resource loading (aka client startup) has completed.
     */
    <V extends T> V createModel();

    /**
     * Creates a new model instance using a custom constructor.
     *
     * @throws IllegalStateException if called before resource loading (aka client startup) has completed.
     */
    <V extends T> V createModel(Supplier<V> supplier);

    /**
     * Retrieves or loads the json context used to constructing models.
     * The context returned presents a managed view of the raw json file(s)
     * referenced when loading this model.
     *
     * @throws IllegalStateException if called before resource loading (aka client startup) has completed.
     */
    JsonContext getModelData();
}
