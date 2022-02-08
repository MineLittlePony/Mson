package com.minelittlepony.mson.api.json;

import net.minecraft.client.model.Model;
import net.minecraft.util.Identifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.minelittlepony.mson.api.CommonLocals;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.impl.skeleton.JsonSkeleton;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * The json loading context.
 *
 * Allows for components to read and write contextual information during parsing.
 */
public interface JsonContext {
    /**
     * Registers a component with a name to the enclosing scope.
     */
    <T> void addNamedComponent(String name, JsonComponent<T> component);

    /**
     * Loads a json block into a component.
     *
     * Defers  to the component-types pipeline to return the corresponding instance to that of the passed in json.
     */
    <T> Optional<JsonComponent<T>> loadComponent(JsonElement json, Identifier defaultAs);

    /**
     * Loads a json block into a component.
     *
     * Defers  to the component-types pipeline to return the corresponding instance to that of the passed in json.
     */
    <T> Optional<JsonComponent<T>> loadComponent(String name, JsonElement json, Identifier defaultAs);

    /**
     * Creates a new model context for the supplied model instance.
     */
    ModelContext createContext(Model model, ModelContext.Locals locals);

    /**
     * Queries this context for all of the available component names.
     *
     * @return A set of all component names.
     */
    CompletableFuture<Set<String>> getComponentNames();

    /**
     * Resolves a new json context against the passed in json block.
     *
     * The new context is independent of this one, with all named components
     * and variables referenced to its own root context.
     *
     * If the json contains an id referencing another file, that file will be loaded asynchronously alongside this one.
     * Otherwise the json tree itself serves as the contents, and the new context is resolved immediately upon return.
     */
    CompletableFuture<JsonContext> resolve(JsonElement json);

    /**
     * Gets the local variable resolver for this context.
     */
    Locals getLocals();

    /**
     * Gets the model's declared skeleton.
     * <p>
     * This is optional metadata that can be used by mods who need to know how to put the model together for animations.
     */
    Optional<JsonSkeleton> getSkeleton();

    /**
     * Interface for accessing contextual values.
     * <p>
     * This typically includes variables and other things that only become available
     * until after the parent model has been resolved.
     * <p>
     * Incompletes are variable-based and require a ModelContext in order to resolve.
     */
    public interface Locals extends CommonLocals {
        /**
         * Reads a json primitive into an incomplete float value.
         */
        Incomplete<Float> get(JsonPrimitive json);

        /**
         * Converts an array of json primitives into an incomplete float array.
         */
        Incomplete<float[]> get(JsonPrimitive... arr);

        /**
         * Reads a json member into an incomplete float array.
         */
        Incomplete<float[]> get(JsonObject json, String member, int len);

        /**
         * Reads a json member into an incomplete holding a unresolved float.
         * Variables in the array are resolved against the model context when requested.
         */
        Incomplete<Float> get(JsonObject json, String member);

        /**
         * Gets a local variable as an incomplete float.
         */
        CompletableFuture<Incomplete<Float>> getLocal(String name);
    }
}
