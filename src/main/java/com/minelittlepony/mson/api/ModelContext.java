package com.minelittlepony.mson.api;

import net.minecraft.client.model.ModelPart;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.api.parser.ModelComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * The loading context for when a model is first created.
 *
 * This allows access to getting out named elements from the model json.
 */
public interface ModelContext extends ModelView {
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
     * Binds this model context to a new object.
     * <p>
     * Effectively changes the value returned by {@link #getThis()} to that of the value passed in as {thisObj}.
     * The returned context will have the same locals as this one.
     *
     * @param thisObj The new this value.
     *
     * @throws NullPointerException if the passed in this value is null.
     */
    default ModelContext bind(Object thisObj) {
        return bind(thisObj, Function.identity());
    }

    /**
     * Binds this model context to a new object and locals pool.
     * <p>
     * Effectively changes the value returned by {@link #getThis()} to that of the value passed in as {thisObj}.
     * The returned context will inherit the locals computed by the passed in {inheritedLocals} function.
     *
     * @param thisObj The new this value.
     * @param inheritedLocals A function to compute the new context's locals blocks.
     *
     * @throws NullPointerException if the passed in this value is null.
     */
    ModelContext bind(Object thisObj, Function<Locals, Locals> inheritedLocals);

    /**
     * Creates a new model context from the passed in content that uses the current model context as its parent and inherits
     * the locals as computed by the supplied {inheritLocals} function.
     */
    default ModelContext extendWith(FileContent<?> content, Function<FileContent.Locals, FileContent.Locals> inheritedLocals) {
        return content.createContext(getModel(), inheritedLocals.apply(content.getLocals()).bake());
    }

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
