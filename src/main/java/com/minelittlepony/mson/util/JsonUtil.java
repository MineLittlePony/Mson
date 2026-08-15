package com.minelittlepony.mson.util;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JsonUtil {
    public static Optional<JsonElement> accept(JsonObject json, String member) {
        return Optional.ofNullable(json.get(member)).filter(j -> !j.isJsonNull());
    }

    public static JsonElement require(JsonObject json, String member, Object... callerStack) {
        if (!json.has(member)) {
            String caller = String.join(" in ", Arrays.stream(callerStack).map(Object::toString).toArray(String[]::new));
            throw new JsonParseException(String.format("Missing required member `%s` in %s", member, caller));
        }
        return json.get(member);
    }

    public static Optional<Boolean> acceptBoolean(JsonObject json, String member) {
        return accept(json, member).map(JsonElement::getAsBoolean);
    }

    public static Optional<float[]> acceptOptionalFloats(JsonObject json, String member, int count) {
        return accept(json, member).map(el -> getAsFloats(el, new float[count]));
    }

    public static float[] acceptFloats(JsonObject json, String member, int count) {
        return acceptOptionalFloats(json, member, count).orElseGet(() -> new float[count]);
    }

    public static Optional<boolean[]> acceptOptionalBooleans(JsonObject json, String member, int count) {
        return accept(json, member).map(el -> getAsBooleans(el, new boolean[count]));
    }


    public static boolean[] acceptBooleans(JsonObject json, String member, int count) {
        return acceptOptionalBooleans(json, member, count).orElseGet(() -> new boolean[count]);
    }

    public static float getFloatOr(String member, JsonObject json, float def) {
        JsonElement el = json.get(member);
        if (el != null && el.isJsonPrimitive() && !el.isJsonNull()) {
            return el.getAsFloat();
        }
        return def;
    }

    public static int getIntOr(String member, JsonObject json, int def) {
        JsonElement el = json.get(member);
        if (el != null && el.isJsonPrimitive() && !el.isJsonNull()) {
            return el.getAsInt();
        }
        return def;
    }

    public static boolean getBooleanOr(String member, JsonObject json, boolean def) {
        JsonElement el = json.get(member);
        if (el != null && el.isJsonPrimitive() && !el.isJsonNull()) {
            return el.getAsBoolean();
        }
        return def;
    }

    private static float[] getAsFloats(JsonElement json, float[] output) {
        if (!json.isJsonArray()) {
            Arrays.fill(output, json.getAsFloat());
            return output;
        }
        JsonArray arr = json.getAsJsonArray();

        if (arr.size() != output.length) {
            throw new JsonParseException("Expected array of " + output.length + " elements. Instead got " + arr.size());
        }

        for (int i = 0; i < output.length; i++) {
            output[i] = arr.get(i).getAsFloat();
        }

        return output;
    }

    private static boolean[] getAsBooleans(JsonElement json, boolean[] output) {
        if (!json.isJsonArray()) {
            Arrays.fill(output, json.getAsBoolean());
            return output;
        }
        JsonArray arr = json.getAsJsonArray();

        if (arr.size() != output.length) {
            throw new JsonParseException("Expected array of " + output.length + " elements. Instead got " + arr.size());
        }

        for (int i = 0; i < output.length; i++) {
            output[i] = arr.get(i).getAsBoolean();
        }
        return output;
    }

    public static <T> Optional<Set<T>> acceptSet(JsonElement json, Function<JsonElement, T> serializerFunc, @Nullable T exclude) {
        if (!json.isJsonArray()) {
            return Optional.empty();
        }
        return Optional.of(json.getAsJsonArray().asList().stream().map(serializerFunc)
                .filter(i -> i != exclude)
                .collect(Collectors.toUnmodifiableSet()))
                .filter(set -> !set.isEmpty());
    }

    public static <T extends Enum<T>> Function<JsonElement, T> enumSerializaerFunc(Function<String, T> lookup, T def) {
        return json -> {
            if (!json.isJsonPrimitive()) {
                return def;
            }
            return MoreObjects.firstNonNull(lookup.apply(json.getAsString().toUpperCase(Locale.ROOT)), def);
        };
    }
}
