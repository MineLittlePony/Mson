package com.minelittlepony.mson.api.parser;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface ModelLoader {
    ResourceManager getResourceManager();

    CompletableFuture<FileContent<?>> loadModel(Identifier modelId, @Nullable ModelFormat<?> preferredFormat);
}
