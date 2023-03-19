package com.minelittlepony.mson.api.parser;

import com.minelittlepony.mson.api.InstanceCreator;
import com.minelittlepony.mson.api.ModelContext;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * A json component.
 *
 * Consumes data and "exports" a concrete model instance, or a piece of a model.
 */
public interface ModelComponent<T> {

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
     * Creates an instance of a custom type from the data stored in this components.
     * Returns an empty Optional if this component does not support conversion to the requested type.
     */
    default <K> Optional<K> exportToType(ModelContext context, InstanceCreator<K> customType) throws InterruptedException, ExecutionException {
        return Optional.empty();
    }

    /**
     * Constructor for creating a component.
     *
     * Accepts the json context and json to parse and return a new component instance.
     */
    @FunctionalInterface
    interface Factory<T, Data> {
        /**
         * Accepts the json context and json to parse and return a new component instance.
         */
        ModelComponent<? extends T> load(FileContent<Data> context, String name, Data data);
    }
}