package com.minelittlepony.mson.impl.model.bbmodel;

import net.minecraft.client.model.Model;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.api.parser.ModelComponent;
import com.minelittlepony.mson.api.parser.ModelFormat;
import com.minelittlepony.mson.impl.ModelContextImpl;
import com.minelittlepony.mson.impl.ModelLocalsImpl;
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

/**
 * Parses a blockbench .bbmodel file.
 *
 * {
 *  "meta": {                         // file metadata. Only used to check if the file is valid
 *    "format_version": "4.0",
 *    "creation_time": 1638295209,
 *    "model_format": "modded_entity",
 *    "box_uv": true
 *  },
 *  "name": "Copper Golem",           // ignored
 *  "geometry_name": "",              // ignored
 *  "visible_box": [ 1, 1, 0 ],       // ignored
 *  "variable_placeholders": "",      // ignored
 *  "resolution": {                   // texture
 *    "width": 64, "height": 64
 *  },
 *  "elements": [ ... ],              // list of model cubes (BbCube)
 *  "outliner": [ ... ],              // a tree of model parts (BbPart)
 *  "textures": [                     // ignored
 *    {
 *     "path": "...",
 *     "name": "xxx.png",
 *     "folder": "block",
 *     "namespace": "",
 *     "id": "0",
 *     "particle": false,
 *     "render_mode": "normal",
 *     "visible": true,
 *     "mode": "bitmap",
 *     "saved": false,
 *     "uuid": "d1ee668a-2985-cc55-7dd6-be6afb1ef330",
 *      "source": "...",
 *      "relative_path": "..."
 *    }
 *  ]
 * }
 */
class BlockBenchFileContent implements JsonContext {

    private final ModelFormat<JsonElement> format;

    public final Map<UUID, ModelComponent<?>> cubes = new HashMap<>();
    private final Map<String, ModelComponent<?>> elements = new HashMap<>();

    private final FileContent.Locals locals;

    public BlockBenchFileContent(String formatVariant, ModelFormat<JsonElement> format, Identifier id, JsonObject json) {
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
            .forEach(element -> {
                loadComponent(element, BbPart.ID).ifPresent(component -> {
                    // Since bbmodel uses lists for everything, we can't use the name property+selfpublish model that mson does.
                    // Instead the parent has to collect models.
                    if (((ModelComponent<?>)component) instanceof BbPart part) {
                        elements.put(part.name, part);
                    }
                });
            });

        // The java_block model type might (very likely will) not have an outliner
        if ("java_block".equalsIgnoreCase(formatVariant) && elements.isEmpty()) {
            elements.put("root", new BbPart(cubes.values(), "root"));
        }
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
        return CompletableFuture.completedFuture(FileContent.empty());
    }

    @Override
    public ModelContext createContext(Model model, Object thisObj, ModelContext.Locals locals) {
        return new RootContext(model, thisObj, (ModelContextImpl)FileContent.empty().createContext(model, thisObj, locals), elements, locals);
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
        public CompletableFuture<Incomplete<Float>> getLocal(String name, float defaultValue) {
            return CompletableFuture.completedFuture(Incomplete.completed(defaultValue));
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
