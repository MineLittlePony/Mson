package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.Box;
import net.minecraft.util.Identifier;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.Face;
import com.minelittlepony.mson.api.model.QuadsBuilder;
import com.minelittlepony.mson.util.JsonUtil;

public class JsonPlane implements JsonComponent<Box> {

    public static final Identifier ID = new Identifier("mson", "plane");

    private final float[] position = new float[3];
    private final int[] size = new int[2];

    private final float stretch;

    private final Face face;

    public JsonPlane(JsonContext context, JsonObject json) {
        JsonUtil.getFloats(json, "position", position);
        JsonUtil.getInts(json, "size", size);
        stretch = JsonUtil.getFloatOr("stretch", json, 0);
        face = Face.valueOf(JsonUtil.require(json, "face").getAsString().toUpperCase());
    }

    @Override
    public Box export(ModelContext context) {
        return new BoxBuilder(context)
            .pos(face.transformPosition(position, context))
            .size(face.getAxis(), size)
            .stretch(stretch)
            .build(QuadsBuilder.plane(face));
    }
}
