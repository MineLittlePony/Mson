package com.minelittlepony.mson.api;

import net.minecraft.client.model.ModelPart;

/**
 * Constructor to create a new mson model.
 */
public interface MsonModel {
    /**
     * Called to initialise this model with all of its contents.
     *
     * @param context The current loading context.
     */
    default void init(ModelContext context) {}

    public interface Factory<T> {
        T create(ModelPart tree);
    }
}
