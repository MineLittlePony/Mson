package com.minelittlepony.mson.api;

import net.minecraft.util.Identifier;

import com.minelittlepony.mson.api.model.Texture;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface CommonLocals {
    /**
     * Gets the current model's id.
     * This corresponds to the id of the key used to register that model.
     */
    Identifier getModelId();

    /**
     * Gets the texture information from the enclosing context or its parent.
     */
    CompletableFuture<Texture> getTexture();

    /**
     * Gets the local dilation to be applied for a component.
     */
    CompletableFuture<float[]> getDilation();

    /**
     * Gets a set containing the names of all the variables available in this scope.
     */
    CompletableFuture<Set<String>> keys();
}
