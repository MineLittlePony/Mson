package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.Cuboid;
import net.minecraft.util.Identifier;

import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.MsonBox;
import com.minelittlepony.mson.api.model.MsonCuboid;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.util.JsonUtil;
import com.mojang.realmsclient.util.JsonUtils;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class JsonCuboid implements JsonComponent<MsonCuboidImpl> {
    public static final Identifier ID = new Identifier("mson", "compound");

    private final float[] center = new float[3];

    private final float[] rotation = new float[3];

    private final float[] position = new float[3];

    private final boolean[] mirror = new boolean[3];

    private final CompletableFuture<Texture> texture;

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

        texture = context.getTexture().thenApply(t -> new JsonTexture(json, t));

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
    public MsonCuboidImpl export(ModelContext context) {
        return context.computeIfAbsent(name, key -> {
            MsonCuboidImpl cuboid = new MsonCuboidImpl(context.getModel(), key);

            export(context, cuboid);

            return cuboid;
        });
    }

    @Override
    public void export(ModelContext context, Cuboid cuboid) throws InterruptedException, ExecutionException {

        ((MsonCuboid)cuboid).at(position[0], position[1], position[2]);
        ((MsonCuboid)cuboid).around(center[0], center[1], center[2]);
        ((MsonCuboid)cuboid).rotate(rotation[0], rotation[1], rotation[2]);
        ((MsonCuboid)cuboid).mirror(mirror[0], mirror[1], mirror[2]);

        cuboid.visible = visible;
        cuboid.field_3664 = hidden;

        Texture tex = texture.get();

        ((MsonCuboid)cuboid).tex(tex.getU(), tex.getV());
        ((MsonCuboid)cuboid).size(tex.getWidth(), tex.getHeight());

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
                .map(c -> c.tryExport(subContext, MsonBox.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())
        );
    }
}
