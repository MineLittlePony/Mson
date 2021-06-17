package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.util.Identifier;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.Face;
import com.minelittlepony.mson.api.model.QuadsBuilder;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.impl.MsonImpl;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class JsonPlane implements JsonComponent<Cuboid> {

    public static final Identifier ID = new Identifier("mson", "plane");

    private final Incomplete<float[]> position;
    private final Incomplete<float[]> size;
    private final Incomplete<float[]> dilate;

    private final Incomplete<Texture> texture;

    private final boolean[] mirror = new boolean[3];

    private final Face face;

    public JsonPlane(JsonContext context, String name, JsonObject json) {
        position = context.getLocals().get(json, "position", 3);
        size = context.getLocals().get(json, "size", 3);
        texture = JsonTexture.localized(JsonUtil.accept(json, "texture"));
        JsonUtil.getBooleans(json, "mirror", mirror);
        if (json.has("stretch")) {
            MsonImpl.LOGGER.warn("Model {} is using the `stretch` property. This is deprecated and will be removed in 1.18. Please use `dilate`.", context.getLocals().getModelId());
            dilate = context.getLocals().get(json, "stretch", 3);
        } else {
            dilate = context.getLocals().get(json, "dilate", 3);
        }
        face = Face.valueOf(JsonUtil.require(json, "face").getAsString().toUpperCase());
    }

    @Override
    public Cuboid export(ModelContext context) throws InterruptedException, ExecutionException {
        return new BoxBuilder(context)
            .tex(Optional.of(texture.complete(context)))
            .mirror(face.getAxis(), mirror)
            .pos(position.complete(context))
            .size(face.getAxis(), size.complete(context))
            .dilate(dilate.complete(context))
            .build(QuadsBuilder.plane(face));
    }
}
