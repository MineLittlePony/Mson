package com.minelittlepony.mson.api.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.api.parser.ModelComponent;

@Deprecated(forRemoval = true)
public interface JsonComponent<T> extends ModelComponent<T> {
    /**
     * Constructor for creating a component.
     *
     * Accepts the json context and json to parse and return a new component instance.
     */
    @Deprecated(forRemoval = true)
    @FunctionalInterface
    interface Factory<T> extends ModelComponent.Factory<T, JsonElement> {
        /**
         * Accepts the json context and json to parse and return a new component instance.
         */
        ModelComponent<? extends T> load(JsonContext context, String name, JsonObject data);

        @Override
        default ModelComponent<? extends T> load(FileContent<JsonElement> context, String name, JsonElement data) {
            return load((JsonContext)context, name, data.getAsJsonObject());
        }
    }
}
