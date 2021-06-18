package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.util.Identifier;

import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.Face.Axis;
import com.minelittlepony.mson.api.model.QuadsBuilder;

import java.util.concurrent.ExecutionException;

public class JsonCone extends JsonBox {

    public static final Identifier ID = new Identifier("mson", "cone");

    private final Incomplete<Float> taper;

    public JsonCone(JsonContext context, String name, JsonObject json) {
        super(context, name, json);
        taper = context.getLocals().get(json, "taper");
    }

    @Override
    public Cuboid export(ModelContext context) throws InterruptedException, ExecutionException {
        return new BoxBuilder(context)
            .tex(texture.complete(context))
            .pos(from.complete(context))
            .size(size.complete(context))
            .dilate(dilate.complete(context))
            .mirror(Axis.X, mirror)
            .build(QuadsBuilder.cone(taper.complete(context)));
    }
}
