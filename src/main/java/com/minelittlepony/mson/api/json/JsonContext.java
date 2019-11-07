package com.minelittlepony.mson.api.json;

import net.minecraft.client.model.Model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.model.Texture;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface JsonContext {

    CompletableFuture<Texture> getTexture();

    <T> void addNamedComponent(String name, JsonComponent<T> component);

    <T> Optional<JsonComponent<T>> loadComponent(JsonElement json);

    ModelContext createContext(Model model);

    CompletableFuture<JsonContext> resolve(JsonElement json);

    interface Constructor<T> {
        JsonComponent<? extends T> loadJson(JsonContext context, JsonObject json);
    }
}
