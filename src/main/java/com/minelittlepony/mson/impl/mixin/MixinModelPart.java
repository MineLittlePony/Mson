package com.minelittlepony.mson.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.minelittlepony.mson.api.model.BoxBuilder.ContentAccessor;
import com.minelittlepony.mson.api.model.MsonPart;
import com.minelittlepony.mson.api.model.Texture;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.Cuboid;

@Mixin(ModelPart.class)
abstract class MixinModelPart implements MsonPart, Texture, ContentAccessor {
    @Override
    @Accessor("textureOffsetU")
    public abstract int getU();

    @Override
    @Accessor("textureOffsetV")
    public abstract int getV();

    @Accessor("textureWidth")
    abstract float getFloatidth();

    @Override
    public int getWidth() {
        return (int)getFloatidth();
    }

    @Accessor("textureHeight")
    public abstract float getFloatHeight();

    @Override
    public int getHeight() {
        return (int)getFloatHeight();
    }

    @Override
    @Accessor("mirror")
    public abstract boolean getMirrorX();

    @Override
    @Accessor("cuboids")
    public abstract ObjectList<Cuboid> cubes();

    @Override
    @Accessor("children")
    public abstract ObjectList<ModelPart> children();
}
