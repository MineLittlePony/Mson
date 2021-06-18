package com.minelittlepony.mson.impl;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext.Locals;
import com.minelittlepony.mson.api.exception.FutureAwaitException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class Local implements Incomplete<Float> {
    private final Incomplete<Float> left;
    private final Local.Operation operation;
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

    public static Block of(Optional<JsonElement> json) {
        return new Block(json
                .map(JsonElement::getAsJsonObject)
                .map(JsonObject::entrySet)
                .orElseGet(() -> new HashSet<>())
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Local.create(e.getValue()))));
    }

    static Incomplete<Float> ref(JsonPrimitive prim) {

        if (prim.isNumber()) {
            return Incomplete.completed(prim.getAsFloat());
        }

        if (prim.isString()) {
            String variableName = prim.getAsString();
            if (variableName.startsWith("#")) {
                String name = variableName.substring(1);
                return local -> {
                    try {
                        return local.getLocal(name).get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new FutureAwaitException(e);
                    }
                };
            }
            return Incomplete.ZERO;
        }

        throw new JsonParseException("Unsupported local value type: " + prim.toString());
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

    public static final class Block {
        private final Map<String, Incomplete<Float>> locals;

        Block(Map<String, Incomplete<Float>> locals) {
            this.locals = locals;
        }

        public Map<String, Incomplete<Float>> entries() {
            return locals;
        }

        public Set<String> appendKeys(Set<String> output) {
            output.addAll(locals.keySet());
            return output;
        }

        public Optional<CompletableFuture<Incomplete<Float>>> get(String name) {
            if (locals.containsKey(name)) {
                return Optional.of(CompletableFuture.completedFuture(locals.get(name)));
            }
            return Optional.empty();
        }
    }
}