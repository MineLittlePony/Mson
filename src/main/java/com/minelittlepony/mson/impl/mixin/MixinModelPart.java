package com.minelittlepony.mson.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.minelittlepony.mson.api.model.BoxBuilder.ContentAccessor;
import com.minelittlepony.mson.api.model.MsonPart;

import java.util.List;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.Cuboid;

@Mixin(ModelPart.class)
abstract class MixinModelPart implements MsonPart, ContentAccessor {
    @Override
    @Accessor("textureOffsetU")
    public abstract int getTextureOffsetU();

    @Override
    @Accessor("textureOffsetV")
    public abstract int getTextureOffsetV();

    @Override
    @Accessor("mirror")
    public abstract boolean getMirrorX();

    @Override
    @Accessor("cubes")
    public abstract List<Cuboid> cubes();

    @Override
    @Accessor("children")
    public abstract List<ModelPart> children();
}
