package com.minelittlepony.mson.impl.model.bbmodel.elements;

import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.Face;
import com.minelittlepony.mson.api.model.Rect;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.api.model.Vert;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.api.parser.ModelComponent;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BbCube implements ModelComponent<Cuboid> {
    public static final Identifier ID = new Identifier("blockbench", "cube");

    private final boolean boxUv;

    private final float[] from = new float[3];
    private final float[] to = new float[3];
    private final float[] origin = new float[3];

    private final Map<Face, CubeFace> faces;

    private final Texture texture;

    public final Optional<UUID> uuid;

    public BbCube(FileContent<JsonElement> context, String name, JsonElement json) {
        this(context, name, json.getAsJsonObject());
    }

    public BbCube(FileContent<JsonElement> context, String name, JsonObject json) {
        boxUv = JsonHelper.getBoolean(json, "box_uv", true);
        JsonUtil.acceptFloats(json, "from", from);
        JsonUtil.acceptFloats(json, "to", to);
        JsonUtil.acceptFloats(json, "origin", origin);

        uuid = JsonUtil.accept(json, "uuid").map(JsonElement::getAsString).map(UUID::fromString);

        JsonObject faces = JsonHelper.getObject(json, "faces", new JsonObject());
        float[] minUv = { 1, 1 };
        this.faces = Face.VALUES.stream()
                .filter(face -> face != Face.NONE)
                .collect(Collectors.toMap(Function.identity(), face -> {
                    CubeFace f = new CubeFace(face, JsonHelper.getObject(faces, face.name().toLowerCase(Locale.ROOT)));
                    minUv[0] = Math.min(minUv[1], f.uv()[0]);
                    minUv[1] = Math.min(minUv[1], f.uv()[1]);
                    return f;
                }, (a, b) -> b, () -> new EnumMap<>(Face.class)));

        texture = new Texture((int)minUv[0], (int)minUv[1], 0, 0);
    }

    @Override
    public Cuboid export(ModelContext context) throws InterruptedException, ExecutionException {

        if (boxUv) {
            return new BoxBuilder(context)
                    .tex(texture)
                    .pos(origin)
                    .size(to[0] - from[0], to[1] - from[1], to[2] - from[2])
                    .dilate(0, 0, 0)
                    .build();
        }

        return new BoxBuilder(context)
            .pos(origin)
            .size(to[0] - from[0], to[1] - from[1], to[2] - from[2])
            .dilate(0, 0, 0)
            .build(ctx -> {
                float xMax = ctx.pos[0] + ctx.size[0] + ctx.dilate[0];
                float yMax = ctx.pos[1] + ctx.size[1] + ctx.dilate[1];
                float zMax = ctx.pos[2] + ctx.size[2] + ctx.dilate[2];

                float xMin = ctx.pos[0] - ctx.dilate[0];
                float yMin = ctx.pos[1] - ctx.dilate[1];
                float zMin = ctx.pos[2] - ctx.dilate[2];

                if (ctx.mirror[0]) {
                    float v = xMax;
                    xMax = xMin;
                    xMin = v;
                }

                // w:west e:east d:down u:up s:south n:north
                Vert wds = ctx.vert(xMin, yMin, zMax, 0, 0);
                Vert eds = ctx.vert(xMax, yMin, zMin, 0, 8);
                Vert eus = ctx.vert(xMax, yMax, zMin, 8, 8);
                Vert wus = ctx.vert(xMin, yMax, zMin, 8, 0);
                Vert wdn = ctx.vert(xMin, yMin, zMax, 0, 0);
                Vert edn = ctx.vert(xMax, yMin, zMax, 0, 8);
                Vert eun = ctx.vert(xMax, yMax, zMax, 8, 8);
                Vert wun = ctx.vert(xMin, yMax, zMax, 8, 0);

                return new Rect[] {
                    faces.get(Face.EAST).createRect(ctx, edn, eds, eus, eun),
                    faces.get(Face.WEST).createRect(ctx, wds, wdn, wun, wus),
                    faces.get(Face.UP).createRect(ctx, edn, wdn, wds, eds),
                    faces.get(Face.DOWN).createRect(ctx, eus, wus, wun, eun),
                    faces.get(Face.NORTH).createRect(ctx, eds, wds, wus, eus),
                    faces.get(Face.SOUTH).createRect(ctx, wdn, edn, eun, wun)
                };
            });
    }

    record CubeFace(Face face, float[] uv, int texture, float rotation) {
        CubeFace(Face face, JsonObject json) {
            this(face, new float[4],
                    json.get("texture").getAsInt(),
                    json.get("rotation").getAsFloat() * MathHelper.RADIANS_PER_DEGREE
                );
            JsonUtil.acceptFloats(json, "uv", uv());
        }

        public Rect createRect(BoxBuilder ctx, Vert a, Vert b, Vert c, Vert d) {
            Face.Axis axis = face.getAxis();
            return ctx.quad(uv[0], uv[1], uv[2], uv[3], face.getLighting(), a, b, c, d).rotate(
                    axis == Face.Axis.X ? rotation : 0,
                    axis == Face.Axis.Y ? rotation : 0,
                    axis == Face.Axis.Z ? rotation : 0
            );
        }
    }
}
