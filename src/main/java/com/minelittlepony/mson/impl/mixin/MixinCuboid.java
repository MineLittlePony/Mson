package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.model.Cuboid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.minelittlepony.mson.api.model.MsonCuboid;

@Mixin(Cuboid.class)
abstract class MixinCuboid implements MsonCuboid {
    @Override
    @Accessor("textureOffsetU")
    public abstract int getTextureOffsetU();

    @Override
    @Accessor("textureOffsetV")
    public abstract int getTextureOffsetV();

    @Override
    @Accessor("mirror")
    public abstract boolean getMirrorX();
}
