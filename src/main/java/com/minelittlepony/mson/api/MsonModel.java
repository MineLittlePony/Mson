package com.minelittlepony.mson.api;

import net.minecraft.client.model.ModelPart;

/**
 * Special instance of a model that directly handle's Mson-supplied values.
 *
 * Implementing this adds the init(context) method that will allow modders to reference custom objects.
 * <p>
 * i.e. MineLittlePony's "parts" or the results of a slot with a non-tree output type.
 */
public interface MsonModel {
    /**
     * Called to initialise this model with all of its contents.
     *
     * @param context The current loading context.
     */
    default void init(ModelContext context) {}

    /**
     * Constructor to create a new mson model.
     */
    public interface Factory<T> {
        T create(ModelPart tree);
    }
}
