package com.minelittlepony.mson.impl.model;

import net.minecraft.util.Identifier;

import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.MsonBox;
import com.minelittlepony.mson.api.model.MsonCuboid;
import com.minelittlepony.mson.util.JsonUtil;
import com.mojang.realmsclient.util.JsonUtils;

public class JsonBox implements JsonComponent<MsonBox> {

    public static final Identifier ID = new Identifier("mson", "box");

    private final float[] from = new float[3];

    private final int[] size = new int[3];

    private final float stretch;

    private final boolean mirror;

    public JsonBox(JsonContext context, JsonObject json) {
        JsonUtil.getFloats(json, "from", from);
        JsonUtil.getInts(json, "size", size);
        stretch = JsonUtil.getFloatOr("stretch", json, 0);
        mirror = JsonUtils.getBooleanOr("mirror", json, false);
    }

    @Override
    public MsonBox export(ModelContext context) {
        MsonCuboid cuboid = (MsonCuboid)context.getContext();
        return cuboid.createBox(from[0], from[1], from[2], size[0], size[1], size[2], stretch, mirror);
    }
}
