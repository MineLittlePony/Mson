package com.minelittlepony.mson.impl.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.minelittlepony.mson.api.model.Cube;
import com.minelittlepony.mson.api.model.Rect;
import com.minelittlepony.mson.api.model.Vert;
import com.minelittlepony.mson.api.model.traversal.PartSkeleton;
import com.minelittlepony.mson.impl.fast.PartAccessor;

import java.util.List;
import java.util.Map;

import net.minecraft.client.model.geom.ModelPart;

@Mixin(value = ModelPart.class, priority = 999)
abstract class MixinModelPart implements PartSkeleton, PartAccessor {
    @Override
    @Accessor("children")
    public abstract Map<String, ModelPart> getChildren();

    @Override
    @Accessor("cubes")
    public abstract List<ModelPart.Cube> getCuboids();

    @Override
    public ModelPart getSelf() {
        return (ModelPart)(Object)this;
    }
}

@Mixin(ModelPart.Cube.class)
abstract class MixinCuboid implements Cube {
    @Shadow @Mutable
    private @Final ModelPart.Polygon[] polygons;
    @Override
    public void setSides(Rect[] sides) {
        if (sideCount() != sides.length) {
            polygons = new ModelPart.Polygon[sides.length];
        }
        System.arraycopy(sides, 0, polygons, 0, sides.length);
    }
    @Override
    public Rect getSide(int index) {
        return (Rect)(Object)polygons[index];
    }
    @Override
    public void setSide(int index, Rect value) {
        polygons[index] = (ModelPart.Polygon)(Object)value;
    }
    @Override
    public int sideCount() {
        return polygons.length;
    }
}

@Mixin(ModelPart.Polygon.class)
abstract class MixinQuad implements Rect {
    @Shadow @Mutable
    private @Final ModelPart.Vertex[] vertices;

    @Deprecated
    @Override
    public Rect setVertices(boolean mirror, Vert...vertices) {
        if (vertexCount() != vertices.length) {
            this.vertices = new ModelPart.Vertex[vertices.length];
        }
        Rect.copyVertexArray(this.vertices, vertices, mirror, null);
        return this;
    }
}

@Mixin(ModelPart.Vertex.class)
abstract class MixinVertex implements Vert { }
