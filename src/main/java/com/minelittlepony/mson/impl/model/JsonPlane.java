package com.minelittlepony.mson.impl.model;

import net.minecraft.util.Identifier;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.Face;
import com.minelittlepony.mson.api.model.MsonCuboid;
import com.minelittlepony.mson.api.model.MsonPlane;
import com.minelittlepony.mson.util.JsonUtil;

public class JsonPlane implements JsonComponent<MsonPlane> {

    public static final Identifier ID = new Identifier("mson", "plane");

    private final float[] position = new float[3];
    private final int[] size = new int[2];

    private final Face face;

    public JsonPlane(JsonContext context, JsonObject json) {
        JsonUtil.getFloats(json, "position", position);
        JsonUtil.getInts(json, "size", size);
        face = Face.valueOf(json.get("face").getAsString().toUpperCase());
    }

    @Override
    public MsonPlane export(ModelContext context) {
        MsonCuboid cuboid = (MsonCuboid)context.getContext();

        float[] pos = face.transformPosition(position, 1);
        Face.Axis axis = face.getAxis();

        return cuboid.createPlane(pos[0], pos[1], pos[2], axis.getWidth(size), axis.getHeight(size), axis.getDeptch(size), 0, face);
    }

}
