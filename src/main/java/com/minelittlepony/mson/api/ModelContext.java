package com.minelittlepony.mson.api;

import net.minecraft.client.model.Cuboid;

/**
 * The loading context for when a model is first created.
 *
 * This allows access to getting out named elements from the model json.
 */
public interface ModelContext {

    /**
     * Gets the named element and returns an instance of the requested type.
     *
     * @throws ClassCastException if the requested named element does not use the requested implementation.
     * @throws InvalidInputException if the named element does not exist.
     */
    <T> T findByName(String name);

    /**
     * Gets the named element and loads it into the provided cuboid.
     * @throws InvalidInputException if the named element does not exist.
     */
    void findByName(String name, Cuboid output);
}
