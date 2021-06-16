package com.minelittlepony.mson.api;

import net.minecraft.client.model.ModelPart;
import net.minecraft.util.Identifier;

import com.minelittlepony.mson.api.json.JsonContext;

import java.util.Optional;

/**
 * Handle for a registered entity model.
 */
public interface ModelKey<T> {

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
    <V extends T> V createModel(MsonModel.Factory<V> supplier);

    /**
     * Creates a model part using the contents of this model's file.
     */
    Optional<ModelPart> createTree();

    /**
     * Retrieves or loads the json context used to constructing models.
     * The context returned presents a managed view of the raw json file(s)
     * referenced when loading this model.
     *
     * @throws IllegalStateException if called before resource loading (aka client startup) has completed.
     */
    Optional<JsonContext> getModelData();
}
