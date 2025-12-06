package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.model.geom.builders.CubeDeformation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CubeDeformation.class)
public interface MixinDilation {
    @Accessor("growX") float getX();
    @Accessor("growY") float getY();
    @Accessor("growZ") float getZ();
}