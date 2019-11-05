package com.minelittlepony.mson.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@FunctionalInterface
public interface MsonModelsReadyCallback {
    Event<MsonModelsReadyCallback> EVENT = EventFactory.createArrayBacked(MsonModelsReadyCallback.class, listeners -> () -> {
        for (MsonModelsReadyCallback event : listeners) {
            event.init();
        }
    });

    void init();
}
