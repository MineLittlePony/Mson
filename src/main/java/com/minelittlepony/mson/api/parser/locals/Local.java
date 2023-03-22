package com.minelittlepony.mson.api.parser.locals;

import net.minecraft.util.Identifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelView.Locals;
import com.minelittlepony.mson.api.exception.FutureAwaitException;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.Arrays;
import java.util.stream.Stream;

public class Local implements Incomplete<Float> {
    private final Incomplete<Float> left;
    private final Operation operation;
    private final Incomplete<Float> right;

    private Local(JsonArray tokens) {
        if (tokens.size() != 3) {
            throw new JsonParseException(String.format("Saw a local of %d members. Expected 3 of (left, op, right).", tokens.size()));
        }

        operation = Operation.of(tokens.get(1).getAsString());

        if (operation == Operation.VAR) {
            throw new JsonParseException("Invalid operation. One of [+,-,*,/]");
        }

        left = create(tokens.get(0));
        right = create(tokens.get(2));
    }

    @Override
    public Float complete(Locals locals) throws FutureAwaitException {
        return operation.apply(left.complete(locals), right.complete(locals));
    }

    public static Incomplete<Float> create(JsonElement json) {
        if (json.isJsonPrimitive()) {
            return ref(json.getAsJsonPrimitive());
        }
        if (json.isJsonArray()) {
            return new Local(json.getAsJsonArray());
        }

        throw new JsonParseException("Unsupported local type. A local must be either a value (number) string (#variable) or an array");
    }

    public static Incomplete<Float> ref(JsonPrimitive prim) {

        if (prim.isNumber()) {
            return Incomplete.completed(prim.getAsFloat());
        }

        if (prim.isString()) {
            String variableName = prim.getAsString();
            if (variableName.startsWith("#")) {
                String name = variableName.substring(1);
                return local -> local.getLocal(name, 0F);
            }
            return Incomplete.ZERO;
        }

        throw new JsonParseException("Unsupported local value type: " + prim.toString());
    }

    @SuppressWarnings("unchecked")
    public static Incomplete<float[]> array(JsonPrimitive... arr) {
        return toFloats(
                (Incomplete<Float>[])Stream.of(arr)
                .map(Local::ref)
                .toArray(Incomplete[]::new)
        );
    }

    public static Incomplete<float[]> array(JsonObject json, String member, int len, Identifier modelId) {
        return toFloats(JsonUtil.accept(json, member).map(js -> {
                Incomplete<Float>[] output = zeros(len);

                if (!js.isJsonArray()) {
                    Arrays.fill(output, Local.ref(js.getAsJsonPrimitive()));
                } else {
                    JsonArray arr = js.getAsJsonArray();

                    for (int i = 0; i < len && i < arr.size(); i++) {
                        if (!arr.get(i).isJsonPrimitive()) {
                            throw new JsonParseException(String.format("Non-primitive type found in array for model %s. Can only be values (Number) or variable references (#variable). %s", modelId, arr));
                        }
                        output[i] = Local.ref(arr.get(i).getAsJsonPrimitive());
                    }
                }
                return output;
            }).orElseGet(() -> zeros(len)));
    }

    public static Incomplete<Float> ref(JsonObject json, String member, Identifier modelId) {
        JsonElement js = JsonUtil.require(json, member, "Locals", modelId);

        if (!js.isJsonPrimitive()) {
            throw new JsonParseException(String.format("Non-primitive type found in member %s for model %s. Can only be values (Number) or variable references (#variable). %s", member, modelId, js));
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