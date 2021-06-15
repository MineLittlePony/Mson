package com.minelittlepony.mson.impl;

import net.minecraft.util.Identifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.exception.FutureAwaitException;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.util.Incomplete;
import com.minelittlepony.mson.util.Maps;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class LocalsImpl implements ModelContext.Locals, VariablesImpl {

    private final Identifier id;
    private final JsonContext.Variables context;

    private final Map<String, CompletableFuture<Float>> precalculatedValues = new HashMap<>();

    public LocalsImpl(Identifier id, JsonContext.Variables context) {
        this.id = id;
        this.context = context;
    }

    @Override
    public CompletableFuture<Incomplete<Float>> getVariable(String name) {
        return context.getVariable(name);
    }

    @Override
    public Identifier getModelId() {
        return id;
    }

    @Override
    public CompletableFuture<Texture> getTexture() {
        return context.getTexture();
    }

    @Override
    public CompletableFuture<Float> getValue(String name) {
        return Maps.computeIfAbsent(precalculatedValues, name, this::lookupValue);
    }

    private CompletableFuture<Float> lookupValue(String name) {
        return context.getVariable(name).thenApplyAsync(value -> {
            try {
                return value.complete(new StackFrame(this, name));
            } catch (InterruptedException | ExecutionException e) {
                throw new FutureAwaitException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Set<String>> getKeys() {
        return context.getKeys();
    }

    @Override
    public String toString() {
        return "[LocalsImpl id=" + id.toString() + "]";
    }

    static Incomplete<Float> variableReference(JsonPrimitive prim) {

        if (prim.isNumber()) {
            return Incomplete.completed(prim.getAsFloat());
        }

        if (prim.isString()) {
            String variableName = prim.getAsString();
            if (variableName.startsWith("#")) {
                String name = variableName.substring(1);
                return local -> local.getValue(name).get();
            }
            return Incomplete.ZERO;
        }

        throw new JsonParseException("Unsupported local value type: " + prim.toString());
    }

    public static Incomplete<Float> createLocal(JsonElement json) {
        if (json.isJsonPrimitive()) {
            return variableReference(json.getAsJsonPrimitive());
        }
        if (json.isJsonArray()) {
            return new Local(json.getAsJsonArray());
        }

        throw new JsonParseException("Unsupported local type. A local must be either a value (number) string (#variable) or an array");
    }

    private static final class StackFrame implements ModelContext.Locals {

        private final String currentVariableRef;
        private final ModelContext.Locals parent;

        private StackFrame(ModelContext.Locals parent, String currentVariableRef) {
            this.currentVariableRef = currentVariableRef;
            this.parent = parent;
        }

        @Override
        public Identifier getModelId() {
            return parent.getModelId();
        }

        @Override
        public CompletableFuture<Texture> getTexture() {
            return parent.getTexture();
        }

        @Override
        public CompletableFuture<Float> getValue(String name) {
            if (currentVariableRef.equalsIgnoreCase(name)) {
                throw new RuntimeException("Cyclical reference. " + toString());
            }
            return parent.getValue(name);
        }

        @Override
        public CompletableFuture<Set<String>> getKeys() {
            return parent.getKeys();
        }

        @Override
        public String toString() {
            return parent.toString() + " -> " + currentVariableRef;
        }
    }
}
