package com.minelittlepony.mson.api;

import net.minecraft.client.model.Model;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.mson.api.model.Texture;

import java.util.Set;

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
