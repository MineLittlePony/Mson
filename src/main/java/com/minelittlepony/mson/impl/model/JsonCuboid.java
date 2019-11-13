package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.util.Identifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.BoxBuilder.ContentAccessor;
import com.minelittlepony.mson.api.model.MsonPart;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.util.Incomplete;
import com.minelittlepony.mson.util.JsonUtil;
import com.mojang.realmsclient.util.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class JsonCuboid implements JsonComponent<ModelPart> {
    public static final Identifier ID = new Identifier("mson", "compound");
    private static final float RADS_DEGS_FACTOR = (float)Math.PI / 180F;

    private final Incomplete<float[]> center;

    private final Incomplete<float[]> rotation;

    private final boolean[] mirror = new boolean[3];

    private final boolean visible;

    private final List<JsonComponent<?>> children = new ArrayList<>();
    private final List<JsonComponent<?>> cubes = new ArrayList<>();

    private final Incomplete<Texture> texture;

    private final String name;

    public JsonCuboid(JsonContext context, JsonObject json) {
        center = context.getVarLookup().getFloats(json, "center", 3);
        rotation = context.getVarLookup().getFloats(json, "rotate", 3);
        JsonUtil.getBooleans(json, "mirror", mirror);

        visible = JsonUtils.getBooleanOr("visible", json, true);
        texture = JsonTexture.localized(JsonUtil.accept(json, "texture"));
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
    public ModelPart export(ModelContext context) {
        return context.computeIfAbsent(name, key -> {
            ModelPart cuboid = new MsonCuboidImpl(context.getModel());

            export(context, cuboid);

            return cuboid;
        });
    }

    @Override
    public void export(ModelContext context, ModelPart cuboid) throws InterruptedException, ExecutionException {

        float[] center = this.center.complete(context);
        float[] rotation = this.rotation.complete(context);

        cuboid.visible = visible;

        ((MsonPart)cuboid).around(center[0], center[1], center[2]);
        ((MsonPart)cuboid).rotate(
                rotation[0] * RADS_DEGS_FACTOR,
                rotation[1] * RADS_DEGS_FACTOR,
                rotation[2] * RADS_DEGS_FACTOR
        );
        ((MsonPart)cuboid).mirror(mirror[0], mirror[1], mirror[2]);
        ((MsonPart)cuboid).tex(texture.complete(context));
        ((ContentAccessor)cuboid).cubes().clear();
        ((ContentAccessor)cuboid).children().clear();

        final ModelContext subContext = context.resolve(cuboid);
        ((ContentAccessor)cuboid).children().addAll(children
                .stream()
                .map(c -> c.tryExport(subContext, ModelPart.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())
        );
        ((ContentAccessor)cuboid).cubes().addAll(cubes
                .stream()
                .map(c -> c.tryExport(subContext, Cuboid.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())
        );
    }
}
