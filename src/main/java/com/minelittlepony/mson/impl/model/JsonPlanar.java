package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.Box;
import net.minecraft.client.model.Cuboid;
import net.minecraft.util.Identifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.Face;
import com.minelittlepony.mson.api.model.QuadsBuilder;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.EnumMap;
import java.util.Map;
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
    public void export(ModelContext context, Cuboid cuboid) throws InterruptedException, ExecutionException {
        super.export(context , cuboid);

        ModelContext subContext = context.resolve(cuboid);
        faces.values().forEach(face -> {
            cuboid.boxes.add(face.export(subContext));
        });
    }

    class JsonFace implements JsonComponent<Box> {

        private final Face face;

        private final float[] position = new float[3];
        private final int[] size = new int[2];

        public JsonFace(JsonContext context, JsonArray json, Face face) {
            this.face = face;

            position[0] =  json.get(0).getAsFloat();
            position[1] =  json.get(1).getAsFloat();
            position[2] =  json.get(2).getAsFloat();
            size[0] = (int)json.get(3).getAsFloat();
            size[1] = (int)json.get(4).getAsFloat();
        }

        @Override
        public Box export(ModelContext context) {
            return new BoxBuilder(context)
                .pos(face.transformPosition(position, context))
                .size(face.getAxis(), size)
                .build(QuadsBuilder.plane(face));
        }

    }
}
