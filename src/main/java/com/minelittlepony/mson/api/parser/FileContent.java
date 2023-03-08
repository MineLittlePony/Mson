package com.minelittlepony.mson.api.parser;

import net.minecraft.client.model.Model;
import net.minecraft.util.Identifier;

import com.minelittlepony.mson.api.CommonLocals;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.model.traversal.Traversable;
import com.minelittlepony.mson.impl.model.EmptyFileContent;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * The json loading context.
 *
 * Allows for components to read and write contextual information during parsing.
 */
@SuppressWarnings("unchecked")
public interface FileContent<Data> {

    static <T> FileContent<T> empty() {
        return (FileContent<T>)EmptyFileContent.INSTANCE;
    }

    /**
     * Registers a component with a name to the enclosing scope.
     *
     * @param The name of the component to make available
     * @param The component instance (often this)
     */
    <T> void addNamedComponent(String name, ModelComponent<T> component);

    /**
     * Loads a json block into a component.
     *
     * Defers  to the component-types pipeline to return the corresponding instance to that of the passed in json.
     *
     * @param json The json element to parse
     * @param defaultAs The default type to assume when the supplied json structure does not define one.
     */
    default <T> Optional<ModelComponent<T>> loadComponent(Data data, Identifier defaultAs) {
        return loadComponent("", data, defaultAs);
    }

    /**
     * Loads a json block into a component.
     *
     * Defers  to the component-types pipeline to return the corresponding instance to that of the passed in json.
     *
     * @param name The name to assign to the loaded component.
     * @param json The json element to parse
     * @param defaultAs The default type to assume when the supplied json structure does not define one.
     */
    <T> Optional<ModelComponent<T>> loadComponent(String name, Data data, Identifier defaultAs);

    /**
     * Creates a new model context for the supplied model instance and local variables.
     *
     * @param model The model being instantiated with this context.
     * @param locals The relevant local variables and inherited references.
     */
    ModelContext createContext(Model model, ModelContext.Locals locals);

    /**
     * Queries this context for all of the available component names.
     *
     * @return A set of all component names.
     */
    CompletableFuture<Set<String>> getComponentNames();

    /**
     * Resolves a new json context against the passed in json block.
     *
     * The new context is independent of this one, with all named components
     * and variables referenced to its own root context.
     *
     * If the json contains an id referencing another file, that file will be loaded asynchronously alongside this one.
     * Otherwise the json tree itself serves as the contents, and the new context is resolved immediately upon return.
     *
     * @param json The json structure to parse
     */
    CompletableFuture<FileContent<?>> resolve(Data data);

    /**
     * Gets the local variable resolver for this context.
     */
    Locals getLocals();

    /**
     * Gets the model's declared skeleton.
     * <p>
     * This is optional metadata that can be used by mods who need to know how to put the model together for animations.
     */
    Optional<Traversable<String>> getSkeleton();

    /**
     * Interface for accessing contextual values.
     * <p>
     * This typically includes variables and other things that only become available
     * until after the parent model has been resolved.
     * <p>
     * Incompletes are variable-based and require a ModelContext in order to resolve.
     */
    public interface Locals extends CommonLocals {
        /**
         * Gets a local variable as an incomplete float.
         */
        CompletableFuture<Incomplete<Float>> getLocal(String name);

        /**
         * Creates a frozen copy of this file's locals
         */
        ModelContext.Locals bake();
    }
}
