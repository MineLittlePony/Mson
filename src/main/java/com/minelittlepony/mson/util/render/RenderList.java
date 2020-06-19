package com.minelittlepony.mson.util.render;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;
import java.util.function.Supplier;

/**
 * A fast iterated render list.
 * <p>
 * Backed by an array and lazily generated wherever possible. This is an experimental class
 * designed to get around the horrible mess that is Mojang's code. Use a cached instance of
 * this instead of {@link ImmutableList#of}
 */
public interface RenderList<T> extends List<T> {
    RenderConsumer<ModelPart> MODEL_PART_CONSUMER = ModelPart::render;
    RenderConsumer<Model> MODEL_CONSUMER = Model::render;

    /**
     * Creates a new render list from a pre-compiled list of renderable elements.
     */
    static RenderList<ModelPart> of(Iterable<ModelPart> list) {
        return of(list, MODEL_PART_CONSUMER);
    }

    /**
     * Creates a new render list from a pre-compiled list of renderable elements.
     * Accepts a function to call for each element when rendering.
     */
    static <T> RenderList<T> of(Iterable<T> list, RenderConsumer<T> consumer) {
        return new RenderListImpl.Flat<>(list, consumer);
    }

    /**
     * Creates a new render list that when compiled will populate itself with the elements returned
     * by the supplied factory method.
     * Accepts a function to call for each element when rendering.
     */
    static RenderList<ModelPart> create(Supplier<Iterable<ModelPart>> factory) {
        return create(factory, MODEL_PART_CONSUMER);
    }

    /**
     * Creates a new render list that when compiled will populate itself with the elements returned
     * by the supplied factory method.
     */
    static <T> RenderList<T> create(Supplier<Iterable<T>> factory, RenderConsumer<T> consumer) {
        return new RenderListImpl.Lazy<>(factory, consumer);
    }

    /**
     * Renders the elements in this list. If it has not been compiled already, this render list will be compiled.
     */
    void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha);

    /**
     * Interface for any method that can be used to render a part of a model.
     * Intentionally matches the signature of {@link Model#render} and {@link ModelPart#render}
     * and accepting the instance object as a first parameter to use together with method references.
     */
    @FunctionalInterface
    interface RenderConsumer<T> {
        void render(T obj, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha);
    }
}
