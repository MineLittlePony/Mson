package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.QuadsBuilder;
import com.minelittlepony.mson.api.model.Rect;
import com.minelittlepony.mson.api.model.Vert;
import com.minelittlepony.mson.util.JsonUtil;
import com.mojang.realmsclient.util.JsonUtils;

import java.util.List;
import java.util.stream.Collectors;

public class JsonQuads implements JsonComponent<Cuboid>, QuadsBuilder {

    public static final Identifier ID = new Identifier("mson", "quads");

    private final List<JsonQuad> quads;

    private final int texU;
    private final int texV;

    public JsonQuads(JsonContext context, String name, JsonObject json) {
        texU = JsonUtil.require(json, "u").getAsInt();
        texV = JsonUtil.require(json, "v").getAsInt();

        List<JsonVertex> vertices = Streams.stream(JsonUtil.require(json, "vertices").getAsJsonArray())
                .map(JsonVertex::new)
                .collect(Collectors.toList());

        quads = Streams.stream(JsonUtil.require(json, "faces").getAsJsonArray())
            .map(v -> new JsonQuad(vertices, v))
            .collect(Collectors.toList());
    }

    @Override
    public Cuboid export(ModelContext context) {
        BoxBuilder builder = new BoxBuilder(context);
        builder.u = texU;
        builder.v = texV;
        return builder.build(this);
    }

    @Override
    public Rect[] build(BoxBuilder box) {
        return quads.stream().map(q -> q.build(box)).toArray(i -> new Rect[i]);
    }

    class JsonQuad {

        private final int x;
        private final int y;

        private final int w;
        private final int h;

        private final List<JsonVertex> verts;

        JsonQuad(List<JsonVertex> vertices, JsonElement json) {
            JsonObject o = json.getAsJsonObject();
            x = JsonUtils.getIntOr("x", o, 0);
            y = JsonUtils.getIntOr("y", o, 0);
            w = JsonUtils.getIntOr("w", o, 0);
            h = JsonUtils.getIntOr("h", o, 0);
            verts = Streams.stream(JsonUtil.require(o, "vertices").getAsJsonArray())
                .map(JsonElement::getAsInt)
                .map(vertices::get)
                .collect(Collectors.toList());
        }

        Rect build(BoxBuilder builder) {
            return builder.quad(x, y, w, h, Direction.UP, verts.stream()
                    .map(v -> v.build(builder))
                    .toArray(Vert[]::new)
            );
        }
    }

    class JsonVertex {

        private final float x;
        private final float y;
        private final float z;

        private final int u;
        private final int v;

        JsonVertex(JsonElement json) {
            if (json.isJsonArray()) {
                JsonArray arr = json.getAsJsonArray();
                x = arr.get(0).getAsFloat();
                y = arr.get(1).getAsFloat();
                z = arr.get(2).getAsFloat();
                u = arr.get(3).getAsInt();
                v = arr.get(4).getAsInt();
            } else {
                JsonObject o = json.getAsJsonObject();
                x = JsonUtil.getFloatOr("x", o, 0);
                y = JsonUtil.getFloatOr("y", o, 0);
                z = JsonUtil.getFloatOr("z", o, 0);
                u = JsonUtils.getIntOr("u", o, 0);
                v = JsonUtils.getIntOr("v", o, 0);
            }
        }

        Vert build(BoxBuilder builder) {
            return builder.vert(x, y, z, u, v);
        }
    }
}
