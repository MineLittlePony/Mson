package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.Box;
import net.minecraft.util.Identifier;

import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.Face.Axis;
import com.minelittlepony.mson.api.model.QuadsBuilder;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.concurrent.ExecutionException;

public class JsonCone extends JsonBox {

    public static final Identifier ID = new Identifier("mson", "cone");

    private final float taper;

    public JsonCone(JsonContext context, JsonObject json) {
        super(context, json);
        taper = JsonUtil.require(json, "taper").getAsFloat();
    }

    @Override
    public Box export(ModelContext context) throws InterruptedException, ExecutionException {
        return new BoxBuilder(context)
            .pos(from.complete(context))
            .size(size.complete(context))
            .stretch(stretch)
            .mirror(Axis.X, mirror)
            .build(QuadsBuilder.cone(taper));
    }
}
