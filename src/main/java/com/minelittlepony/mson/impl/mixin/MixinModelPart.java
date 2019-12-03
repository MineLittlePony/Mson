package com.minelittlepony.mson.impl.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.minelittlepony.mson.api.model.BoxBuilder.ContentAccessor;
import com.minelittlepony.mson.api.model.MsonPart;
import com.minelittlepony.mson.api.model.Rect;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.api.model.Vert;

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

    @Shadow
    private float textureWidth;
    @Override
    public int getWidth() {
        return (int)textureWidth;
    }

    @Shadow
    private float textureHeight;
    @Override
    public int getHeight() {
        return (int)textureHeight;
    }

    @Override
    @Accessor("mirror")
    public abstract boolean getMirrorX();

    @Shadow @Final
    private ObjectList<ModelPart.Cuboid> cuboids;
    @Override
    public ObjectList<Cuboid> cubes() {
        return cuboids;
    }

    @Shadow @Final
    private ObjectList<ModelPart> children;
    @Override
    public ObjectList<ModelPart> children() {
        return children;
    }
}

@Mixin(targets = {"net.minecraft.client.model.ModelPart$Quad"})
abstract class MixinQuad implements Rect { }

@Mixin(targets = {"net.minecraft.client.model.ModelPart$Vertex"})
abstract class MixinVertex implements Vert { }
