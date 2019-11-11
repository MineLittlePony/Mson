package com.minelittlepony.mson.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minelittlepony.mson.api.json.Variables;
import com.minelittlepony.mson.util.Incomplete;
import com.minelittlepony.mson.util.JsonUtil;

final class VariablesImpl implements Variables {

    static Variables INSTANCE = new VariablesImpl();

    private Incomplete<Float>[] getIncompletes(JsonObject json, String member, int len) {
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

    @Override
    public Incomplete<int[]> getInts(JsonObject json, String member, int len) {
        return toInts(getIncompletes(json, member, len));
    }

    @Override
    public Incomplete<float[]> getFloats(JsonObject json, String member, int len) {
        return toFloats(getIncompletes(json, member, len));
    }

    private Incomplete<int[]> toInts(Incomplete<Float>[] input) {
        return locals -> {
            int[] result = new int[input.length];
            for (int i = 0; i < input.length; i++) {
                result[i] = input[i].complete(locals).intValue();
            }
            return result;
        };
    }

    private Incomplete<float[]> toFloats(Incomplete<Float>[] input) {
        return locals -> {
            float[] result = new float[input.length];
            for (int i = 0; i < input.length; i++) {
                result[i] = input[i].complete(locals);
            }
            return result;
        };
    }

}
