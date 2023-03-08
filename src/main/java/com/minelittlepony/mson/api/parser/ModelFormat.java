package com.minelittlepony.mson.api.parser;

import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import java.util.Optional;

public interface ModelFormat<Data> {
    Identifier MSON_V2 = new Identifier("mson", "json");
    Identifier MSON = MSON_V2;
    Identifier BBMODEL = new Identifier("blockbench", "bbmodel");

    String getFileExtension();

    Optional<FileContent<Data>> loadModel(Identifier modelId, ModelLoader loader);

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

    default <T> Optional<ModelComponent<T>> loadComponent(Data data, Identifier defaultAs, FileContent<Data> context) {
        return loadComponent("", data, defaultAs, context);
    }

    <T> Optional<ModelComponent<T>> loadComponent(String name, Data data, Identifier defaultAs, FileContent<Data> context);
}
