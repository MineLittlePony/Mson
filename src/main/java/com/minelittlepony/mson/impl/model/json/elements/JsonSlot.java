package com.minelittlepony.mson.impl.model.json.elements;

import net.minecraft.util.Identifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.InstanceCreator;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.api.parser.ModelComponent;
import com.minelittlepony.mson.api.parser.locals.LocalBlock;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.impl.model.FileContentLocalsImpl;
import com.minelittlepony.mson.util.JsonUtil;

import javax.annotation.Nullable;

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

    @Nullable
    private final InstanceCreator<T> implementation;

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

    private final Identifier id;

    public JsonSlot(FileContent<JsonElement> context, String name, JsonElement json) {
        this(context, name, json.getAsJsonObject());
    }

    public JsonSlot(FileContent<JsonElement> context, String name, JsonObject json) {
        implementation = json.has("implementation") ? InstanceCreator.byName(json.get("implementation").getAsString()) : null;
        data = context.resolve(json.get("data"));
        this.name = name.isEmpty() ? JsonUtil.require(json, "name", ID, context.getLocals().getModelId()).getAsString() : name;
        texture = JsonUtil.accept(json, "texture").map(JsonTexture::of);
        context.addNamedComponent(this.name, this);
        id = new Identifier("dynamic", context.getLocals().getModelId().getPath() + "/" + this.name);
        locals = LocalBlock.of(JsonUtil.accept(json, "locals"));
    }

    @Override
    public <K> Optional<K> tryExportTreeNodes(ModelContext context, Class<K> type) {
        if (implementation == null || !implementation.isCompatible(type)) {
            return Optional.empty();
        }
        return tryExport(context, type);
    }

    @Override
    public T export(ModelContext context) {
        return context.computeIfAbsent(name, key -> {
            ModelContext subContext = context.extendWith(data.get(), Locals::new);

            T inst = (implementation == null ? InstanceCreator.<T>ofPart() : implementation).createInstance(subContext);

            return inst;
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K> Optional<K> exportToType(ModelContext context, InstanceCreator<K> customType) throws InterruptedException, ExecutionException {
        ModelContext subContext = context.extendWith(data.get(), Locals::new);

        InstanceCreator<T> implementation = (this.implementation == null ? InstanceCreator.<T>ofPart() : this.implementation);

        if (implementation.isCompatible(customType)) {
            return Optional.of((K)implementation.createInstance(subContext));
        }

        return Optional.of(customType.createInstance(subContext));
    }

    private class Locals implements FileContentLocalsImpl {
        private final FileContent.Locals parent;

        Locals(FileContent.Locals parent) {
            this.parent = parent;
        }

        @Override
        public Identifier getModelId() {
            return id;
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
        public CompletableFuture<Incomplete<Float>> getLocal(String name, float defaultValue) {
            return locals.get(name).orElseGet(() -> parent.getLocal(name, defaultValue));
        }

        @Override
        public CompletableFuture<Set<String>> keys() {
            return parent.keys().thenApplyAsync(locals::appendKeys);
        }
    }
}
