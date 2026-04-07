package com.minelittlepony.mson.impl.model.json.elements;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.export.ModelFileWriter;
import com.minelittlepony.mson.api.parser.ModelComponent;
import com.minelittlepony.mson.api.parser.locals.LocalBlock;
import com.minelittlepony.mson.impl.MsonImpl;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a model part who's contents are loaded from another file.
 *
 * @author Sollace
 */
public record JsonImport(
        CompletableFuture<FileContent<?>> file,
        /**
         * The optional locals block.
         */
        Optional<LocalBlock> locals,
        String name
    ) implements ModelComponent<ModelPart> {
    public static final Identifier ID = MsonImpl.id("import");

    public JsonImport(FileContent<JsonElement> context, String name, JsonPrimitive file) {
        this(context.resolve(file), Optional.empty(), name);
    }

    public JsonImport(FileContent<JsonElement> context, String name, JsonElement json) {
        this(context, name, json.getAsJsonObject());
    }

    public JsonImport(FileContent<JsonElement> context, String name, JsonObject json) {
        this(
            context.resolve(json.get("data")),
            Optional.of(LocalBlock.of(JsonUtil.accept(json, "locals"))),
            name.isEmpty() ? JsonUtil.accept(json, "name").map(JsonElement::getAsString).orElse("") : name
        );
        context.addNamedComponent(this.name, this);
    }

    @Override
    public ModelPart export(ModelContext context) {
        return context.computeIfAbsent(name, _ -> convertContextToTree(context.extendWith(file.get(),
            parent -> parent.extendWith(parent.modelId(), locals.map(l -> l.bind(context.getLocals())), Optional.empty())
        )));
    }

    @Override
    public void write(ModelContext context, ModelFileWriter writer) {
        try {
            FileContent<?> fileContent = file.get();
            Set<String> components = fileContent.getComponentNames().get();

            if (components.size() != 1) {
                throw new JsonParseException("Imported file must define exactly one part.");
            }

            String name = components.stream().findFirst().get();

            var boundContext = context.extendWith(file.get(),
                parent -> parent.extendWith(parent.modelId(), locals.map(l -> l.bind(context.getLocals())), Optional.empty())
            );

            writer.write(name, boundContext, fileContent.getComponent(name).get());
        } catch (Exception e) {
            throw new JsonParseException(e);
        }
    }

    private ModelPart convertContextToTree(ModelContext context) {
        Map<String, ModelPart> tree = new HashMap<>();
        context.getTree(tree);

        if (tree.size() != 1) {
            throw new JsonParseException("Imported file must define exactly one part.");
        }

        return tree.values().stream().findFirst().get();
    }
}
