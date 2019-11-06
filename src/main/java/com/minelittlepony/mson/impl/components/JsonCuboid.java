package com.minelittlepony.mson.impl.components;

import net.minecraft.client.model.Box;
import net.minecraft.client.model.Cuboid;
import net.minecraft.util.Identifier;

import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.model.MsonCuboid;
import com.minelittlepony.mson.util.JsonUtil;
import com.mojang.realmsclient.util.JsonUtils;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonCuboid implements JsonComponent<MsonCuboid> {
    public static final Identifier ID = new Identifier("mson", "compound");

    private final float[] center = new float[3];

    private final float[] rotation = new float[3];

    private final float[] position = new float[3];

    private final boolean[] mirror = new boolean[3];

    @Nullable
    private float[] textureSize;

    @Nullable
    private int[] textureUv;

    private final boolean visible;
    private final boolean hidden;

    private final List<JsonComponent<?>> children = new ArrayList<>();
    private final List<JsonComponent<?>> cubes = new ArrayList<>();

    @Nullable
    private String name;

    public JsonCuboid(JsonContext context, JsonObject json) {
        JsonUtil.getFloats(json, "center", center);
        JsonUtil.getFloats(json, "rotation", rotation);
        JsonUtil.getFloats(json, "position", position);
        JsonUtil.getBooleans(json, "mirror", mirror);

        visible = JsonUtils.getBooleanOr("visible", json, true);
        hidden = JsonUtils.getBooleanOr("hidden", json, false);

        if (json.has("texture")) {
            JsonObject tex = json.get("texture").getAsJsonObject();
            textureUv = new int[] {
                    JsonUtils.getIntOr("u", tex, 0),
                    JsonUtils.getIntOr("v", tex, 0)
            };
            textureSize = new float[] {
                    JsonUtil.getFloatOr("w", tex, 64),
                    JsonUtil.getFloatOr("h", tex, 32)
            };
        }

        if (json.has("children")) {
            json.get("children").getAsJsonArray().forEach(element -> {
                JsonComponent<?> component = context.loadComponent(element);
                if (component != null) {
                    children.add(component);
                }
            });
        }
        if (json.has("cubes")) {
            json.get("cubes").getAsJsonArray().forEach(element -> {
                JsonComponent<?> component = context.loadComponent(element);
                if (component != null) {
                    cubes.add(component);
                }
            });
        }

        if (json.has("name")) {
            name = json.get("name").getAsString();
            context.addNamedComponent(name, this);
        }
    }

    @Override
    public MsonCuboid export(ModelContext context) {
        return context.computeIfAbsent(name, key -> {
            MsonCuboid cuboid = new MsonCuboid(context.getModel(), key);

            export(context, cuboid);

            return cuboid;
        });
    }

    @Override
    public void export(ModelContext context, Cuboid cuboid) {

        MsonCuboid.at(cuboid, position[0], position[1], position[2]);

        cuboid.setRotationPoint(center[0], center[1], center[2]);

        cuboid.pitch = rotation[0];
        cuboid.yaw = rotation[1];
        cuboid.roll = rotation[2];

        cuboid.visible = visible;
        cuboid.field_3664 = hidden;

        cuboid.mirror = mirror[0];

        if (cuboid instanceof MsonCuboid) {
            MsonCuboid part = (MsonCuboid)cuboid;
            part.mirrorY = mirror[1];
            part.mirrorZ = mirror[2];
        }

        if (textureUv != null) {
            cuboid.setTextureOffset(textureUv[0], textureUv[1]);
        }
        if (textureSize != null) {
            cuboid.textureWidth = textureSize[0];
            cuboid.textureHeight = textureSize[1];
        }

        cuboid.children.clear();
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
