package com.minelittlepony.mson.impl.model.json.elements;

import net.minecraft.client.model.ModelPart;
import net.minecraft.util.Identifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.parser.ModelComponent;
import com.minelittlepony.mson.api.parser.locals.LocalBlock;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a model part who's contents are loaded from another file.
 *
 * @author Sollace
 */
public class JsonImport implements ModelComponent<ModelPart> {
    public static final Identifier ID = new Identifier("mson", "import");

    private final CompletableFuture<FileContent<?>> file;

    /**
     * The optional locals block.
     */
    private final Optional<LocalBlock> locals;

    private final String name;

    public JsonImport(FileContent<JsonElement> context, String name, JsonPrimitive file) {
        this.name = name;
        this.file = context.resolve(file);
        this.locals = Optional.empty();
    }

    public JsonImport(FileContent<JsonElement> context, String name, JsonElement json) {
        this(context, name, json.getAsJsonObject());
    }

    public JsonImport(FileContent<JsonElement> context, String name, JsonObject json) {
        this.name = name.isEmpty() ? JsonUtil.accept(json, "name").map(JsonElement::getAsString).orElse("") : name;
        file = context.resolve(json.get("data"));
        locals = Optional.of(LocalBlock.of(JsonUtil.accept(json, "locals")));

        context.addNamedComponent(this.name, this);
    }

    @Override
    public ModelPart export(ModelContext context) {
        return context.computeIfAbsent(name, key -> convertContextToTree(context.extendWith(file.get(),
            parent -> parent.extendWith(parent.getModelId(), locals.map(l -> l.bind(context.getLocals())), Optional.empty())
        )));
    }

    private ModelPart convertContextToTree(ModelContext context) {
        Map<String, ModelPart> tree = new HashMap<>();
        context.getTree(tree);

        if (tree.size() != 1) {
            throw new JsonParseException("Imported file must define exactly one part.");
        }

        return tree.values().stream().findFirst().orElseThrow(() -> new JsonParseException("Imported file must define exactly one part."));
    }
}
