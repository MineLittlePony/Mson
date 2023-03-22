package com.minelittlepony.mson.impl.model.json;

import net.minecraft.client.model.Model;
import net.minecraft.util.Identifier;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.api.model.traversal.Traversable;
import com.minelittlepony.mson.api.parser.ModelComponent;
import com.minelittlepony.mson.api.parser.ModelFormat;
import com.minelittlepony.mson.api.parser.ModelLoader;
import com.minelittlepony.mson.api.parser.locals.LocalBlock;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.impl.ModelContextImpl;
import com.minelittlepony.mson.impl.model.EmptyFileContent;
import com.minelittlepony.mson.impl.model.FileContentLocalsImpl;
import com.minelittlepony.mson.impl.model.RootContext;
import com.minelittlepony.mson.impl.model.json.elements.JsonCompound;
import com.minelittlepony.mson.impl.model.json.elements.JsonTexture;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonFileContent implements JsonContext {

    private final ModelLoader loader;
    private final ModelFormat<JsonElement> format;

    private final Map<String, ModelComponent<?>> elements = new HashMap<>();

    private final CompletableFuture<FileContent<?>> parent;

    private final FileContent.Locals variables;

    private final Optional<Traversable<String>> skeleton;

    public JsonFileContent(ModelLoader loader, ModelFormat<JsonElement> format, Identifier id, JsonObject json) {
        this.loader = loader;
        this.format = format;
        parent = JsonUtil.accept(json, "parent")
            .map(JsonElement::getAsString)
            .map(Identifier::new)
            .map(parentId -> loader.loadModel(parentId, format))
            .orElseGet(() -> CompletableFuture.completedFuture(EmptyFileContent.INSTANCE));

        variables = new RootVariables(id, json, parent.thenApplyAsync(FileContent::getLocals));

        elements.putAll(getChildren(json).collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> loadComponent(entry.getKey(), entry.getValue(), JsonCompound.ID).orElseGet(null)
        )));

        skeleton = JsonUtil.accept(json, "skeleton")
                .map(JsonElement::getAsJsonObject)
                .map(JsonSkeleton::new);
    }

    private Stream<Map.Entry<String, JsonElement>> getChildren(JsonObject json) {
        if (json.has("data")) {
            return json.get("data").getAsJsonObject().entrySet().stream().filter(entry -> {
                return entry.getValue().isJsonObject() || (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString());
            });
        }
        return Stream.empty();
    }

    @Override
    public ModelFormat<JsonElement> getFormat() {
        return format;
    }

    @Override
    public CompletableFuture<Set<String>> getComponentNames() {
        return parent.thenComposeAsync(p -> p.getComponentNames()).thenApply(output -> {
            output.addAll(elements.keySet());
            return output;
        });
    }

    @Override
    public CompletableFuture<Optional<ModelComponent<?>>> getComponent(String name) {
        return parent.thenComposeAsync(p -> p.getComponent(name)).thenApply(component -> {
            return component.or(() -> Optional.ofNullable(elements.getOrDefault(name, null)));
        });
    }

    @Override
    public <T> void addNamedComponent(String name, ModelComponent<T> component) {
        if (!Strings.isNullOrEmpty(name)) {
            elements.put(name, component);
        }
    }

    @Override
    public <T> Optional<ModelComponent<T>> loadComponent(String name, JsonElement json, Identifier defaultAs) {
        return format.loadComponent(name, json, defaultAs, this);
    }

    @Override
    public Optional<Traversable<String>> getSkeleton() {
        return skeleton.or(() -> parent.getNow(EmptyFileContent.INSTANCE).getSkeleton());
    }

    @Override
    public CompletableFuture<FileContent<?>> resolve(JsonElement json) {

        if (json.isJsonPrimitive()) {
            return loader.loadModel(new Identifier(json.getAsString()), format);
        }

        Identifier id = getLocals().getModelId();
        Identifier autoGen = new Identifier(id.getNamespace(), id.getPath() + "_dynamic");

        if (json.getAsJsonObject().has("data")) {
            throw new JsonParseException("Dynamic model files should not have a nested data block");
        }

        JsonObject file = new JsonObject();
        file.add("data", json.getAsJsonObject());

        return CompletableFuture.completedFuture(new JsonFileContent(loader, format, autoGen, file));
    }

    @Override
    public ModelContext createContext(Model model, Object thisObj, ModelContext.Locals locals) {
        return new RootContext(model, thisObj, (ModelContextImpl)parent.getNow(EmptyFileContent.INSTANCE).createContext(model, thisObj, locals), elements, locals);
    }

    @Override
    public FileContent.Locals getLocals() {
        return variables;
    }

    public static class RootVariables implements FileContentLocalsImpl {
        private final Identifier id;
        private final CompletableFuture<FileContent.Locals> parent;
        private final CompletableFuture<Texture> texture;
        private final CompletableFuture<float[]> dilate;

        private final LocalBlock locals;

        RootVariables(Identifier id, JsonObject json, CompletableFuture<FileContent.Locals> parent) {
            this.id = id;
            this.parent = parent;
            texture = JsonTexture.unlocalized(JsonUtil.accept(json, "texture"), parent.thenComposeAsync(Locals::getTexture));
            locals = LocalBlock.of(JsonUtil.accept(json, "locals"));
            dilate = JsonUtil.acceptFloats(json, "dilate", new float[3])
                    .map(CompletableFuture::completedFuture)
                    .orElseGet(() -> parent.thenComposeAsync(FileContent.Locals::getDilation));
        }

        @Override
        public Identifier getModelId() {
            return id;
        }

        @Override
        public CompletableFuture<float[]> getDilation() {
            return dilate;
        }

        @Override
        public CompletableFuture<Texture> getTexture() {
            return texture;
        }

        @Override
        public CompletableFuture<Incomplete<Float>> getLocal(String name, float defaultValue) {
            return locals.get(name).orElseGet(() -> parent.thenComposeAsync(p -> p.getLocal(name, defaultValue)));
        }

        @Override
        public CompletableFuture<Set<String>> keys() {
            return parent.thenComposeAsync(Locals::keys).thenApply(locals::appendKeys);
        }
    }
}