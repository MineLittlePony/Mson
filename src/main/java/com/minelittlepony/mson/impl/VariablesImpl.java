package com.minelittlepony.mson.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.util.Incomplete;
import com.minelittlepony.mson.util.JsonUtil;

public interface VariablesImpl extends JsonContext.Variables {
    @Override
    default Incomplete<Float> getFloat(JsonPrimitive json) {
        return LocalsImpl.variableReference(json);
    }

    @Override
    default Incomplete<float[]> getFloats(JsonObject json, String member, int len) {
        return toFloats(getIncompletes(json, member, len));
    }

    private static Incomplete<Float>[] getIncompletes(JsonObject json, String member, int len) {
        @SuppressWarnings("unchecked")
        Incomplete<Float>[] output = new Incomplete[len];

        for (int i = 0; i < len; i++) {
            output[i] = Incomplete.ZERO;
        }
        JsonUtil.accept(json, member)
            .map(JsonElement::getAsJsonArray)
            .ifPresent(arr -> {
                for (int i = 0; i < len && i < arr.size(); i++) {
                    if (!arr.get(i).isJsonPrimitive()) {
                        throw new JsonParseException("Non-primitive type found in array. Can only be values (Number) or variable references (#variable). " + arr.toString());
                    }
                    output[i] = LocalsImpl.variableReference(arr.get(i).getAsJsonPrimitive());
                }
            });

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
