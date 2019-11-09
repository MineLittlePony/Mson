package com.minelittlepony.mson.api;

import net.minecraft.client.model.Cuboid;
import net.minecraft.client.model.Model;

import com.minelittlepony.mson.api.model.Texture;

import javax.annotation.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * The loading context for when a model is first created.
 *
 * This allows access to getting out named elements from the model json.
 */
public interface ModelContext {

    /**
     * Gets the currently-active model instance.
     */
    Model getModel();

    /**
     * Gets the immediate object in this context.
     * May be the same as the model if called on the root context.
     *
     * Otherwise it is the object this context was resolved against.
     */
    Object getContext();

    /**
     * Gets the json context creating this model.
     * The json context is filtered bubbled up from the initial call site.
     */
    Locals getLocals();

    /**
     * Gets the world scale factor.
     */
    float getScale();

    /**
     * Checks if a value has been stored for the given name.
     * If one was not found, computes one using the supplied method and returns that.
     *
     * Will always return a new instance if the name is empty or null.
     */
    <T> T computeIfAbsent(@Nullable String name, ContentSupplier<T> supplier);

    /**
     * Gets the named element and returns an instance of the requested type.
     *
     * @throws ClassCastException if the requested named element does not use the requested implementation.
     * @throws InvalidInputException if the named element does not exist.
     */
    <T> T findByName(String name);

    /**
     * Gets the named element and loads it into the provided cuboid.
     * @throws InvalidInputException if the named element does not exist.
     */
    void findByName(String name, Cuboid output);

    /**
     * Gets the root context.
     * Returns `this` when called on the root context.
     */
    ModelContext getRoot();

    /**
     * Resolves this context against the given object.
     * Returns a new sub-context as a child of this one where the result of `getContext()` returns the passed in object.
     *
     * @throws NullPointerException if the passed in object is null.
     */
    ModelContext resolve(Object child);

    @FunctionalInterface
    interface ContentSupplier<T> extends Function<String, T> {
        @Override
        default T apply(String key) {
            try {
                return get(key);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        T get(String key) throws InterruptedException, ExecutionException;
    }

    interface Locals {
        CompletableFuture<Texture> getTexture();
    }
}
