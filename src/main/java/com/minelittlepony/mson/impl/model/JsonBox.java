package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.util.Identifier;

import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.api.model.Face.Axis;
import com.minelittlepony.mson.util.Incomplete;
import com.minelittlepony.mson.util.JsonUtil;
import com.minelittlepony.mson.util.TriState;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class JsonBox implements JsonComponent<Cuboid> {

    public static final Identifier ID = new Identifier("mson", "box");

    protected final Incomplete<float[]> from;

    protected final Incomplete<int[]> size;

    protected final float[] stretch = new float[3];

    protected final TriState mirror;

    protected final Optional<Texture> texture;

    public JsonBox(JsonContext context, JsonObject json) {
        from = context.getVarLookup().getFloats(json, "from", 3);
        size = context.getVarLookup().getInts(json, "size", 3);
        texture = JsonUtil.accept(json, "texture").map(JsonTexture::create);
        JsonUtil.getFloats(json, "stretch", stretch);
        mirror = JsonUtil.getTriState("mirror", json);
    }

    @Override
    public Cuboid export(ModelContext context) throws InterruptedException, ExecutionException {
        return new BoxBuilder(context)
            .tex(texture)
            .pos(from.complete(context))
            .size(size.complete(context))
            .stretch(stretch)
            .mirror(Axis.X, mirror)
            .build();
    }
}
