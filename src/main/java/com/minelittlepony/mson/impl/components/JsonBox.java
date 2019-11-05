package com.minelittlepony.mson.impl.components;

import net.minecraft.client.model.Box;
import net.minecraft.client.model.Cuboid;
import net.minecraft.util.Identifier;

import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.util.JsonUtil;
import com.mojang.realmsclient.util.JsonUtils;

public class JsonBox implements JsonComponent<Box> {

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
    public Box export(ModelContext context) {
        Cuboid cuboid = (Cuboid)context.getContext();
        return new Box(cuboid, 0, 0, from[0], from[1], from[2], size[0], size[1], size[2], stretch, mirror);
    }

}
