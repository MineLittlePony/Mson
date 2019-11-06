package com.minelittlepony.mson.impl.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.util.JsonUtil;
import com.mojang.realmsclient.util.JsonUtils;

public class JsonTexture implements Texture {

    public static final Texture EMPTY = new JsonTexture(new JsonObject());

    private int[] parameters;

    public JsonTexture(JsonObject json) {
        this(json, 0, 0, 64, 32);
    }

    public JsonTexture(JsonObject json, Texture inherited) {
        this(json, inherited.getU(), inherited.getV(), inherited.getWidth(), inherited.getHeight());
    }

    private JsonTexture(JsonObject json, int... inherited) {
        parameters = inherited;

        if (json.has("texture")) {
            JsonElement el = json.get("texture");
            if (el.isJsonObject()) {
                JsonObject tex = el.getAsJsonObject();
                parameters = new int[] {
                        JsonUtils.getIntOr("u", tex, getU()),
                        JsonUtils.getIntOr("v", tex, getV()),
                        JsonUtils.getIntOr("w", tex, getWidth()),
                        JsonUtils.getIntOr("h", tex, getHeight())
                };
            } else if (el.isJsonArray()) {
                JsonUtil.getInts(json, "texture", parameters);
            }
        }
    }

    @Override
    public int getU() {
        return parameters[0];
    }

    @Override
    public int getV() {
        return parameters[1];
    }

    @Override
    public int getWidth() {
        return parameters[2];
    }

    @Override
    public int getHeight() {
        return parameters[3];
    }
}
