package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.client.realms.util.JsonUtils;
import net.minecraft.util.Identifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.PartBuilder;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.impl.exception.FutureAwaitException;
import com.minelittlepony.mson.util.Incomplete;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class JsonCuboid implements JsonComponent<ModelPart> {
    public static final Identifier ID = new Identifier("mson", "compound");
    private static final float RADS_DEGS_FACTOR = (float)Math.PI / 180F;

    private final Incomplete<float[]> center;

    private final Incomplete<float[]> offset;

    private final Incomplete<float[]> rotation;

    private final boolean[] mirror = new boolean[3];
    private final boolean visible;

    private final Map<String, JsonComponent<?>> children = new TreeMap<>();
    private final List<JsonComponent<?>> cubes = new ArrayList<>();

    private final Incomplete<Texture> texture;

    private final String name;

    public JsonCuboid(JsonContext context, String name, JsonObject json) {
        center = context.getVarLookup().getFloats(json, "center", 3);
        offset = context.getVarLookup().getFloats(json, "offset", 3);
        rotation = context.getVarLookup().getFloats(json, "rotate", 3);
        JsonUtil.getBooleans(json, "mirror", mirror);

        visible = JsonUtils.getBooleanOr("visible", json, true);
        texture = JsonTexture.localized(JsonUtil.accept(json, "texture"));
        this.name = name.isEmpty() ? JsonUtil.accept(json, "name").map(JsonElement::getAsString).orElse("") : name;

        JsonUtil.accept(json, "children").map(this::loadChildrenMap).ifPresent(el -> {
            children.putAll(el.stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            i -> context.loadComponent(i.getValue(), ID).orElse(null))
                    ));
        });
        JsonUtil.accept(json, "cubes").map(JsonElement::getAsJsonArray).ifPresent(el -> {
            el.forEach(element -> {
                context.loadComponent(element, JsonBox.ID).ifPresent(cubes::add);
            });
        });

        context.addNamedComponent(this.name, this);
    }

    private Set<Map.Entry<String, JsonElement>> loadChildrenMap(JsonElement json) {
        if (json.isJsonObject()) {
            return json.getAsJsonObject().entrySet();
        }
        Map<String, JsonElement> map = new HashMap<>();
        JsonArray arr = json.getAsJsonArray();
        for (int i = 0; i < arr.size(); i++) {
            map.put("unnamed_member_" + i, arr.get(i));
        }
        return map.entrySet();
    }

    @Override
    public ModelPart export(ModelContext context) {
        return context.computeIfAbsent(name, key -> {
            return export(context, new PartBuilder()).build();
        });
    }

    protected PartBuilder export(ModelContext context, PartBuilder builder) throws InterruptedException, ExecutionException {
        float[] rotation = this.rotation.complete(context);
        builder
                .hidden(!visible)
                .pivot(this.center.complete(context))
                .offset(this.offset.complete(context))
                .mirror(mirror)
                .rotate(
                    rotation[0] * RADS_DEGS_FACTOR,
                    rotation[1] * RADS_DEGS_FACTOR,
                    rotation[2] * RADS_DEGS_FACTOR)
                .tex(texture.complete(context));

        final ModelContext subContext = context.resolve(builder, new Locals(context.getLocals()));
        children.entrySet().forEach(c -> {
            c.getValue().tryExport(subContext, ModelPart.class).ifPresent(part -> {
               builder.addChild(c.getKey(), part);
            });
        });
        cubes.forEach(c -> c.tryExport(subContext, Cuboid.class).ifPresent(builder::addCube));
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
        public CompletableFuture<Texture> getTexture() {
            try {
                return CompletableFuture.completedFuture(texture.complete(parent));
            } catch (InterruptedException | ExecutionException e) {
                throw new FutureAwaitException(e);
            }
        }

        @Override
        public CompletableFuture<Float> getValue(String name) {
            return parent.getValue(name);
        }

        @Override
        public CompletableFuture<Set<String>> getKeys() {
            return parent.getKeys();
        }
    }
}
