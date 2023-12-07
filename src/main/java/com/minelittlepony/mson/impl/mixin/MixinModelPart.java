package com.minelittlepony.mson.impl.mixin;

import org.joml.Vector3f;
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

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.Cuboid;

@Mixin(value = ModelPart.class, priority = 999)
abstract class MixinModelPart implements PartSkeleton, PartAccessor {
    @Override
    @Accessor("children")
    public abstract Map<String, ModelPart> getChildren();

    @Override
    @Accessor("cuboids")
    public abstract List<Cuboid> getCuboids();

    @Override
    public int getTotalDirectCubes() {
        return getCuboids().size();
    }

    @Override
    public ModelPart getSelf() {
        return (ModelPart)(Object)this;
    }
}

@Mixin(ModelPart.Cuboid.class)
abstract class MixinCuboid implements Cube {
    @Shadow @Mutable
    private @Final ModelPart.Quad[] sides;
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
    @Shadow @Mutable
    private @Final ModelPart.Vertex[] vertices;

    @Accessor("direction")
    @Override
    public abstract Vector3f getNormal();

    @Override
    public Vert getVertex(int index) {
        return (Vert)vertices[index];
    }
    @Override
    public void setVertex(int index, Vert value) {
        vertices[index] = (ModelPart.Vertex)value;
    }

    @Override
    public int vertexCount() {
        return vertices.length;
    }
    @Override
    public Vert[] getVertices() {
        Vert[] vertices = new Vert[this.vertices.length];
        System.arraycopy(this.vertices, 0, vertices, 0, vertices.length);
        return vertices;
    }

    @Override
    public Rect setVertices(boolean reflect, Vert...vertices) {
        this.vertices = new ModelPart.Vertex[vertices.length];
        System.arraycopy(vertices, 0, this.vertices, 0, vertices.length);

        if (reflect) {
            int length = this.vertices.length;

            for(int i = 0; i < length / 2; ++i) {
                ModelPart.Vertex vertex = this.vertices[i];
                this.vertices[i] = this.vertices[length - 1 - i];
                this.vertices[length - 1 - i] = vertex;
            }
        }
        return this;
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
