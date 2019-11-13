package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.model.ModelPart.*;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import com.minelittlepony.mson.api.model.BoxBuilder.PolygonsSetter;
import com.minelittlepony.mson.api.model.Rect;

@Mixin(Cuboid.class)
abstract class MixinCuboid implements PolygonsSetter {

    @Shadow
    @Final
    @Mutable
    private Object[] sides;

    @Override
    public void setPolygons(Rect[] quads) {
        sides = quads;
    }
}
