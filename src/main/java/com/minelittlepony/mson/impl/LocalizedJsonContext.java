package com.minelittlepony.mson.impl;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.Texture;

import java.util.concurrent.CompletableFuture;

class LocalizedJsonContext implements ModelContext.Locals {

    private final JsonContext context;

    LocalizedJsonContext(JsonContext context) {
        this.context = context;
    }

    @Override
    public CompletableFuture<Texture> getTexture() {
        return context.getTexture();
    }
}
