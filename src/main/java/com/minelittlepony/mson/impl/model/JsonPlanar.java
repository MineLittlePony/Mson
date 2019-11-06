package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.Cuboid;
import net.minecraft.util.Identifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.Face;
import com.minelittlepony.mson.api.model.MsonCuboid;
import com.minelittlepony.mson.api.model.MsonPlane;

import java.util.EnumMap;
import java.util.Map;

public class JsonPlanar extends JsonCuboid {
    public static final Identifier ID = new Identifier("mson", "planar");

    private final Map<Face, JsonFace> faces = new EnumMap<>(Face.class);

    public JsonPlanar(JsonContext context, JsonObject json) {
        super(context, json);

        Face.VALUES.forEach(face -> {
            String key = face.name().toLowerCase();
            if (json.has(key)) {
                faces.put(face, new JsonFace(context, json.get(key).getAsJsonArray(), face));
            }
        });
    }

    @Override
    public void export(ModelContext context, Cuboid cuboid) {
        super.export(context , cuboid);

        ModelContext subContext = context.resolve(cuboid);
        faces.values().forEach(face -> {
            cuboid.boxes.add(face.export(subContext));
        });
    }

    class JsonFace implements JsonComponent<MsonPlane> {

        private final Face face;

        private final float[] position = new float[3];
        private final int[] size = new int[2];

        public JsonFace(JsonContext context, JsonArray json, Face face) {
            this.face = face;

            position[0] = json.get(0).getAsFloat();
            position[1] = json.get(1).getAsFloat();
            position[2] = json.get(2).getAsFloat();
            size[0] = (int)json.get(3).getAsFloat();
            size[1] = (int)json.get(4).getAsFloat();
        }

        @Override
        public MsonPlane export(ModelContext context) {
            MsonCuboid cuboid = (MsonCuboid)context.getContext();

            float[] pos = face.transformPosition(position, 1);
            Face.Axis axis = face.getAxis();

            return cuboid.createPlane(pos[0], pos[1], pos[2], axis.getWidth(size), axis.getHeight(size), axis.getDeptch(size), 0, face);
        }

    }
}
