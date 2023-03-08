package com.minelittlepony.mson.api;

import net.minecraft.client.model.Model;
import net.minecraft.util.Identifier;

import com.google.gson.JsonElement;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.parser.ModelFormat;
import com.minelittlepony.mson.impl.MsonImpl;

import java.util.Optional;

/**
 * The main Mson class.
 *
 */
@SuppressWarnings("removal")
public interface Mson {

    /**
     * Gets the global Mson instance.
     */
    static Mson getInstance() {
        return MsonImpl.INSTANCE;
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
    <T extends Model> ModelKey<T> registerModel(Identifier id, MsonModel.Factory<T> constructor);

    /**
     * Registers a custom component to load model json.
     *
     * The constructor function will be called with a JsonContext and JsonObject to parse into a JsonComponent.
     * Use this to extend Mson's json handling, or if you need more refined control over your model's json data.
     *
     * @param id            Identifier for the component type.
     * @param constructor   The component constructor.
     *
     * @deprecated Call Mson.getInstance().getDefaultFormatHandler().registerComponentType(id, constructor) instead.
     */
    @Deprecated(forRemoval = true)
    default void registerComponentType(Identifier id, JsonComponent.Factory<?> constructor) {
        getDefaultFormatHandler().registerComponentType(id, constructor);
    }

    /**
     * Gets the format handler responsible for loading MSON json models.
     */
    ModelFormat<JsonElement> getDefaultFormatHandler();

    /**
     * Registers a format handler for loading custom models.
     * @param format The model format handler to register.
     *
     * @return The supplied format handler.
     */
    <Data, T extends ModelFormat<Data>> T registerModelFormatHandler(Identifier id, T format);

    /**
     * Gets a format handler registered with a particular id.
     *
     * For a list of built-in formats, see ModelFormat
     */
    <Data> Optional<ModelFormat<Data>> getFormatHandler(Identifier id);

    /**
     * Gets the registry for adding entity renderers to the game.
     */
    EntityRendererRegistry getEntityRendererRegistry();
}
