package com.minelittlepony.mson.impl.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import com.minelittlepony.mson.api.model.Cube;
import com.minelittlepony.mson.api.model.MsonPart;
import com.minelittlepony.mson.api.model.Rect;
import com.minelittlepony.mson.api.model.Vert;

import java.util.List;
import java.util.Map;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.Vector3f;

@Mixin(ModelPart.class)
abstract class MixinModelPart implements MsonPart {
    @Override
    @Accessor("cuboids")
    public abstract List<ModelPart.Cuboid> getCubes();
    @Override
    @Accessor("children")
    public abstract Map<String, ModelPart> getChildren();
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
