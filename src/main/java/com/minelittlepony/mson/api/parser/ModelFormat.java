package com.minelittlepony.mson.api.parser;

import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import com.minelittlepony.mson.api.export.ModelSerializer;

import java.util.Optional;

/**
 * Represents a distinct model format.
 *
 * Objects of this type serves as a translation layer that can turn a block of raw data into a FileContent instance.
 *
 * @param <Data> The type of data that this format consumes (typically json).
 */
public interface ModelFormat<Data> {
    Identifier MSON_V2 = new Identifier("mson", "json");

    /**
     * Mson *.json files
     */
    Identifier MSON = MSON_V2;

    /**
     * BlockBench *.bbmodel files
     */
    Identifier BBMODEL = new Identifier("blockbench", "bbmodel");

    /**
     * The file extension that this format is capable of parsing.
     */
    String getFileExtension();

    /**
     * Creates a serializer that converts a loaded model back into this format.
     */
    default Optional<ModelSerializer<FileContent<?>>> createSerializer() {
        return Optional.empty();
    }

    /**
     * Loads a model for the requested model id using the provided asset loader.
     *
     * @param id The identifier of the model to load.
     * @param loader  A model loader
     * @return The new FileContent instance if parsing succeeded, otherwise an empty optional.
     */
    Optional<FileContent<Data>> loadModel(Identifier id, ModelLoader loader);

    /**
     * Loads a model from a provided filepath and resource.
     *
     *
     * @param id The identifer of the model to load.
     * @param file The direct filepath of the model to load
     * @param source A resource corresponding to the requested file
     * @param loader  A model loader
     * @return The new FileContent instance if parsing succeeded, otherwise an empty optional.
     */
    Optional<FileContent<Data>> loadModel(Identifier id, Identifier file, Resource resource, boolean failHard, ModelLoader loader);

    /**
     * Registers a custom component to load data for this format.
     *
     * The constructor function will be called with a FileContent and a data fragment to parse into a ModelComponent instance.
     * Use this to extend Mson's handling, or if you need more refined control over your model's data structure once parsed.
     *
     * @param id            Identifier for the component type.
     * @param constructor   The component constructor.
     */
    default void registerComponentType(Identifier id, ModelComponent.Factory<?, Data> constructor) {
        throw new UnsupportedOperationException();
    }

    /**
     * Loads a component from a provided data fragment.
     */
    default <T> Optional<ModelComponent<T>> loadComponent(Data data, Identifier defaultAs, FileContent<Data> context) {
        return loadComponent("", data, defaultAs, context);
    }

    /**
     * Loads a component from a provided data fragment.
     */
    <T> Optional<ModelComponent<T>> loadComponent(String name, Data data, Identifier defaultAs, FileContent<Data> context);
}
