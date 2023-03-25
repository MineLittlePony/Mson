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
import com.minelittlepony.mson.api.model.QuadsBuilder;
import com.minelittlepony.mson.api.model.Vert;
import com.minelittlepony.mson.api.parser.ModelComponent;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a custom structure where the quads and vertices are manually defined.
 *
 * @author Sollace
 * @apiNote Experimental. This feature may disappear in the future.
 */
public class JsonQuads implements ModelComponent<Cuboid>, QuadsBuilder {

    public static final Identifier ID = new Identifier("mson", "quads");

    private final List<JsonQuad> quads;

    private final int texU;
    private final int texV;

    public JsonQuads(FileContent<JsonElement> context, String name, JsonElement json) {
        this(context, name, json.getAsJsonObject());
    }

    public JsonQuads(FileContent<JsonElement> context, String name, JsonObject json) {
        texU = JsonUtil.require(json, "u", ID, context.getLocals().getModelId()).getAsInt();
        texV = JsonUtil.require(json, "v", ID, context.getLocals().getModelId()).getAsInt();

        List<JsonVertex> vertices = Streams.stream(JsonUtil.require(json, "vertices", ID, context.getLocals().getModelId())
                .getAsJsonArray())
                .map(JsonVertex::new)
                .collect(Collectors.toList());

        quads = Streams.stream(JsonUtil.require(json, "faces", ID, context.getLocals().getModelId()).getAsJsonArray())
            .map(v -> new JsonQuad(context, vertices, v))
            .collect(Collectors.toList());
    }

    @Override
    public Cuboid export(ModelContext context) {
        BoxBuilder builder = new BoxBuilder(context);
        builder.u = texU;
        builder.v = texV;
        return builder.quads(this).build();
    }

    @Override
    public void write(ModelContext context, ModelFileWriter writer) {
        BoxBuilder builder = new BoxBuilder(context);
        builder.u = texU;
        builder.v = texV;
        writer.writeBox(builder.quads(this));
    }

    @Override
    public void build(BoxBuilder box, QuadBuffer buffer) {
        quads.forEach(q -> q.build(box, buffer));
    }

    class JsonQuad {
        private final int x;
        private final int y;

        private final int w;
        private final int h;

        private final List<JsonVertex> verts;

        JsonQuad(FileContent<JsonElement> context, List<JsonVertex> vertices, JsonElement json) {
            JsonObject o = json.getAsJsonObject();
            x = JsonUtils.getIntOr("x", o, 0);
            y = JsonUtils.getIntOr("y", o, 0);
            w = JsonUtils.getIntOr("w", o, 0);
            h = JsonUtils.getIntOr("h", o, 0);
            verts = Streams.stream(JsonUtil.require(o, "vertices", ID, context.getLocals().getModelId()).getAsJsonArray())
                .map(JsonElement::getAsInt)
                .map(vertices::get)
                .collect(Collectors.toList());
        }

        void build(BoxBuilder builder, QuadBuffer buffer) {
            buffer.quad(x, y, w, h, Direction.UP, builder.mirror[0], verts.stream().map(v -> v.build(buffer)).toArray(Vert[]::new));
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

        Vert build(QuadBuffer buffer) {
            return buffer.vert(x, y, z, u, v);
        }
    }
}
