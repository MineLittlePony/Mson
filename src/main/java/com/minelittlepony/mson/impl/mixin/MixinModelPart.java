package com.minelittlepony.mson.impl.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.mson.api.model.BoxBuilder.ContentAccessor;

import java.util.Random;

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

    // https://bugs.mojang.com/browse/MC-169239
    @Inject(method = "getRandomCuboid(Ljava/util/Random;)Lnet/minecraft/client/model/ModelPart$Cuboid;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onGetRandomCuboid(Random random, CallbackInfoReturnable<ModelPart.Cuboid> info) {
        if (cubes().isEmpty()) {
            Cuboid cube = EMPTY_CUBE;
            if (!children().isEmpty()) {
                // Any loops in the structure would be caught normally when rendering
                // so we don't have to worry about that.
                cube = children().get(random.nextInt(children().size())).getRandomCuboid(random);
            }

            info.setReturnValue(cube);
        }
    }
}

@Mixin(targets = {"net.minecraft.client.model.ModelPart$Quad"})
abstract class MixinQuad implements Rect { }

@Mixin(targets = {"net.minecraft.client.model.ModelPart$Vertex"})
abstract class MixinVertex implements Vert { }
