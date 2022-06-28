package com.minelittlepony.mson.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.Arrays;
import java.util.stream.Stream;

public interface JsonLocalsImpl extends JsonContext.Locals {
    @Override
    default Incomplete<Float> get(JsonPrimitive json) {
        return Local.ref(json);
    }

    @SuppressWarnings("unchecked")
    @Override
    default Incomplete<float[]> get(JsonPrimitive... arr) {
        return toFloats(
                (Incomplete<Float>[])Stream.of(arr)
                .map(Local::ref)
                .toArray(Incomplete[]::new)
        );
    }

    @Override
    default Incomplete<float[]> get(JsonObject json, String member, int len) {
        return toFloats(JsonUtil.accept(json, member).map(js -> {
                Incomplete<Float>[] output = zeros(len);

                if (!js.isJsonArray()) {
                    Arrays.fill(output, Local.ref(js.getAsJsonPrimitive()));
                } else {
                    JsonArray arr = js.getAsJsonArray();

                    for (int i = 0; i < len && i < arr.size(); i++) {
                        if (!arr.get(i).isJsonPrimitive()) {
                            throw new JsonParseException(String.format("Non-primitive type found in array for model %s. Can only be values (Number) or variable references (#variable). %s", getModelId(), arr));
                        }
                        output[i] = Local.ref(arr.get(i).getAsJsonPrimitive());
                    }
                }
                return output;
            }).orElseGet(() -> zeros(len)));
    }

    @Override
    default Incomplete<Float> get(JsonObject json, String member) {
        JsonElement js = JsonUtil.require(json, member, "Locals", getModelId());

        if (!js.isJsonPrimitive()) {
            throw new JsonParseException(String.format("Non-primitive type found in member %s for model %s. Can only be values (Number) or variable references (#variable). %s", member, getModelId(), js));
        }
        return Local.ref(js.getAsJsonPrimitive());
    }

    private static Incomplete<Float>[] zeros(int len) {
        @SuppressWarnings("unchecked")
        Incomplete<Float>[] output = new Incomplete[len];
        Arrays.fill(output, Incomplete.ZERO);
        return output;
    }

    private static Incomplete<float[]> toFloats(Incomplete<Float>[] input) {
        return locals -> {
            float[] result = new float[input.length];
            for (int i = 0; i < input.length; i++) {
                result[i] = input[i].complete(locals);
            }
            return result;
        };
    }
}
