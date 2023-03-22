package com.minelittlepony.mson.api;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.mson.api.model.Texture;

import java.util.Set;
import java.util.function.Function;

/**
 * Represents a managed view of the current model being constructed.
 *
 * This allows access to getting out named elements from the model json.
 */
public interface ModelView {
    /**
     * Gets the immediate object in this context.
     * May be the same as the model if called on the root context.
     *
     * Otherwise it is the object this context was resolved against.
     *
     * @throws ClassCastException if the context doesn't match the requested type.
     */
    @Nullable
    <T> T getThis() throws ClassCastException;

    /**
     * Gets the root context.
     * Returns `this` when called on the root context.
     */
    ModelView getRoot();

    /**
     * Provides access to the contextual information for the current context.
     * <p>
     * Includes access to inherited values and properties.
     */
    Locals getLocals();

    /**
     * Gets the currently-active model instance.
     */
    @Nullable
    <T extends Model> T getModel();

    /**
     * Gets a value from this context's local variable table.
     */
    float getLocalValue(String name, float defaultValue);

    /**
     * Gets the named element and returns an instance of the requested type.
     *
     * @throws ClassCastException if the requested named element does not use the requested implementation.
     * @throws InvalidInputException if the named element does not exist.
     */
    <T> T findByName(String name);

    /**
     * Gets the named element after converting to a particular custom object type.
     *
     * @apiNote Experimental
     */
    <T> T findByName(String name, Function<ModelPart, T> function);

    /**
     * Gets the named element after converting to a particular custom object type.
     * <p>
     * This is the poloymphic version. If the value created by the queried
     * component is a subtype of {rootType} will return that instead, otherwise will try to
     * create an instance using the supplied function.
     *
     * @apiNote Experimental
     */
    <T> T findByName(String name, Function<ModelPart, T> function, Class<T> rootType);

    /**
     * Interface for accessing contextual values.
     * <p>
     * This typically includes variables and other things that only become available
     * until after the parent model has been resolved.
     */
    interface Locals extends CommonLocals {
        /**
         * Gets the texture information from the enclosing context or its parent.
         */
        Texture getTexture();

        /**
         * Gets the local dilation to be applied for a component.
         */
        float[] getDilation();

        /**
         * Gets a set containing the names of all the variables available in this scope.
         */
        Set<String> keys();

        /**
         * Gets a completed local variable.
         */
        float getLocal(String name, float defaultValue);

        default float getLocal(String name) {
            return getLocal(name, 0F);
        }
    }
}
