package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.client.realms.util.JsonUtils;
import net.minecraft.util.Identifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.exception.FutureAwaitException;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.PartBuilder;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.impl.MsonImpl;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonCompound implements JsonComponent<ModelPart> {
    public static final Identifier ID = new Identifier("mson", "compound");
    private static final float RADS_DEGS_FACTOR = (float)Math.PI / 180F;

    @Deprecated
    private final Incomplete<float[]> offset;

    private final Incomplete<float[]> pivot;

    protected final Incomplete<float[]> dilate;

    private final Incomplete<float[]> rotate;

    private final boolean[] mirror = new boolean[3];
    private final boolean visible;

    private final Map<String, JsonComponent<?>> children = new TreeMap<>();
    private final List<JsonComponent<?>> cubes = new ArrayList<>();

    protected final Incomplete<Texture> texture;

    private final String name;

    public JsonCompound(JsonContext context, String name, JsonObject json) {
        if (json.has("offset")) {
            MsonImpl.LOGGER.warn("Model {} is using the `offset` property. This is deprecated and will be removed in 1.18.", context.getLocals().getModelId());
        }
        offset = context.getLocals().get(json, "offset", 3);

        if (json.has("center")) {
            MsonImpl.LOGGER.warn("Model {} is using the `center` property. This is deprecated and will be removed in 1.18. Please replace with `pivot`", context.getLocals().getModelId());
            pivot = context.getLocals().get(json, "center", 3);
        } else {
            pivot = context.getLocals().get(json, "pivot", 3);
        }

        if (json.has("stretch")) {
            MsonImpl.LOGGER.warn("Model {} is using the `stretch` property. This is deprecated and will be removed in 1.18. Please use `dilate`.", context.getLocals().getModelId());
            dilate = context.getLocals().get(json, "stretch", 3);
        } else {
            dilate = context.getLocals().get(json, "dilate", 3);
        }

        rotate = context.getLocals().get(json, "rotate", 3);
        JsonUtil.acceptBooleans(json, "mirror", mirror);
        visible = JsonUtils.getBooleanOr("visible", json, true);
        texture = JsonTexture.incomplete(JsonUtil.accept(json, "texture"));
        this.name = name.isEmpty() ? JsonUtil.accept(json, "name").map(JsonElement::getAsString).orElse("") : name;

        JsonUtil.accept(json, "children").ifPresent(el -> {
            children.putAll(parseChildren(context, el).collect(Collectors.toMap(
                Map.Entry::getKey,
                i -> context.loadComponent(i.getValue(), ID).orElse(null))
            ));
        });
        JsonUtil.accept(json, "cubes").ifPresent(el -> {
            el.getAsJsonArray().forEach(element -> {
                context.loadComponent(element, JsonBox.ID).ifPresent(cubes::add);
            });
        });

        context.addNamedComponent(this.name, this);
    }

    private Stream<Map.Entry<String, JsonElement>> parseChildren(JsonContext context, JsonElement json) {
        if (json.isJsonObject()) {
            return json.getAsJsonObject().entrySet().stream();
        }
        if (json.isJsonArray()) {
            MsonImpl.LOGGER.warn("Model {} is using a children array. Versions in 1.18 will require this to be an object.", context.getLocals().getModelId());
            JsonArray arr = json.getAsJsonArray();
            Map<String, JsonElement> children = new HashMap<>();
            for (int i = 0; i < arr.size(); i++) {
                children.put("unnamed_element_" + i, arr.get(i));
            }
            return children.entrySet().stream();
        }
        return Stream.empty();
    }

    @Override
    public ModelPart export(ModelContext context) {
        return context.computeIfAbsent(name, key -> {
            final PartBuilder builder = new PartBuilder();
            final ModelContext subContext = context.resolve(builder, new Locals(context.getLocals()));
            return export(subContext, builder).build();
        });
    }

    protected PartBuilder export(ModelContext context, PartBuilder builder) throws FutureAwaitException {
        float[] rotate = this.rotate.complete(context);
        builder
                .hidden(!visible)
                .pivot(this.pivot.complete(context))
                .offset(this.offset.complete(context))
                .mirror(mirror)
                .rotate(
                    rotate[0] * RADS_DEGS_FACTOR,
                    rotate[1] * RADS_DEGS_FACTOR,
                    rotate[2] * RADS_DEGS_FACTOR)
                .tex(texture.complete(context));

        children.entrySet().forEach(c -> {
            c.getValue().tryExport(context, ModelPart.class).ifPresent(part -> {
               builder.addChild(c.getKey(), part);
            });
        });
        cubes.forEach(c -> c.tryExport(context, Cuboid.class).ifPresent(builder::addCube));
        return builder;
    }

    class Locals implements ModelContext.Locals {
        private final ModelContext.Locals parent;

        Locals(ModelContext.Locals parent) {
            this.parent = parent;
        }

        @Override
        public Identifier getModelId() {
            return parent.getModelId();
        }

        @Override
        public CompletableFuture<float[]> getDilation() {
            return CompletableFuture.completedFuture(dilate.complete(parent));
        }

        @Override
        public CompletableFuture<Texture> getTexture() {
            return CompletableFuture.completedFuture(texture.complete(parent));
        }

        @Override
        public CompletableFuture<Float> getLocal(String name) {
            return parent.getLocal(name);
        }

        @Override
        public CompletableFuture<Set<String>> keys() {
            return parent.keys();
        }
    }
}
