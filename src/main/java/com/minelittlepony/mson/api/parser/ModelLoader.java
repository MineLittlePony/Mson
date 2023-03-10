package com.minelittlepony.mson.api.parser;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * An a synchronous model loader.
 */
public interface ModelLoader {
    /**
     * Gets the current resource manager
     */
    ResourceManager getResourceManager();

    /**
     * Asynchronously loads a model corresponding to an and optional preferred format.
     *
     * If a model file corresponding to the requested format exists, this loaded will load and return that one
     * first, otherwise will parse and return the first matching resource using the format matching its own.
     */
    CompletableFuture<FileContent<?>> loadModel(Identifier modelId, @Nullable ModelFormat<?> preferredFormat);
}
