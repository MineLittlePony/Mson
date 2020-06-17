package com.minelittlepony.mson.impl.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

import com.minelittlepony.mson.api.model.BoxBuilder.ContentAccessor;
import com.minelittlepony.mson.api.model.Cube;
import com.minelittlepony.mson.api.model.MsonPart;
import com.minelittlepony.mson.api.model.Rect;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.api.model.Vert;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.client.util.math.Vector3f;

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

@Mixin(ModelPart.Cuboid.class)
abstract class MixinCuboid implements Cube {
    @Shadow @Final @Mutable
    private ModelPart.Quad[] sides;

    @Override
    public void setSides(Rect[] sides) {
        this.sides = new ModelPart.Quad[sides.length];
        System.arraycopy(sides, 0, this.sides, 0, sides.length);
    }
    @Override
    public Rect getSide(int index) {
        return (Rect)sides[index];
    }
    @Override
    public void setSide(int index, Rect value) {
        sides[index] = (ModelPart.Quad)value;
    }
    @Override
    public int sideCount() {
        return sides.length;
    }
}

@Mixin(ModelPart.Quad.class)
abstract class MixinQuad implements Rect {
    @Override
    public Vector3f getNormal() {
        return ((ModelPart.Quad)(Object)this).direction;
    }
    @Override
    public Vert getVertex(int index) {
        return (Vert)((ModelPart.Quad)(Object)this).vertices[index];
    }
    @Override
    public void setVertex(int index, Vert value) {
        ((ModelPart.Quad)(Object)this).vertices[index] = (ModelPart.Vertex)value;
    }
    @Override
    public int vertexCount() {
        return ((ModelPart.Quad)(Object)this).vertices.length;
    }
}

@Mixin(ModelPart.Vertex.class)
abstract class MixinVertex implements Vert {
    @Accessor("pos") @Override
    public abstract Vector3f getPos();
    @Accessor("u") @Override
    public abstract float getU();
    @Accessor("v") @Override
    public abstract float getV();
}
