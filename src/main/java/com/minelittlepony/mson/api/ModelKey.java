package com.minelittlepony.mson.api;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

import com.minelittlepony.mson.api.parser.FileContent;

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
    <V extends T> V createModel(MsonModel.Factory<V> factory);

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
     * @deprecated Use {@link ModelKey#getOrLoadModelData()}
     */
    @Deprecated
    Optional<FileContent<?>> getModelData();

    /**
     * Retrieves or loads the JSON context used to construct models.
     * The context returned presents a managed view of the raw json file(s)
     * referenced when loading this model.
     *
     * @throws IllegalStateException if called before resource loading (aka client startup) has completed.
     */
    @SuppressWarnings("deprecation")
    default Optional<FileContent<?>> getOrLoadModelData() {
        return getModelData();
    }

    /**
     * Checks whether this model key has data bound to it.
     * Use this to check whether it's safe to use this key to create a model.
     *
     * @return False if the model file for this key does not exist or has not been loaded yet.
     */
    default boolean isBound() {
        return getOrLoadModelData().isPresent();
    }
}
