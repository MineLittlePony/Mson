package com.minelittlepony.mson.impl;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.minelittlepony.mson.api.ModelContext.Locals;
import com.minelittlepony.mson.api.exception.FutureAwaitException;
import com.minelittlepony.mson.util.Incomplete;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

class Local implements Incomplete<Float> {

    private final Incomplete<Float> left;
    private final Local.Operation operation;
    private final Incomplete<Float> right;

    Local(JsonArray tokens) {

        if (tokens.size() != 3) {
            throw new JsonParseException(String.format("Saw a local of %d members. Expected 3 of (left, op, right).", tokens.size()));
        }

        operation = Operation.of(tokens.get(1).getAsString());

        if (operation == Operation.VAR) {
            throw new JsonParseException("Invalid operation. One of [+,-,*,/]");
        }

        left = ModelLocalsImpl.createLocal(tokens.get(0));
        right = ModelLocalsImpl.createLocal(tokens.get(2));
    }

    @Override
    public Float complete(Locals locals) throws FutureAwaitException {
        return operation.apply(left.complete(locals), right.complete(locals));
    }

    enum Operation implements BiFunction<Float, Float, Float> {
        ADD("+", (one, two) -> one + two),
        SUBTRACT("-", (one, two) -> one - two),
        MULTIPLY("*", (one, two) -> one * two),
        DIVIDE("/", (one, two) -> one / two),
        MODULUS("%", (one, two) -> one % two),
        EXPONENT("^", (one, two) -> (float)Math.pow(one, two)),
        VAR("", (one, two) -> {throw new RuntimeException("Impossible Operation");});

        static List<Local.Operation> VALUES = Lists.newArrayList(values());
        static Map<String, Local.Operation> REGISTRY = new HashMap<>();

        private final String op;

        private final BiFunction<Float, Float, Float> function;

        Operation(String op, BiFunction<Float, Float, Float> function) {
            this.op = op;
            this.function = function;
        }

        @Override
        public Float apply(Float one, Float two) {
            return function.apply(one, two);
        }

        static Local.Operation of(String op) {
            return REGISTRY.getOrDefault(op, VAR);
        }

        static {
            VALUES.forEach(v -> REGISTRY.put(v.op, v));
        }
    }
}