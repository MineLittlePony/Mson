package com.minelittlepony.mson.api.json;

import net.minecraft.client.model.Model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.model.Texture;

import java.util.function.Supplier;

public interface JsonContext {

    Texture getTexture();

    <T> void addNamedComponent(String name, JsonComponent<T> component);

    <T> JsonComponent<T> loadComponent(JsonElement json);

    ModelContext createContext(Model model);

    Supplier<JsonContext> resolve(JsonElement json);

    interface Constructor<T> {
        JsonComponent<? extends T> loadJson(JsonContext context, JsonObject json);
    }
}
