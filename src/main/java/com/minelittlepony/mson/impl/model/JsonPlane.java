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
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.util.Incomplete;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class JsonPlane implements JsonComponent<Cuboid> {

    public static final Identifier ID = new Identifier("mson", "plane");

    private final Incomplete<float[]> position;
    private final Incomplete<int[]> size;

    private final Incomplete<Texture> texture;

    private final float[] stretch = new float[3];

    private final Face face;

    public JsonPlane(JsonContext context, JsonObject json) {
        position = context.getVarLookup().getFloats(json, "position", 3);
        size = context.getVarLookup().getInts(json, "size", 3);
        texture = JsonTexture.localized(JsonUtil.accept(json, "texture"));
        JsonUtil.getFloats(json, "stretch", stretch);
        face = Face.valueOf(JsonUtil.require(json, "face").getAsString().toUpperCase());
    }

    @Override
    public Cuboid export(ModelContext context) throws InterruptedException, ExecutionException {
        return new BoxBuilder(context)
            .tex(Optional.of(texture.complete(context)))
            .pos(face.transformPosition(position.complete(context), context))
            .size(face.getAxis(), size.complete(context))
            .stretch(stretch)
            .build(QuadsBuilder.plane(face));
    }
}
