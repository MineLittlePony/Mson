package com.minelittlepony.mson.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.realms.util.JsonUtils;

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

    public static Optional<Boolean> getBoolean(String member, JsonObject json) {
        if (json.has(member)) {
            return Optional.of(JsonUtils.getBooleanOr(member, json, false));
        }
        return Optional.empty();
    }

    public static float getFloatOr(String member, JsonObject json, float def) {
        JsonElement el = json.get(member);
        if (el != null && el.isJsonPrimitive() && !el.isJsonNull()) {
            return el.getAsFloat();
        }
        return def;
    }

    public static float[] getFloats(JsonObject json, String member, float[] output) {
        accept(json, member).ifPresent(el -> {
            getAsFloats(el.getAsJsonArray(), output);
        });
        return output;
    }

    public static void getAsFloats(JsonArray arr, float[] output) {
        for (int i = 0; i < output.length && i < arr.size(); i++) {
            output[i] = arr.get(i).getAsFloat();
        }
    }

    public static void getAsInts(JsonArray arr, int[] output) {
        for (int i = 0; i < output.length && i < arr.size(); i++) {
            output[i] = arr.get(i).getAsInt();
        }
    }

    public static void getBooleans(JsonObject json, String member, boolean[] output) {
        accept(json, member).ifPresent(el -> {
            if (el.isJsonArray()) {
                getAsBooleans(el.getAsJsonArray(), output);
            } else {
                Arrays.fill(output, el.getAsBoolean());
            }
        });
    }

    private static void getAsBooleans(JsonArray arr, boolean[] output) {
        for (int i = 0; i < output.length && i < arr.size(); i++) {
            output[i] = arr.get(i).getAsBoolean();
        }
    }
}
