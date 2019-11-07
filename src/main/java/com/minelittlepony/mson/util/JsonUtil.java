package com.minelittlepony.mson.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

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

    public static float getFloatOr(String member, JsonObject json, float def) {
        JsonElement el = json.get(member);
        if (el != null && !el.isJsonNull()) {
            return el.getAsFloat();
        }
        return def;
    }

    public static void getInts(JsonObject json, String member, int[] output) {
        accept(json, member).map(JsonElement::getAsJsonArray).ifPresent(arr -> {
            for (int i = 0; i < output.length && i < arr.size(); i++) {
                output[i] = arr.get(i).getAsInt();
            }
        });
    }

    public static void getFloats(JsonObject json, String member, float[] output) {
        accept(json, member).map(JsonElement::getAsJsonArray).ifPresent(arr -> {
            for (int i = 0; i < output.length && i < arr.size(); i++) {
                output[i] = arr.get(i).getAsFloat();
            }
        });
    }

    public static void getBooleans(JsonObject json, String member, boolean[] output) {
        accept(json, member).map(JsonElement::getAsJsonArray).ifPresent(arr -> {
            for (int i = 0; i < output.length && i < arr.size(); i++) {
                output[i] = arr.get(i).getAsBoolean();
            }
        });
    }
}
