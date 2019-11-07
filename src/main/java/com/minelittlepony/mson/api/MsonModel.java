package com.minelittlepony.mson.api;

/**
 * A Mson Model.
 *
 * All models must implement this interface.
 */
public interface MsonModel {

    /**
     * Called to initialise this model with all of its contents.
     *
     * @param context The current loading context.
     */
    void init(ModelContext context);
}
