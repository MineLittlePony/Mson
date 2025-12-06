package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.model.geom.builders.MaterialDefinition;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MaterialDefinition.class)
public interface MixinTextureDimensions {
    @Accessor("xTexSize") int getWidth();
    @Accessor("yTexSize") int getHeight();
}