package com.minelittlepony.mson.api.parser.locals;

import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public enum Operation implements BiFunction<Float, Float, Float> {
    ADD("+", (one, two) -> one + two),
    SUBTRACT("-", (one, two) -> one - two),
    MULTIPLY("*", (one, two) -> one * two),
    DIVIDE("/", (one, two) -> one / two),
    MODULUS("%", (one, two) -> one % two),
    EXPONENT("^", (one, two) -> (float)Math.pow(one, two)),
    VAR("", (one, two) -> {throw new RuntimeException("Impossible Operation");});

    static List<Operation> VALUES = Lists.newArrayList(values());
    static Map<String, Operation> REGISTRY = new HashMap<>();

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

    public static Operation of(String op) {
        return REGISTRY.getOrDefault(op, VAR);
    }

    static {
        VALUES.forEach(v -> REGISTRY.put(v.op, v));
    }
}