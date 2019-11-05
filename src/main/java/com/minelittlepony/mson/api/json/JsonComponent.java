package com.minelittlepony.mson.api.json;

import net.minecraft.client.model.Cuboid;

import org.apache.commons.lang3.NotImplementedException;

public interface JsonComponent<T> {
    T export();

    default void export(Cuboid output) {
        throw new NotImplementedException("I am not a cuboid");
    }
}