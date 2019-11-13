package com.minelittlepony.mson.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.minelittlepony.mson.api.model.Vert;

@Mixin(targets = {"net.minecraft.client.model.ModelPart.Vertex"})
abstract class MixinVertex implements Vert {

}
