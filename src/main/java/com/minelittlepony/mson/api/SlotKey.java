package com.minelittlepony.mson.api;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

import com.minelittlepony.mson.impl.MsonImpl;

public record SlotKey<T>(Identifier id, InstanceCreator<T> factory) {
    public static final SlotKey<ModelPart> DEFAULT = new SlotKey<>(MsonImpl.id("part"), InstanceCreator.ofPart());
}
