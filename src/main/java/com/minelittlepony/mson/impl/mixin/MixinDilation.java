package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.model.Dilation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Dilation.class)
public interface MixinDilation {
    @Accessor("radiusX") float getX();
    @Accessor("radiusY") float getY();
    @Accessor("radiusZ") float getZ();
}