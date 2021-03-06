package com.minelittlepony.mson.api.json;

import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * A json component.
 *
 * Consumes json data and "exports" a concrete model instance, or a piece of a model.
 */
public interface JsonComponent<T> {

    /**
     * Tries to export this component to the chosen type.
     * Returns an optional containing the result for a successful conversion.
     */
    default <K> Optional<K> tryExportTreeNodes(ModelContext context, Class<K> type) {
        return tryExport(context, type);
    }

    /**
     * Tries to export this component to the chosen type.
     * Returns an optional containing the result for a successful conversion.
     */
    @SuppressWarnings("unchecked")
    default <K> Optional<K> tryExport(ModelContext context, Class<K> type) {
        Object s;
        try {
            s = export(context);
        } catch (InterruptedException | ExecutionException e) {
            return Optional.empty();
        }

        if (s != null && type.isAssignableFrom(s.getClass())) {
            return Optional.of((K)s);
        }
        return Optional.empty();
    }

    /**
     * Creates an instance of this component's object type within the supplied model loading context.
     */
    T export(ModelContext context) throws InterruptedException, ExecutionException;

    /**
     * Constructor for creating a component.
     *
     * Accepts the json context and json to parse and return a new component instance.
     */
    @FunctionalInterface
    interface Factory<T> {
        /**
         * Accepts the json context and json to parse and return a new component instance.
         */
        JsonComponent<? extends T> loadJson(JsonContext context, String name, JsonObject json);
    }
}