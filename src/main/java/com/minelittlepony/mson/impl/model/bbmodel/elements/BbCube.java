package com.minelittlepony.mson.impl.model.bbmodel.elements;

import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;

import org.joml.Quaternionf;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.export.ModelFileWriter;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.Face;
import com.minelittlepony.mson.api.model.QuadsBuilder;
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
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Parses a "cube" as defined by blockbench's bbmodel format;
 *
 * {
 *   "name": "body",
 *   "rescale": false,
 *   "locked": false,
 *   "from": [ -4, 4, -4 ],
 *   "to": [ 4, 11, 1 ],
 *   "autouv": 0,
 *   "color": 1,
 *   "origin": [ 0, -2, 0 ],
 *   "uv_offset": [ 0, 13 ],
 *   "faces": {
 *      "north": { "uv": [ 5, 18, 13, 25 ], "texture": 0 },
 *      "east": { "uv": [ 0, 18, 5, 25 ], "texture": 0 },
 *      "south": { "uv": [ 18, 18, 26, 25 ], "texture": 0 },
 *      "west": { "uv": [ 13, 18, 18, 25 ], "texture": 0 },
 *      "up": { "uv": [ 13, 18, 5, 13 ], "texture": 0 },
 *      "down": { "uv": [ 21, 13, 13, 18 ], "texture": 0 }
 *   },
 *   "uuid": "b38e15fc-748d-c7fa-fa9d-2ac7b265d63e"
 * }
 */
public class BbCube implements ModelComponent<Cuboid>, QuadsBuilder {
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

        float[] uvOffset = new float[2];
        JsonUtil.acceptFloats(json, "uv_offset", uvOffset);

        uuid = JsonUtil.accept(json, "uuid").map(JsonElement::getAsString).map(UUID::fromString);

        JsonObject faces = JsonHelper.getObject(json, "faces", new JsonObject());
        this.faces = Face.VALUES.stream()
                .filter(face -> face != Face.NONE)
                .collect(Collectors.toMap(Function.identity(), face -> {
                    return new CubeFace(face, JsonHelper.getObject(faces, face.name().toLowerCase(Locale.ROOT)));
                }, (a, b) -> b, () -> new EnumMap<>(Face.class)));

        texture = new Texture((int)uvOffset[0], (int)uvOffset[1], 0, 0);
    }

    @Override
    public Cuboid export(ModelContext context) {
        return createBuilder(context).build();
    }

    @Override
    public void write(ModelContext context, ModelFileWriter writer) {
        writer.writeBox(createBuilder(context));
    }

    private BoxBuilder createBuilder(ModelContext context) {

        if (boxUv) {
            return new BoxBuilder(context)
                    .tex(texture)
                    .pos(origin)
                    .size(to[0] - from[0], to[1] - from[1], to[2] - from[2])
                    .dilate(0, 0, 0);
        }

        return new BoxBuilder(context)
            .pos(origin)
            .size(to[0] - from[0], to[1] - from[1], to[2] - from[2])
            .dilate(0, 0, 0)
            .quads(this);
    }

    @Override
    public void build(BoxBuilder ctx, QuadBuffer buffer) {
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

        faces.get(Face.EAST).createRect(ctx, buffer, edn, eds, eus, eun);
        faces.get(Face.WEST).createRect(ctx, buffer, wds, wdn, wun, wus);
        faces.get(Face.UP).createRect(ctx, buffer, edn, wdn, wds, eds);
        faces.get(Face.DOWN).createRect(ctx, buffer, eus, wus, wun, eun);
        faces.get(Face.NORTH).createRect(ctx, buffer, eds, wds, wus, eus);
        faces.get(Face.SOUTH).createRect(ctx, buffer, wdn, edn, eun, wun);
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    record CubeFace(Face face, float[] uv, int texture, float rotation) {
        CubeFace(Face face, JsonObject json) {
            this(face, new float[4],
                    json.get("texture").getAsInt(),
                    json.get("rotation").getAsFloat() * MathHelper.RADIANS_PER_DEGREE
                );
            JsonUtil.acceptFloats(json, "uv", uv());
        }

        public void createRect(BoxBuilder builder, QuadsBuilder.QuadBuffer buffer, Vert a, Vert b, Vert c, Vert d) {
            Face.Axis axis = face.getAxis();

            buffer.quad(uv[0], uv[1], uv[2], uv[3], face.getLighting(), builder.mirror[0], true, new Quaternionf().rotateXYZ(
                axis == Face.Axis.X ? rotation : 0,
                axis == Face.Axis.Y ? rotation : 0,
                axis == Face.Axis.Z ? rotation : 0
            ), a, b, c, d);
        }
    }

}
