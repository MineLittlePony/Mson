package com.minelittlepony.mson.impl.model.json.elements;

import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.client.realms.util.JsonUtils;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.export.ModelFileWriter;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.BoxParameters;
import com.minelittlepony.mson.api.model.QuadsBuilder;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.api.model.Vert;
import com.minelittlepony.mson.api.parser.ModelComponent;
import com.minelittlepony.mson.impl.MsonImpl;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.List;

/**
 * Represents a custom structure where the quads and vertices are manually defined.
 *
 * @author Sollace
 * @apiNote Experimental. This feature may disappear in the future.
 */
public record JsonQuads (List<JsonQuad> quads, int texU, int texV) implements ModelComponent<Cuboid>, QuadsBuilder {
    public static final Identifier ID = MsonImpl.id("quads");

    public JsonQuads(FileContent<JsonElement> context, String name, JsonElement json) {
        this(context, name, json.getAsJsonObject());
    }

    public JsonQuads(FileContent<JsonElement> context, String name, JsonObject json) {
        this(context, name, json,
            Streams.stream(JsonUtil.require(json, "vertices", ID, context.locals().modelId()).getAsJsonArray()).map(JsonVertex::fromJson).toList(),
            JsonUtil.require(json, "u", ID, context.locals().modelId()).getAsInt(),
            JsonUtil.require(json, "v", ID, context.locals().modelId()).getAsInt()
        );
    }

    public JsonQuads(FileContent<JsonElement> context, String name, JsonObject json, List<JsonVertex> vertices, int texU, int texV) {
        this(
            Streams.stream(JsonUtil.require(json, "faces", ID, context.locals().modelId()).getAsJsonArray()).map(v -> JsonQuad.fromJson(context, vertices, v)).toList(),
            texU,
            texV
        );
    }

    public BoxBuilder builder(ModelContext context) {
        return new BoxBuilder(context)
                .tex(new Texture(texU, texV, 0, 0))
                .quads(this);
    }

    @Override
    public Cuboid export(ModelContext context) {
        return builder(context).build();
    }

    @Override
    public void write(ModelContext context, ModelFileWriter writer) {
        writer.writeBox(builder(context));
    }

    @Override
    public void build(BoxParameters pars, BoxBuilder box, QuadBuffer buffer) {
        quads.forEach(q -> q.build(box, buffer));
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    record JsonQuad (int x, int y, int w, int h, List<JsonVertex> verts) {
        static JsonQuad fromJson(FileContent<JsonElement> context, List<JsonVertex> vertices, JsonElement json) {
            JsonObject o = json.getAsJsonObject();
            return new JsonQuad(
                JsonUtils.getIntOr("x", o, 0),
                JsonUtils.getIntOr("y", o, 0),
                JsonUtils.getIntOr("w", o, 0),
                JsonUtils.getIntOr("h", o, 0),
                Streams.stream(JsonUtil.require(o, "vertices", ID, context.locals().modelId()).getAsJsonArray())
                    .map(JsonElement::getAsInt)
                    .map(vertices::get)
                    .toList()
            );
        }

        void build(BoxBuilder builder, QuadBuffer buffer) {
            buffer.quad(Direction.UP, x, y, w, h, builder.parameters.mirror[0], verts.stream().map(v -> v.build(builder)).toArray(Vert[]::new));
        }
    }

    record JsonVertex (float x, float y, float z, int u, int v) {
        static JsonVertex fromJson(JsonElement json) {
            if (json.isJsonArray()) {
                JsonArray arr = json.getAsJsonArray();
                return new JsonVertex(
                    arr.get(0).getAsFloat(), arr.get(1).getAsFloat(), arr.get(2).getAsFloat(),
                    arr.get(3).getAsInt(), arr.get(4).getAsInt()
                );
            }

            JsonObject o = json.getAsJsonObject();
            return new JsonVertex(
                JsonUtil.getFloatOr("x", o, 0),
                JsonUtil.getFloatOr("y", o, 0),
                JsonUtil.getFloatOr("z", o, 0),
                JsonUtils.getIntOr("u", o, 0),
                JsonUtils.getIntOr("v", o, 0)
            );
        }

        Vert build(BoxBuilder builder) {
            return builder.vert(x, y, z, u, v);
        }
    }
}
