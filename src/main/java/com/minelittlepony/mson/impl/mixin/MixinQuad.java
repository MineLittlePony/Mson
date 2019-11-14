package com.minelittlepony.mson.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.minelittlepony.mson.api.model.Rect;

@Mixin(targets = {"net.minecraft.client.model.ModelPart$Quad"})
abstract class MixinQuad implements Rect { }
