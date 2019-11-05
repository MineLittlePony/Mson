package com.minelittlepony.mson.api.json;

import com.google.gson.JsonObject;

public interface JsonContext {

    <T> void addNamedComponent(String name, JsonComponent<T> component);

    <T> JsonComponent<T> loadComponent(JsonObject json);

    interface Constructor<T> {
        JsonComponent<? extends T> loadJson(JsonContext context, JsonObject json);
    }
}
