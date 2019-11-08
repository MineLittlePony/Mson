package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.Box;
import net.minecraft.util.Identifier;

import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.Face.Axis;
import com.minelittlepony.mson.util.JsonUtil;
import com.minelittlepony.mson.util.Qbit;
import com.mojang.realmsclient.util.JsonUtils;

public class JsonBox implements JsonComponent<Box> {

    public static final Identifier ID = new Identifier("mson", "box");

    protected final float[] from = new float[3];

    protected final int[] size = new int[3];

    protected final float stretch;

    protected Qbit mirror = Qbit.UNKNOWN;

    public JsonBox(JsonContext context, JsonObject json) {
        JsonUtil.getFloats(json, "from", from);
        JsonUtil.getInts(json, "size", size);
        stretch = JsonUtil.getFloatOr("stretch", json, 0);
        mirror = json.has("mirror") ? Qbit.of(JsonUtils.getBooleanOr("mirror", json, false)) : Qbit.UNKNOWN;
    }

    @Override
    public Box export(ModelContext context) {
        return new BoxBuilder(context)
            .pos(from)
            .size(size)
            .stretch(stretch)
            .mirror(Axis.X, mirror)
            .build();
    }
}
