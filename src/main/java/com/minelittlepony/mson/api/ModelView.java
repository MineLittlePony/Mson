package com.minelittlepony.mson.api;

import net.minecraft.client.model.Model;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a managed view of the current model being constructed.
 *
 * This allows access to getting out named elements from the model json.
 */
public interface ModelView {
    /**
     * Gets the immediate object in this context.
     * May be the same as the model if called on the root context.
     *
     * Otherwise it is the object this context was resolved against.
     *
     * @throws ClassCastException if the context doesn't match the requested type.
     */
    @Nullable
    <T> T getThis() throws ClassCastException;

    /**
     * Gets the root context.
     * Returns `this` when called on the root context.
     */
    ModelView getRoot();

    /*
     * Gets the block containing local variables and model metadata.
     */
    ModelMetadata getMetadata();

    /**
     * Gets the currently-active model instance.
     */
    @Nullable
    <T extends Model> T getModel();

    /**
     * Gets a value from this context's local variable table.
     */
    float getLocalValue(String name, float defaultValue);

    /**
     * Gets the named element and returns an instance of the requested type.
     *
     * @throws ClassCastException if the requested named element does not use the requested implementation.
     * @throws InvalidInputException if the named element does not exist.
     *
     * @deprecated Callers should pass a function to convert from a ModelPart to the expected type.
     */
    @Deprecated
    default <T> T findByName(String name) {
        return findByName(name, null, null);
    }

    /**
     * Gets the named element and returns an instance of the requested type.
     *
     * @throws ClassCastException if the requested named element does not use the requested implementation.
     * @throws InvalidInputException if the named element does not exist.
     */
    default <T> T findByName(String name, MsonModel.Factory<T> factory) {
        return findByName(name, factory, null);
    }

    /**
     * Gets the named element and returns an instance of the requested type.
     *
     * Unlike {@link #findByName(String, com.minelittlepony.mson.api.MsonModel.Factory)} this method will also allow
     * returning sub-types of the requested class.
     *
     * @throws InvalidInputException if the named element does not exist.
     */
    <T> T findByName(String name, MsonModel.Factory<T> factory, Class<T> type);
}
