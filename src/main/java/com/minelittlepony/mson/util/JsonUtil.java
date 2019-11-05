package com.minelittlepony.mson.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonUtil {


    public static float getFloatOr(String member, JsonObject json, float def) {
        JsonElement el = json.get(member);
        if (el != null && !el.isJsonNull()) {
            return el.getAsFloat();
        }
        return def;
    }

    public static void getInts(JsonObject json, String member, int[] output) {
        JsonArray arr = json.get(member).getAsJsonArray();
        for (int i = 0; i < output.length && i < arr.size(); i++) {
            output[i] = arr.get(i).getAsInt();
        }
    }

    public static void getFloats(JsonObject json, String member, float[] output) {
        JsonArray arr = json.get(member).getAsJsonArray();
        for (int i = 0; i < output.length && i < arr.size(); i++) {
            output[i] = arr.get(i).getAsFloat();
        }
    }
}
