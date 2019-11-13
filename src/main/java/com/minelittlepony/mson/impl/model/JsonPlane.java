package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.util.Identifier;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.Face;
import com.minelittlepony.mson.api.model.QuadsBuilder;
import com.minelittlepony.mson.util.Incomplete;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.concurrent.ExecutionException;

public class JsonPlane implements JsonComponent<Cuboid> {

    public static final Identifier ID = new Identifier("mson", "plane");

    private final Incomplete<float[]> position;
    private final Incomplete<int[]> size;

    private final float stretch;

    private final Face face;

    public JsonPlane(JsonContext context, JsonObject json) {
        position = context.getVarLookup().getFloats(json, "position", 3);
        size = context.getVarLookup().getInts(json, "size", 3);
        stretch = JsonUtil.getFloatOr("stretch", json, 0);
        face = Face.valueOf(JsonUtil.require(json, "face").getAsString().toUpperCase());
    }

    @Override
    public Cuboid export(ModelContext context) throws InterruptedException, ExecutionException {
        return new BoxBuilder(context)
            .pos(face.transformPosition(position.complete(context), context))
            .size(face.getAxis(), size.complete(context))
            .stretch(stretch)
            .build(QuadsBuilder.plane(face));
    }
}
