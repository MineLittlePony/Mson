package com.minelittlepony.mson.impl.model.bbmodel;

import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.export.ModelSerializer;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.api.parser.ModelComponent;
import com.minelittlepony.mson.api.parser.ModelFormat;
import com.minelittlepony.mson.api.parser.ModelLoader;
import com.minelittlepony.mson.impl.MsonImpl;
import com.minelittlepony.mson.impl.model.bbmodel.elements.BbCube;
import com.minelittlepony.mson.impl.model.bbmodel.elements.BbPart;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of ModelFormat that reads a bbmodel file.
 */
public class BBModelFormat implements ModelFormat<JsonElement> {
    public static final ModelFormat<JsonElement> INSTANCE = new BBModelFormat();
    private static final Gson GSON = new Gson();

    private final Map<Identifier, ModelComponent.Factory<?, JsonElement>> componentTypes = new HashMap<>();

    private final Set<String> acceptableModelFormats = Set.of("modded_entity", "java_block");

    private BBModelFormat() {
        registerComponentType(BbCube.ID, BbCube::new);
        registerComponentType(BbPart.ID, BbPart::new);
    }

    @Override
    public String getFileExtension() {
        return "bbmodel";
    }

    @Override
    public Optional<ModelSerializer<FileContent<?>>> createSerializer() {
        return Optional.of(new BBModelWriter());
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
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            JsonObject meta = JsonHelper.getObject(json, "meta", new JsonObject());
            String modelFormat = JsonHelper.getString(meta, "model_format", "").toLowerCase(Locale.ROOT);

            if (!acceptableModelFormats.contains(modelFormat)) {
                return Optional.empty();
            }

            return Optional.of(new BlockBenchFileContent(modelFormat, this, modelId, json));
        } catch (Exception e) {}
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

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<ModelComponent<T>> loadComponent(String name, JsonElement data, Identifier defaultAs, FileContent<JsonElement> context) {

        if (!data.isJsonObject()) {
            return Optional.empty();
        }

        JsonObject json = data.getAsJsonObject();
        Identifier id = new Identifier(json.get("type").getAsString());
        final String fname = Strings.nullToEmpty(name).trim();

        if (id.getNamespace().equalsIgnoreCase("minecraft")) {
            id = new Identifier("blockbench", id.getPath());
        }

        return Optional.ofNullable(componentTypes.get(id)).map(c -> (ModelComponent<T>)c.load(context, fname, json));
    }
}
