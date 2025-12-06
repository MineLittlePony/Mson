package com.minelittlepony.mson.util;

import net.minecraft.client.model.geom.ModelPart;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public interface RenderList {
    void accept(PoseStack stack, VertexConsumer vertices, int overlay, int light, int color);

    default RenderList add(RenderList part) {
        return this;
    }

    default RenderList add(Consumer<PoseStack> action) {
        return add((stack, vertices, overlay, light, color) -> action.accept(stack));
    }

    default RenderList add(ModelPart...parts) {
        return add(of(parts));
    }

    default RenderList checked(BooleanSupplier check) {
        final RenderList self = this;
        return (stack, vertices, overlay, light, color) -> {
            if (check.getAsBoolean()) {
                self.accept(stack, vertices, overlay, light, color);
            }
        };
    }

    default <T> void pose(T state) {

    }

    default void clear() {}

    static RenderList of() {
        return new Impl(new RenderList[0]);
    }

    static RenderList of(ModelPart part) {
        return new Impl(new RenderList[] { part::render });
    }

    static RenderList of(ModelPart...parts) {
        return new Impl(Arrays.stream(parts).map(part -> (RenderList)part::render).toArray(RenderList[]::new));
    }

    class Impl implements RenderList {
        private RenderList[] parts;

        Impl(RenderList[] parts) {
            this.parts = parts;
        }

        @Override
        public RenderList add(RenderList part) {
            RenderList[] newArray = new RenderList[parts.length + 1];
            System.arraycopy(parts, 0, newArray, 0, parts.length);
            parts = newArray;
            parts[parts.length - 1] = part;
            return this;
        }

        @Override
        public void clear() {
            parts = new RenderList[0];
        }

        @Override
        public void accept(PoseStack stack, VertexConsumer vertices, int overlay, int light, int color) {
            for (RenderList part : parts) {
                part.accept(stack, vertices, overlay, light, color);
            }
        }

        @Override
        public <T> void pose(T state) {
            for (RenderList part : parts) {
                part.pose(state);
            }
        }
    }
}


