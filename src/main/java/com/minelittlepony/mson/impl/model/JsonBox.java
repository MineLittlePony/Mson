package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.Box;
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
import com.minelittlepony.mson.util.Qbit;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class JsonBox implements JsonComponent<Box> {

    public static final Identifier ID = new Identifier("mson", "box");

    protected final Incomplete<float[]> from;

    protected final Incomplete<int[]> size;

    protected final float stretch;

    protected final Qbit mirror;

    protected final Optional<Texture> texture;

    public JsonBox(JsonContext context, JsonObject json) {
        from = context.getVarLookup().getFloats(json, "from", 3);
        size = context.getVarLookup().getInts(json, "size", 3);
        texture = JsonUtil.accept(json, "texture").map(JsonTexture::create);
        stretch = JsonUtil.getFloatOr("stretch", json, 0);
        mirror = JsonUtil.getQBit("mirror", json);
    }

    @Override
    public Box export(ModelContext context) throws InterruptedException, ExecutionException {
        return new BoxBuilder(context)
            .tex(texture)
            .pos(from.complete(context))
            .size(size.complete(context))
            .stretch(stretch)
            .mirror(Axis.X, mirror)
            .build();
    }
}
