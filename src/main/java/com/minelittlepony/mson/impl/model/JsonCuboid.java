package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.Box;
import net.minecraft.client.model.Cuboid;
import net.minecraft.util.Identifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.MsonCuboid;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.util.Incomplete;
import com.minelittlepony.mson.util.JsonUtil;
import com.mojang.realmsclient.util.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class JsonCuboid implements JsonComponent<Cuboid> {
    public static final Identifier ID = new Identifier("mson", "compound");
    private static final float RADS_DEGS_FACTOR = (float)Math.PI / 180F;

    private final float[] center = new float[3];

    private final float[] rotation = new float[3];

    private final float[] position = new float[3];

    private final boolean[] mirror = new boolean[3];

    private final boolean visible;
    private final boolean hidden;

    private final List<JsonComponent<?>> children = new ArrayList<>();
    private final List<JsonComponent<?>> cubes = new ArrayList<>();

    private final Incomplete<Texture> texture;

    private final String name;

    public JsonCuboid(JsonContext context, JsonObject json) {
        JsonUtil.getFloats(json, "center", center);
        JsonUtil.getFloats(json, "rotation", rotation);
        JsonUtil.getFloats(json, "position", position);
        JsonUtil.getBooleans(json, "mirror", mirror);

        visible = JsonUtils.getBooleanOr("visible", json, true);
        hidden = JsonUtils.getBooleanOr("hidden", json, false);
        texture = JsonTexture.localized(json);
        name = JsonUtil.accept(json, "name").map(JsonElement::getAsString).orElse("");

        JsonUtil.accept(json, "children").map(JsonElement::getAsJsonArray).ifPresent(el -> {
            el.forEach(element -> {
                context.loadComponent(element, ID).ifPresent(children::add);
            });
        });
        JsonUtil.accept(json, "cubes").map(JsonElement::getAsJsonArray).ifPresent(el -> {
            el.forEach(element -> {
                context.loadComponent(element, JsonBox.ID).ifPresent(cubes::add);
            });
        });

        context.addNamedComponent(name, this);
    }

    @Override
    public Cuboid export(ModelContext context) {
        return context.computeIfAbsent(name, key -> {
            Cuboid cuboid = new MsonCuboidImpl(context.getModel(), key);

            export(context, cuboid);

            return cuboid;
        });
    }

    @Override
    public void export(ModelContext context, Cuboid cuboid) throws InterruptedException, ExecutionException {

        ((MsonCuboid)cuboid).at(position[0], position[1], position[2]);
        ((MsonCuboid)cuboid).around(center[0], center[1], center[2]);
        ((MsonCuboid)cuboid).rotate(rotation[0] * RADS_DEGS_FACTOR, rotation[1] * RADS_DEGS_FACTOR, rotation[2] * RADS_DEGS_FACTOR);
        ((MsonCuboid)cuboid).mirror(mirror[0], mirror[1], mirror[2]);
        ((MsonCuboid)cuboid).tex(texture.complete(context));

        cuboid.visible = visible;
        cuboid.field_3664 = hidden;
        cuboid.children = new ArrayList<>();
        cuboid.boxes.clear();

        final ModelContext subContext = context.resolve(cuboid);
        cuboid.children.addAll(children
                .stream()
                .map(c -> c.tryExport(subContext, Cuboid.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())
        );
        cuboid.boxes.addAll(cubes
                .stream()
                .map(c -> c.tryExport(subContext, Box.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())
        );
    }
}
