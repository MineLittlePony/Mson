package com.minelittlepony.mson.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.Arrays;
import java.util.Optional;

public class JsonUtil {
    public static Optional<JsonElement> accept(JsonObject json, String member) {
        return Optional.ofNullable(json.get(member)).filter(j -> !j.isJsonNull());
    }

    public static JsonElement require(JsonObject json, String member, String caller) {
        if (!json.has(member)) {
            throw new JsonParseException(String.format("Missing required member `%s` in %s", member, caller));
        }
        return json.get(member);
    }

    public static Optional<Boolean> acceptBoolean(JsonObject json, String member) {
        return accept(json, member).map(JsonElement::getAsBoolean);
    }

    public static Optional<float[]> acceptFloats(JsonObject json, String member, float[] output) {
        return accept(json, member).map(el -> getAsFloats(el.getAsJsonArray(), output));
    }

    public static Optional<boolean[]> acceptBooleans(JsonObject json, String member, boolean[] output) {
        return accept(json, member).map(el -> getAsBooleans(el, output));
    }

    public static float getFloatOr(String member, JsonObject json, float def) {
        JsonElement el = json.get(member);
        if (el != null && el.isJsonPrimitive() && !el.isJsonNull()) {
            return el.getAsFloat();
        }
        return def;
    }

    private static float[] getAsFloats(JsonElement json, float[] output) {
        if (!json.isJsonArray()) {
            Arrays.fill(output, json.getAsFloat());
            return output;
        }
        JsonArray arr = json.getAsJsonArray();
        for (int i = 0; i < output.length && i < arr.size(); i++) {
            output[i] = arr.get(i).getAsFloat();
        }
        return output;
    }

    @Deprecated
    public static int[] getAsInts(JsonElement json, int[] output) {
        if (!json.isJsonArray()) {
            Arrays.fill(output, json.getAsInt());
            return output;
        }
        JsonArray arr = json.getAsJsonArray();
        for (int i = 0; i < output.length && i < arr.size(); i++) {
            output[i] = arr.get(i).getAsInt();
        }
        return output;
    }

    // Boolean arrays are flexible
    private static boolean[] getAsBooleans(JsonElement json, boolean[] output) {
        if (!json.isJsonArray()) {
            Arrays.fill(output, json.getAsBoolean());
            return output;
        }
        JsonArray arr = json.getAsJsonArray();
        for (int i = 0; i < output.length && i < arr.size(); i++) {
            output[i] = arr.get(i).getAsBoolean();
        }
        return output;
    }
}
