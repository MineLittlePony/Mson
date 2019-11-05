package com.minelittlepony.mson.api.json;

import net.minecraft.client.model.Model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;

public interface JsonContext {

    <T> void addNamedComponent(String name, JsonComponent<T> component);

    <T> JsonComponent<T> loadComponent(JsonElement json);

    ModelContext createContext(Model model);

    interface Constructor<T> {
        JsonComponent<? extends T> loadJson(JsonContext context, JsonObject json);
    }
}
