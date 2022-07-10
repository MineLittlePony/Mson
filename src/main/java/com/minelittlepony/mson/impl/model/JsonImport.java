package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.util.Identifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.impl.ModelLocalsImpl;
import com.minelittlepony.mson.impl.JsonLocalsImpl;
import com.minelittlepony.mson.impl.Local;
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
public class JsonImport implements JsonComponent<ModelPart> {
    public static final Identifier ID = new Identifier("mson", "import");

    private final CompletableFuture<JsonContext> file;

    /**
     * The optional locals block.
     */
    private final Optional<Local.Block> locals;

    private final String name;

    public JsonImport(JsonContext context, String name, JsonElement file) {
        this.name = name;
        this.file = context.resolve(file);
        this.locals = Optional.empty();
    }

    public JsonImport(JsonContext context, String name, JsonObject json) {
        this.name = name.isEmpty() ? JsonUtil.accept(json, "name").map(JsonElement::getAsString).orElse("") : name;
        file = context.resolve(json.get("data"));
        locals = Optional.of(Local.of(JsonUtil.accept(json, "locals")));

        context.addNamedComponent(this.name, this);
    }

    @Override
    public ModelPart export(ModelContext context) {
        return context.computeIfAbsent(name, key -> {
            JsonContext jsContext = file.get();
            ModelContext modelContext = jsContext.createContext(context.getModel(),
                new ModelLocalsImpl(new Locals(jsContext.getLocals()))
            );

            Map<String, ModelPart> tree = new HashMap<>();
            modelContext.getTree(tree);

            if (tree.size() != 0) {
                throw new JsonParseException("Imported file must define exactly one part.");
            }

            return tree.values().stream().findFirst().orElseThrow(() -> new JsonParseException("Imported file must define exactly one part."));
        });
    }

    private class Locals implements JsonLocalsImpl {
        private final JsonContext.Locals parent;

        Locals(JsonContext.Locals parent) {
            this.parent = parent;
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
            return parent.getTexture();
        }

        @Override
        public CompletableFuture<Incomplete<Float>> getLocal(String name) {
            return locals.flatMap(locals -> locals.get(name)).orElseGet(() -> parent.getLocal(name));
        }

        @Override
        public CompletableFuture<Set<String>> keys() {
            return locals
                    .map(locals -> parent.keys().thenApplyAsync(locals::appendKeys))
                    .orElseGet(() -> parent.keys());
        }
    }
}
