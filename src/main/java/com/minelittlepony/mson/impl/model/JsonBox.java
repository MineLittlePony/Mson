package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.util.Identifier;

import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.impl.MsonImpl;
import com.minelittlepony.mson.api.model.Face.Axis;
import com.minelittlepony.mson.util.Incomplete;
import com.minelittlepony.mson.util.JsonUtil;
import com.minelittlepony.mson.util.TriState;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class JsonBox implements JsonComponent<Cuboid> {

    public static final Identifier ID = new Identifier("mson", "box");

    protected final Incomplete<float[]> from;

    protected final Incomplete<float[]> size;

    protected final Incomplete<float[]> dilate;

    protected final TriState mirror;

    protected final Optional<Texture> texture;

    public JsonBox(JsonContext context, String name, JsonObject json) {
        from = context.getLocals().get(json, "from", 3);
        size = context.getLocals().get(json, "size", 3);
        texture = JsonUtil.accept(json, "texture").map(JsonTexture::create);
        mirror = JsonUtil.getTriState("mirror", json);
        if (json.has("stretch")) {
            MsonImpl.LOGGER.warn("Model {} is using the `stretch` property. This is deprecated and will be removed in 1.18. Please use `dilate`.", context.getLocals().getModelId());
            dilate = context.getLocals().get(json, "stretch", 3);
        } else {
            dilate = context.getLocals().get(json, "dilate", 3);
        }
    }

    @Override
    public Cuboid export(ModelContext context) throws InterruptedException, ExecutionException {
        return new BoxBuilder(context)
            .tex(texture)
            .pos(from.complete(context))
            .size(size.complete(context))
            .dilate(dilate.complete(context))
            .mirror(Axis.X, mirror)
            .build();
    }
}
