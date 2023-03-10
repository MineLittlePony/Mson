package com.minelittlepony.mson.impl.model.bbmodel;

import net.minecraft.client.model.Model;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.api.parser.ModelComponent;
import com.minelittlepony.mson.api.parser.ModelFormat;
import com.minelittlepony.mson.api.parser.ModelLoader;
import com.minelittlepony.mson.impl.ModelContextImpl;
import com.minelittlepony.mson.impl.ModelLocalsImpl;
import com.minelittlepony.mson.impl.model.EmptyFileContent;
import com.minelittlepony.mson.impl.model.RootContext;
import com.minelittlepony.mson.impl.model.bbmodel.elements.BbCube;
import com.minelittlepony.mson.impl.model.bbmodel.elements.BbPart;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BlockBenchFileContent implements JsonContext {

    private final ModelLoader loader;
    private final ModelFormat<JsonElement> format;

    public final Map<UUID, BbCube> cubes = new HashMap<>();
    private final Map<String, ModelComponent<?>> elements = new HashMap<>();

    private final FileContent.Locals locals;

    public BlockBenchFileContent(ModelLoader loader, ModelFormat<JsonElement> format, Identifier id, JsonObject json) {
        this.loader = loader;
        this.format = format;
        this.locals = new RootVariables(id, json);

        JsonUtil.require(json, "elements", id).getAsJsonArray().asList().stream().forEach(element -> {
            loadComponent(element, BbCube.ID).ifPresent(component -> {
                if (((ModelComponent<?>)component) instanceof BbCube cube) {
                    cube.uuid.ifPresent(uuid -> {
                        cubes.put(uuid, cube);
                    });
                }
            });
        });

        JsonUtil.accept(json, "outliner")
            .map(JsonElement::getAsJsonArray)
            .stream()
            .flatMap(e -> e.asList().stream())
            .map(JsonElement::getAsJsonObject)
            .forEach(element -> loadComponent(element, BbPart.ID));
    }

    @Override
    public ModelFormat<JsonElement> getFormat() {
        return format;
    }

    @Override
    public CompletableFuture<Set<String>> getComponentNames() {
        return CompletableFuture.completedFuture(elements.keySet());
    }

    @Override
    public <T> void addNamedComponent(String name, ModelComponent<T> component) {
        if (!Strings.isNullOrEmpty(name)) {
            elements.put(name, component);
        }
    }

    @Override
    public <T> Optional<ModelComponent<T>> loadComponent(String name, JsonElement json, Identifier defaultAs) {
        if (!json.isJsonObject()) {
            try {
                @SuppressWarnings("unchecked")
                ModelComponent<T> cube = (ModelComponent<T>)cubes.getOrDefault(UUID.fromString(json.getAsString()), null);

                if (cube != null) {
                    return Optional.of(cube);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return JsonContext.super.loadComponent(name, json, defaultAs);
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

        return CompletableFuture.completedFuture(new BlockBenchFileContent(loader, format, autoGen, file));
    }

    @Override
    public ModelContext createContext(Model model, ModelContext.Locals locals) {
        return new RootContext(model, (ModelContextImpl)EmptyFileContent.INSTANCE.createContext(model, locals), elements, locals);
    }

    @Override
    public FileContent.Locals getLocals() {
        return locals;
    }

    public static class RootVariables implements FileContent.Locals {
        private final Identifier id;
        private final CompletableFuture<Texture> texture;
        private final CompletableFuture<float[]> dilate = CompletableFuture.completedFuture(new float[] {1,1,1});

        RootVariables(Identifier id, JsonObject json) {
            this.id = id;
            texture = CompletableFuture.completedFuture(JsonUtil.accept(json, "resolution")
                    .map(JsonElement::getAsJsonObject)
                    .map(resolution -> {
                return new Texture(
                        0,
                        0,
                        JsonHelper.getInt(resolution, "width", 64),
                        JsonHelper.getInt(resolution, "height", 64)
                );
            }).orElse(Texture.EMPTY));
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
        public CompletableFuture<Incomplete<Float>> getLocal(String name) {
            return CompletableFuture.completedFuture(Incomplete.ZERO);
        }

        @Override
        public CompletableFuture<Set<String>> keys() {
            return CompletableFuture.completedFuture(new HashSet<>());
        }

        @Override
        public ModelContext.Locals bake() {
            return new ModelLocalsImpl(this);
        }
    }
}
