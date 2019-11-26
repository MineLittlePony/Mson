package com.minelittlepony.mson.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.realmsclient.util.JsonUtils;

import java.util.Arrays;
import java.util.Optional;

public class JsonUtil {

    public static Optional<JsonElement> accept(JsonObject json, String member) {
        if (json.has(member)) {
            return Optional.of(json.get(member));
        }
        return Optional.empty();
    }

    public static JsonElement require(JsonObject json, String member) {
        if (!json.has(member)) {
            throw new JsonParseException(String.format("Missing required member `%s`", member));
        }
        return json.get(member);
    }

    public static Qbit getQBit(String member, JsonObject json) {
        if (json.has(member)) {
            return Qbit.of(JsonUtils.getBooleanOr(member, json, false));
        }
        return Qbit.UNKNOWN;
    }

    public static float getFloatOr(String member, JsonObject json, float def) {
        JsonElement el = json.get(member);
        if (el != null && !el.isJsonNull()) {
            return el.getAsFloat();
        }
        return def;
    }

    public static void getFloats(JsonObject json, String member, float[] output) {
        accept(json, member).ifPresent(el -> {
            if (el.isJsonArray()) {
                getAsFloats(el.getAsJsonArray(), output);
            } else {
                Arrays.fill(output, el.getAsFloat());
            }
        });
    }

    public static void getAsFloats(JsonArray arr, float[] output) {
        for (int i = 0; i < output.length && i < arr.size(); i++) {
            output[i] = arr.get(i).getAsInt();
        }
    }

    public static void getAsInts(JsonArray arr, int[] output) {
        for (int i = 0; i < output.length && i < arr.size(); i++) {
            output[i] = arr.get(i).getAsInt();
        }
    }

    public static void getBooleans(JsonObject json, String member, boolean[] output) {
        accept(json, member).ifPresent(el -> {
            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isBoolean()) {
                output[0] = el.getAsBoolean();
            } else {
                JsonArray arr = el.getAsJsonArray();
                for (int i = 0; i < output.length && i < arr.size(); i++) {
                    output[i] = arr.get(i).getAsBoolean();
                }
            }
        });
    }
}
