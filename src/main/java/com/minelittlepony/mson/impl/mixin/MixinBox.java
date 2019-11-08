package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.model.Box;
import net.minecraft.client.model.Quad;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import com.minelittlepony.mson.api.model.BoxBuilder.PolygonsSetter;

@Mixin(Box.class)
abstract class MixinBox implements PolygonsSetter {

    @Shadow
    @Final
    @Mutable
    private Quad[] polygons;

    @Override
    public void setPolygons(Quad[] quads) {
        polygons = quads;
    }
}
