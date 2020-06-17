package com.minelittlepony.mson.api.json;

import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.util.Incomplete;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface JsonVariables {
    /**
     * Gets the texture information from the enclosing context or its parent.
     */
    CompletableFuture<Texture> getTexture();

    /**
     * Gets the variable lookup mechanism.
     */
    Variables getVarLookup();

    /**
     * Gets a local variable from this context.
     */
    CompletableFuture<Incomplete<Float>> getLocalVariable(String name);

    /**
     * Gets a set of all named variables.
     */
    CompletableFuture<Set<String>> getVariableNames();
}
