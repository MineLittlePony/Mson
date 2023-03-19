package com.minelittlepony.mson.api;

import net.minecraft.client.model.ModelPart;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.mson.api.parser.ModelComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The loading context for when a model is first created.
 *
 * This allows access to getting out named elements from the model json.
 */
public interface ModelContext extends ModelView {
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
     * Converts the entire model tree into native objects and returns in a root ModelPart.
     */
    default ModelPart toTree() {
        Map<String, ModelPart> tree = new HashMap<>();
        getTree(tree);
        return new ModelPart(new ArrayList<>(), tree);
    }

    /**
     * Converts the entire model tree into native objects, outputting into the provided output object.
     */
    void getTree(Map<String, ModelPart> tree);

    /**
     * Checks if a value has been stored for the given name.
     * If one was not found, creates one using the provided factory function.
     *
     * Will always return a new instance if the name is empty or null.
     */
    <T> T computeIfAbsent(@Nullable String name, FutureFunction<T> factory);

    /**
     * Finds a component with the matching name.
     */
    Optional<ModelComponent<?>> findComponent(String name);

    /**
     * Resolves this context against the given object.
     * Returns a new sub-context as a child of this one where the result of `getContext()` returns the passed in object.
     *
     * @param child The object instance to serve as the new immediate context.
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
     * @param child The object instance to serve as the new immediate context.
     * @param locals The new local variables.
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
        CompletableFuture<Float> getLocal(String name, float defaultValue);

        default CompletableFuture<Float> getLocal(String name) {
            return getLocal(name, 0F);
        }
    }
}
