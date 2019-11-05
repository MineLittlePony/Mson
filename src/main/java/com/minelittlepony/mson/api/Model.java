package com.minelittlepony.mson.api;

/**
 * A Mson Model.
 */
public interface Model {

    /**
     * Called to initialise this model with all of its contents.
     *
     * @param context The current loading context.
     */
    void init(ModelContext context);
}
