package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.model.TextureDimensions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextureDimensions.class)
public interface MixinTextureDimensions {
    @Accessor("width") int getWidth();
    @Accessor("height") int getHeight();
}