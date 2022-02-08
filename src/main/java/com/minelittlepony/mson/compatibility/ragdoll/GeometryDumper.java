package com.minelittlepony.mson.compatibility.ragdoll;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

import com.minelittlepony.mson.impl.skeleton.PartSkeleton;
import com.minelittlepony.mson.impl.skeleton.Skeleton;

import java.util.HashMap;
import java.util.Map;

class GeometryDumper implements VertexConsumerProvider, Skeleton.Visitor {
    static final GeometryDumper INSTANCE = new GeometryDumper();

    private int cubeIndex = 0;
    private final Map<ModelPart, Integer> parts = new HashMap<>();

    public <T extends Entity> Map<ModelPart, Integer> dumpGeometry(T entity) {
        cubeIndex = 0;
        parts.clear();

        @SuppressWarnings("unchecked")
        EntityRenderer<T> renderer = (EntityRenderer<T>)MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        // Physics mod uses a hack to get out all the geometry by re-rendering the entity.
        // But it doesn't retain any information about which part produced which cube
        // So we do the same to extract that information ourselves.
        if (renderer != null) {
            renderer.render(entity, 0, 0, new MatrixStack(), this, 0);
        }

        return parts;
    }

    @Override
    public void visit(ModelPart part) {
        if (part.visible && !part.isEmpty()) {
            if (PartSkeleton.of(part).getTotalDirectCubes() > 0 && !parts.containsKey(part)) {
                parts.put(part, cubeIndex);
                cubeIndex += PartSkeleton.of(part).getTotalDirectCubes();
            }
        }
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer layer) {
        return new NoOpVertexConsumer();
    }

    private final class NoOpVertexConsumer implements VertexConsumer, Skeleton.Visitor {
        @Override
        public void visit(ModelPart part) {
            GeometryDumper.this.visit(part);
        }

        @Override
        public VertexConsumer vertex(double var1, double var3, double var5) {
            return this;
        }

        @Override
        public VertexConsumer color(int var1, int var2, int var3, int var4) {
            return this;
        }

        @Override
        public VertexConsumer texture(float var1, float var2) {
            return this;
        }

        @Override
        public VertexConsumer overlay(int var1, int var2) {
            return this;
        }

        @Override
        public VertexConsumer light(int var1, int var2) {
            return this;
        }

        @Override
        public VertexConsumer normal(float var1, float var2, float var3) {
            return this;
        }

        @Override
        public void next() {

        }

        @Override
        public void fixedColor(int var1, int var2, int var3, int var4) {

        }

        @Override
        public void unfixColor() {

        }
    }
}
