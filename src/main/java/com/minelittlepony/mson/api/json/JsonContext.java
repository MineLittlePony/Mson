package com.minelittlepony.mson.api.json;

import net.minecraft.client.model.Model;
import net.minecraft.util.Identifier;

import com.google.gson.JsonElement;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.model.Texture;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The json loading context.
 *
 * Allows for components to read and write contextual information during parsing.
 */
public interface JsonContext {

    /**
     * Gets the texture information from the enclosing context or its parent.
     */
    CompletableFuture<Texture> getTexture();

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
     * Creates a new model context for the supplied model instance.
     */
    ModelContext createContext(Model model);

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
}
