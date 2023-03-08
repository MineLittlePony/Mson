package com.minelittlepony.mson.impl.model.json.elements;

import net.minecraft.util.Identifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.api.parser.ModelComponent;
import com.minelittlepony.mson.api.parser.locals.LocalBlock;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.impl.key.ReflectedModelKey;
import com.minelittlepony.mson.impl.model.FileContentLocalsImpl;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
public class JsonSlot<T> implements ModelComponent<T> {
    public static final Identifier ID = new Identifier("mson", "slot");

    @Deprecated
    private final ReflectedModelKey<T> implementation;

    /**
     * The contents of this slot either expressed at a map of child elements, or a string pointing to a file.
     */
    private final CompletableFuture<FileContent<?>> data;

    /**
     * The optional locals block.
     */
    private final LocalBlock locals;

    /**
     * The optional texture with parameters inherited from the slot's outer context.
     * This texture is applied <b>instead of</b> the texture defined in the imported file.
     */
    private final Optional<Texture> texture;

    /**
     * The name that this slot is to be exposed as.
     */
    private final String name;

    public JsonSlot(FileContent<JsonElement> context, String name, JsonElement json) {
        this(context, name, json.getAsJsonObject());
    }

    public JsonSlot(FileContent<JsonElement> context, String name, JsonObject json) {
        implementation = ReflectedModelKey.fromJson(json);
        data = context.resolve(json.get("data"));
        this.name = name.isEmpty() ? JsonUtil.require(json, "name", ID, context.getLocals().getModelId()).getAsString() : name;
        texture = JsonUtil.accept(json, "texture").map(JsonTexture::of);
        context.addNamedComponent(this.name, this);

        locals = LocalBlock.of(JsonUtil.accept(json, "locals"));
    }

    @Override
    public <K> Optional<K> tryExportTreeNodes(ModelContext context, Class<K> type) {
        if (!implementation.isCompatible(type)) {
            return Optional.empty();
        }
        return tryExport(context, type);
    }

    @Override
    public T export(ModelContext context) {
        return context.computeIfAbsent(name, key -> {
            FileContent<?> jsContext = data.get();
            ModelContext subContext = jsContext.createContext(context.getModel(), new Locals(jsContext).bake());

            T inst = implementation.createModel(subContext);
            if (inst instanceof MsonModel) {
                ((MsonModel)inst).init(subContext.resolve(context.getContext()));
            }

            return inst;
        });
    }

    @Override
    public <K> Optional<K> exportToType(ModelContext context, MsonModel.Factory<K> customType) throws InterruptedException, ExecutionException {
        return Optional.of(context.computeIfAbsent(name, key -> {
            FileContent<?> jsContext = data.get();
            ModelContext subContext = jsContext.createContext(context.getModel(), new Locals(jsContext).bake());
            return customType.create(subContext.toTree());
        }));
    }

    private class Locals implements FileContentLocalsImpl {
        private final FileContent.Locals parent;

        Locals(FileContent<?> parent) {
            this.parent = parent.getLocals();
        }

        @Override
        public Identifier getModelId() {
            return implementation.getId();
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
