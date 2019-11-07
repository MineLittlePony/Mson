package com.minelittlepony.mson.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Event fired when all Mson models are parsed and ready for construction.
 *
 * This event is fired when the resource manager reloads, and immediately after all models are deemed 'ready' for interaction.
 *
 * ** THIS EVENT MAY FIRE MORE THAN ONCE **
 */
@FunctionalInterface
public interface MsonModelsReadyCallback {
    Event<MsonModelsReadyCallback> EVENT = EventFactory.createArrayBacked(MsonModelsReadyCallback.class, listeners -> () -> {
        for (MsonModelsReadyCallback event : listeners) {
            event.init();
        }
    });

    /**
     * Called when model loading completes.
     *
     * This is where you would create all your model instances and register entity renderers.
     */
    void init();
}
