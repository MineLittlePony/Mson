package com.minelittlepony.mson.api;

import net.minecraft.util.Identifier;

import com.minelittlepony.mson.api.model.Texture;

public interface ModelMetadata {
    /**
     * Gets the current model's id.
     * This corresponds to the id of the key used to register that model.
     */
    Identifier getModelId();

    /**
     * Gets the texture information from the enclosing context or its parent.
     */
    Texture getTexture();

    /**
     * Gets the local dilation to be applied for a component.
     */
    float[] getDilation();
}
