package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.util.Identifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.BoxBuilder.ContentAccessor;
import com.minelittlepony.mson.api.model.Face;
import com.minelittlepony.mson.api.model.QuadsBuilder;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.impl.exception.FutureAwaitException;
import com.minelittlepony.mson.util.Incomplete;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class JsonPlanar extends JsonCuboid {
    public static final Identifier ID = new Identifier("mson", "planar");

    private final Map<Face, JsonFace> faces = new EnumMap<>(Face.class);

    public JsonPlanar(JsonContext context, JsonObject json) {
        super(context, json);

        Face.VALUES.forEach(face -> {
            JsonUtil.accept(json, face.name().toLowerCase())
                .map(JsonElement::getAsJsonArray)
                .ifPresent(el -> faces.put(face, new JsonFace(context, el, face)));
        });
    }

    @Override
    public void export(ModelContext context, ModelPart cuboid) throws InterruptedException, ExecutionException {
        super.export(context , cuboid);

        ModelContext subContext = context.resolve(cuboid);
        faces.values().forEach(face -> {
            try {
                ((ContentAccessor)cuboid).cubes().add(face.export(subContext));
            } catch (InterruptedException | ExecutionException e) {
                throw new FutureAwaitException(e);
            }
        });
    }

    class JsonFace implements JsonComponent<Cuboid> {

        private final Face face;

        private final float[] position = new float[3];
        private final int[] size = new int[2];

        private final Incomplete<Optional<Texture>> texture;

        public JsonFace(JsonContext context, JsonArray json, Face face) {
            this.face = face;

            position[0] =  json.get(0).getAsFloat();
            position[1] =  json.get(1).getAsFloat();
            position[2] =  json.get(2).getAsFloat();
            size[0] = (int)json.get(3).getAsFloat();
            size[1] = (int)json.get(4).getAsFloat();

            if (json.size() > 6) {
                texture = createTexture(json.get(5).getAsInt(), json.get(6).getAsInt());
            } else {
                texture = Incomplete.completed(Optional.empty());
            }
        }

        private Incomplete<Optional<Texture>> createTexture(int u, int v) {
            return locals -> {
                Texture parent = locals.getTexture().get();
                return Optional.of(new JsonTexture(u, v, parent.getWidth(), parent.getHeight()));
            };
        }

        @Override
        public Cuboid export(ModelContext context) throws InterruptedException, ExecutionException {
            return new BoxBuilder(context)
                .tex(texture.complete(context))
                .pos(face.transformPosition(position, context))
                .size(face.getAxis(), size)
                .build(QuadsBuilder.plane(face));
        }

    }
}
