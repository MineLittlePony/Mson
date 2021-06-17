package com.minelittlepony.mson.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.util.Incomplete;
import com.minelittlepony.mson.util.JsonUtil;

public interface JsonLocalsImpl extends JsonContext.Locals {
    @Override
    default Incomplete<Float> get(JsonPrimitive json) {
        return ModelLocalsImpl.variableReference(json);
    }

    @Override
    default Incomplete<float[]> get(JsonPrimitive... arr) {

        @SuppressWarnings("unchecked")
        Incomplete<Float>[] output = new Incomplete[arr.length];

        for (int i = 0; i < output.length; i++) {
            output[i] = Incomplete.ZERO;
        }

        for (int i = 0; i < arr.length; i++) {
            if (!arr[i].isJsonPrimitive()) {
                throw new JsonParseException("Non-primitive type found in array. Can only be values (Number) or variable references (#variable). " + arr.toString());
            }
            output[i] = ModelLocalsImpl.variableReference(arr[i].getAsJsonPrimitive());
        }

        return toFloats(output);
    }

    @Override
    default Incomplete<float[]> get(JsonObject json, String member, int len) {
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
                    output[i] = ModelLocalsImpl.variableReference(arr.get(i).getAsJsonPrimitive());
                }
            });

        return toFloats(output);
    }

    @Override
    default Incomplete<Float> get(JsonObject json, String member) {
        JsonElement js = JsonUtil.require(json, member);

        if (!js.isJsonPrimitive()) {
            throw new JsonParseException("Non-primitive type found in member " + member + ". Can only be values (Number) or variable references (#variable). " + js.toString());
        }
        return ModelLocalsImpl.variableReference(js.getAsJsonPrimitive());
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
