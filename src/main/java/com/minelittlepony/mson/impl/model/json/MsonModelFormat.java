package com.minelittlepony.mson.impl.model.json;

import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.api.parser.ModelComponent;
import com.minelittlepony.mson.api.parser.ModelFormat;
import com.minelittlepony.mson.api.parser.ModelLoader;
import com.minelittlepony.mson.impl.MsonImpl;
import com.minelittlepony.mson.impl.model.json.elements.*;
import com.minelittlepony.mson.util.JsonUtil;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MsonModelFormat implements ModelFormat<JsonElement> {
    public static final ModelFormat<JsonElement> INSTANCE = new MsonModelFormat();
    private static final Gson GSON = new Gson();

    private final Map<Identifier, ModelComponent.Factory<?, JsonElement>> componentTypes = new HashMap<>();

    private MsonModelFormat() {
        componentTypes.put(JsonCompound.ID, JsonCompound::new);
        componentTypes.put(JsonBox.ID, JsonBox::new);
        componentTypes.put(JsonPlane.ID, JsonPlane::new);
        componentTypes.put(JsonPlanar.ID, JsonPlanar::new);
        componentTypes.put(JsonSlot.ID, JsonSlot::new);
        componentTypes.put(JsonCone.ID, JsonCone::new);
        componentTypes.put(JsonQuads.ID, JsonQuads::new);
        componentTypes.put(JsonImport.ID, JsonImport::new);
    }

    @Override
    public String getFileExtension() {
        return "json";
    }

    @Override
    public Optional<FileContent<JsonElement>> loadModel(Identifier modelId, ModelLoader loader) {
        Identifier file = new Identifier(modelId.getNamespace(), "models/" + modelId.getPath() + "." + getFileExtension());
        return loader.getResourceManager().getResource(file).flatMap(resource -> {
            return loadModel(modelId, file, resource, true, loader);
        });
    }

    @Override
    public Optional<FileContent<JsonElement>> loadModel(Identifier modelId, Identifier file, Resource resource, boolean failHard, ModelLoader loader) {
        try (var reader = new InputStreamReader(resource.getInputStream(), Charsets.UTF_8)) {
            return Optional.of(new JsonFileContent(loader, this, modelId, GSON.fromJson(reader, JsonObject.class)));
        } catch (Exception e) {
            MsonImpl.LOGGER.fatal("Exception whilst loading model file {}", modelId, e);
        }
        return Optional.empty();
    }

    @Override
    public void registerComponentType(Identifier id, ModelComponent.Factory<?, JsonElement> constructor) {
        Objects.requireNonNull(id, "Id must not be null");
        Objects.requireNonNull(constructor, "Constructor must not be null");
        MsonImpl.checkNamespace(id.getNamespace());
        Preconditions.checkArgument(!componentTypes.containsKey(id), "A component with the id `%s` was already registered", id);

        componentTypes.put(id, constructor);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<ModelComponent<T>> loadComponent(String name, JsonElement json, Identifier defaultAs, FileContent<JsonElement> context) {
        if (json.isJsonObject()) {
            JsonObject o = json.getAsJsonObject();
            final String fname = Strings.nullToEmpty(name).trim();

            if (!o.has("type") && o.has("data")) {
                return Optional.of(new JsonSlot<>(context, name, o));
            }

            return Optional.ofNullable(componentTypes.get(JsonUtil.accept(o, "type")
                        .map(JsonElement::getAsString)
                        .map(Identifier::new)
                        .orElse(defaultAs))
                    )
                    .map(c -> (ModelComponent<T>)c.load(context, fname, json));
        }
        if (json.isJsonPrimitive()) {
            JsonPrimitive prim = json.getAsJsonPrimitive();
            if (prim.isString()) {
                String s = prim.getAsString();

                if (s.startsWith("#")) {
                    return Optional.of((ModelComponent<T>)new JsonLink(s));
                }

                return Optional.of((ModelComponent<T>)new JsonImport(context, name, prim));
            }
        }

        throw new UnsupportedOperationException("Json was not a js object and could not be resolved to a #link or model reference");
    }
}
