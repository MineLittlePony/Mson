package com.minelittlepony.mson.api.parser;

import net.minecraft.client.model.geom.ModelPart;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.mson.api.InstanceCreator;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.export.ModelFileWriter;

import java.util.Optional;

/**
 * A json component.
 *
 * Consumes data and "exports" a concrete model instance, or a piece of a model.
 */
public interface ModelComponent<T> extends ModelFileWriter.Writeable {

    static boolean canConvertTo(Class<?> type, Class<?> clazz) {
        return type == null || InstanceCreator.isCompatible(type, clazz);
    }

    default boolean canConvertTo(ModelContext context, @Nullable Class<?> type) {
        return canConvertTo(type, outputType(context));
    }

    Class<?> outputType(ModelContext context);

    /**
     * Tries to export this component to the chosen type.
     * Returns an optional containing the result for a successful conversion.
     */
    default Optional<ModelPart> tryExportTreeNodes(ModelContext context) {
        return tryExport(context, ModelPart.class);
    }

    /**
     * Tries to export this component to the chosen type.
     * Returns an optional containing the result for a successful conversion.
     */
    @SuppressWarnings("unchecked")
    default <K> Optional<K> tryExport(ModelContext context, Class<K> type) {
        if (!canConvertTo(context, type)) {
            return Optional.empty();
        }
        Object s;
        try {
            s = export(context);
        } catch (Exception e) {
            return Optional.empty();
        }

        return Optional.ofNullable((K)s);
    }

    /**
     * Creates an instance of this component's object type within the supplied model loading context.
     */
    @Nullable
    T export(ModelContext context);

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