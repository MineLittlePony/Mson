package com.minelittlepony.mson.impl.model;

import net.minecraft.util.Identifier;

import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.impl.ModelLocalsImpl;
import com.minelittlepony.mson.impl.JsonLocalsImpl;
import com.minelittlepony.mson.impl.Local;
import com.minelittlepony.mson.impl.key.ReflectedModelKey;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Represents an in-place insertion of another model file's contents
 * with the option to instantiate it to a custom type.
 *
 * @param <T> The type of the part produced.
 *              If the part is compatible with either ModelPart or Cuboid it may appear in the model's structure
 *              as a normal part, otherwise it has to be retrieved via ModelContext#findByName.
 *
 * @author Sollace
 */
public class JsonSlot<T> implements JsonComponent<T> {
    public static final Identifier ID = new Identifier("mson", "slot");

    private final ReflectedModelKey<T> implementation;

    /**
     * The contents of this slot either expressed at a map of child elements, or a string pointing to a file.
     */
    private final CompletableFuture<JsonContext> data;

    /**
     * The optional locals block.
     */
    private final Local.Block locals;

    /**
     * The optional texture with parameters inherited from the slot's outer context.
     * This texture is applied <b>instead of</b> the texture defined in the imported file.
     */
    private final Optional<Texture> texture;

    /**
     * The name that this slot is to be exposed as.
     */
    private final String name;

    @Override
    public <K> Optional<K> tryExportTreeNodes(ModelContext context, Class<K> type) {
        if (!implementation.isCompatible(type)) {
            return Optional.empty();
        }
        return tryExport(context, type);
    }

    public JsonSlot(JsonContext context, String name, JsonObject json) {
        implementation = ReflectedModelKey.fromJson(json);
        data = context.resolve(json.get("data"));
        this.name = name.isEmpty() ? JsonUtil.require(json, "name", " required by mson:slot component in " + context.getLocals().getModelId()).getAsString() : name;
        texture = JsonUtil.accept(json, "texture").map(JsonTexture::of);
        context.addNamedComponent(this.name, this);

        locals = Local.of(JsonUtil.accept(json, "locals"));
    }

    @Override
    public T export(ModelContext context) {
        return context.computeIfAbsent(name, key -> {
            JsonContext jsContext = data.get();
            ModelContext subContext = jsContext.createContext(context.getModel(), new ModelLocalsImpl(implementation.getId(), new Locals(jsContext)));

            T inst = implementation.createModel(subContext);
            if (inst instanceof MsonModel) {
                ((MsonModel)inst).init(subContext.resolve(context.getContext()));
            }

            return inst;
        });
    }

    private class Locals implements JsonLocalsImpl {
        private final JsonContext.Locals parent;

        Locals(JsonContext parent) {
            this.parent = parent.getLocals();
        }

        @Override
        public Identifier getModelId() {
            return parent.getModelId();
        }

        @Override
        public CompletableFuture<float[]> getDilation() {
            return parent.getDilation();
        }

        @Override
        public CompletableFuture<Texture> getTexture() {
            return texture
                    .map(CompletableFuture::completedFuture)
                    .orElseGet(parent::getTexture);
        }

        @Override
        public CompletableFuture<Incomplete<Float>> getLocal(String name) {
            return locals.get(name).orElseGet(() -> parent.getLocal(name));
        }

        @Override
        public CompletableFuture<Set<String>> keys() {
            return parent.keys().thenApplyAsync(locals::appendKeys);
        }
    }
}
