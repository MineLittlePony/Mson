package com.minelittlepony.mson.api;

import net.minecraft.client.model.Model;
import net.minecraft.util.Identifier;

import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.impl.MsonImpl;

import java.util.function.Supplier;

/**
 * The main Mson class.
 *
 */
public interface Mson {

    /**
     * Gets the global Mson instance.
     */
    static Mson getInstance() {
        return MsonImpl.instance();
    }

    /**
     * Registers a model to be loaded by mson.
     *
     * The returned key can be used to create instances of the registered model <i>after</i> a resource manager reload.
     * See {@link MsonModelsReadyCallback} to receive events for when it is safe to do so.
     *
     * @param id             The model identifier.
     * @param implementation The class to instantiate.
     * @return A key to create instances of the registered model type.
     */
    <T extends Model & MsonModel> ModelKey<T> registerModel(Identifier id, Supplier<T> constructor);

    /**
     * Registers a custom component to load model json.
     *
     * The constructor function will be called with a JsonContext and JsonObject to parse into a JsonComponent.
     * Use this to extend Mson's json handling, or if you need more refined control over your model's json data.
     *
     * @param id            Identifier for the component type.
     * @param constructor   The component constructor.
     */
    void registerComponentType(Identifier id, JsonComponent.Constructor<?> constructor);

    /**
     * Gets the registry for adding entity renderers to the game.
     */
    EntityRendererRegistry getEntityRendererRegistry();
}
