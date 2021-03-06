package com.minelittlepony.mson.api;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * The loading context for when a model is first created.
 *
 * This allows access to getting out named elements from the model json.
 */
public interface ModelContext {

    /**
     * Gets the root context.
     * Returns `this` when called on the root context.
     */
    ModelContext getRoot();

    /**
     * Gets the currently-active model instance.
     */
    @Nullable
    <T extends Model> T getModel();

    /**
     * Gets the immediate object in this context.
     * May be the same as the model if called on the root context.
     *
     * Otherwise it is the object this context was resolved against.
     *
     * @throws ClassCastException if the context doesn't match the requested type.
     */
    @Nullable
    <T> T getContext() throws ClassCastException;

    /**
     * Provides access to the contextual information for the current context.
     * <p>
     * Includes access to inherited values and properties.
     */
    Locals getLocals();

    /**
     * Checks if a value has been stored for the given name.
     * If one was not found, computes one using the supplied method and returns that.
     *
     * Will always return a new instance if the name is empty or null.
     */
    <T> T computeIfAbsent(@Nullable String name, FutureSupplier<T> supplier);

    /**
     * Converts the entire model tree into native objects, outputting into the provided output object.
     */
    default void getTree(Map<String, ModelPart> tree) {
        getTree(this, tree);
    }

    /**
     * Converts the entire model tree into native objects, outputting into the provided output object.
     *
     * @param context The context where this call originated.
     */
    void getTree(ModelContext context, Map<String, ModelPart> tree);

    /**
     * Gets the named element and returns an instance of the requested type.
     *
     * @throws ClassCastException if the requested named element does not use the requested implementation.
     * @throws InvalidInputException if the named element does not exist.
     */
    default <T> T findByName(String name) {
        return findByName(this, name);
    }

    /**
     * Gets the named element and returns an instance of the requested type.
     *
     * @throws ClassCastException if the requested named element does not use the requested implementation.
     * @throws InvalidInputException if the named element does not exist.
     */
    <T> T findByName(ModelContext context, String name);

    /**
     * Resolves this context against the given object.
     * Returns a new sub-context as a child of this one where the result of `getContext()` returns the passed in object.
     *
     * @throws NullPointerException if the passed in object is null.
     */
    default ModelContext resolve(Object child) {
        return resolve(child, getLocals());
    }

    /**
     * Resolves this context against the given object and local variables.
     * Returns a new sub-context as a child of this one where the result of `getContext()` returns the passed in object.
     *
     * @throws NullPointerException if the passed in object is null.
     */
    ModelContext resolve(Object child, Locals locals);

    /**
     * Interface for accessing contextual values.
     * <p>
     * This typically includes variables and other things that only become available
     * until after the parent model has been resolved.
     */
    interface Locals extends CommonLocals {
        /**
         * Gets a completed local variable.
         */
        CompletableFuture<Float> getLocal(String name);
    }
}
