package com.minelittlepony.mson.impl.model.json.elements;

import net.minecraft.client.model.ModelPart;
import net.minecraft.util.Identifier;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minelittlepony.mson.api.InstanceCreator;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.export.ModelFileWriter;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.api.parser.ModelComponent;
import com.minelittlepony.mson.api.parser.locals.LocalBlock;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.Optional;
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
public class JsonSlot<T> implements ModelComponent<T> {
    public static final Identifier ID = new Identifier("mson", "slot");

    /**
     * The object type produced by this slot.
     */
    private final Optional<InstanceCreator<T>> implementation;

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
        implementation = JsonUtil.accept(json, "implementation").map(JsonElement::getAsString).map(InstanceCreator::byName);
        data = context.resolve(json.get("data"));
        this.name = name.isEmpty() ? JsonUtil.require(json, "name", ID, context.getLocals().getModelId()).getAsString() : name;
        texture = JsonUtil.accept(json, "texture").map(JsonTexture::of);
        id = new Identifier("dynamic", context.getLocals().getModelId().getPath() + "/" + this.name);
        locals = LocalBlock.of(JsonUtil.accept(json, "locals"));

        context.addNamedComponent(this.name, this);
    }

    @Override
    public <K> Optional<K> tryExportTreeNodes(ModelContext context, Class<K> type) {
        if (implementation.isPresent() && !implementation.get().isCompatible(type)) {
            return Optional.empty();
        }
        Optional<K> value = tryExport(context, type);
        if (!implementation.isPresent()) {
            return Optional.empty();
        }
        return value;
    }

    @Nullable
    @Override
    public T export(ModelContext context) {
        return compile(context).result();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K> Optional<K> export(ModelContext context, InstanceCreator<K> customType) {
        CompiledSlot<T> compiled = compile(context);
        if (implementation.filter(i -> i.isCompatible(customType)).isPresent()) {
            return Optional.ofNullable((K)compiled.result());
        }
        return Optional.ofNullable(customType.createInstance(compiled.sourceContext(), ctx -> compiled.tree()));
    }

    private CompiledSlot<T> compile(ModelContext context) {
        return context.computeIfAbsent(name, key -> {
            ModelContext subContext = context.extendWith(data.get(),
                parent -> parent.extendWith(id, Optional.of(locals.bind(context.getLocals())), texture)
            );

            ModelPart tree = subContext.toTree();
            T result = implementation.map(type -> type.createInstance(subContext, ctx -> tree)).orElse(null);

            return new CompiledSlot<>(result, tree, subContext);
        });
    }

    @Override
    public void write(ModelContext context, ModelFileWriter writer) {
        try {
            FileContent<?> content = data.get();
            writer.writeTree(name, content, context.extendWith(content,
                parent -> parent.extendWith(id, Optional.of(locals.bind(context.getLocals())), texture)
            ));
        } catch (Exception e) {
            throw new JsonParseException(e);
        }
    }

    record CompiledSlot<T>(@Nullable T result, ModelPart tree, ModelContext sourceContext) {}

}
