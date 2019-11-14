package com.minelittlepony.mson.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.minelittlepony.mson.api.model.Rect;

@Mixin(targets = {"net.minecraft.client.model.ModelPart$Quad"})
abstract class MixinQuad implements Rect {
    @Override
    @Invoker("flip")
    public abstract void invertNormals();
}
