package com.minelittlepony.mson.impl;

import net.minecraft.util.Identifier;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.exception.FutureAwaitException;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.util.Maps;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class ModelLocalsImpl implements ModelContext.Locals {

    private final FileContent.Locals context;

    private final Map<String, CompletableFuture<Float>> precalculatedValues = new TreeMap<>();

    public ModelLocalsImpl(FileContent.Locals context) {
        this.context = context;
    }

    @Override
    public Identifier getModelId() {
        return context.getModelId();
    }

    @Override
    public float[] getDilation() {
        try {
            return context.getDilation().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new FutureAwaitException(e);
        }
    }

    @Override
    public Texture getTexture() {
        try {
            return context.getTexture().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new FutureAwaitException(e);
        }
    }

    @Override
    public float getLocal(String name, float defaultValue) {
        try {
            return Maps.computeIfAbsent(precalculatedValues, name, n -> {
                return context.getLocal(n, defaultValue).thenApplyAsync(value -> {
                    return value.complete(new StackFrame(this, n));
                });
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new FutureAwaitException(e);
        }
    }

    @Override
    public Set<String> keys() {
        try {
            return context.keys().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new FutureAwaitException(e);
        }
    }

    @Override
    public String toString() {
        return "[ModelLocalsImpl id=" + context.getModelId().toString() + "]";
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
        public Texture getTexture() {
            return parent.getTexture();
        }

        @Override
        public float[] getDilation() {
            return parent.getDilation();
        }

        @Override
        public float getLocal(String name, float defaultValue) {
            if (currentVariableRef.equalsIgnoreCase(name)) {
                throw new RuntimeException("Cyclical reference. " + toString());
            }
            return parent.getLocal(name, defaultValue);
        }

        @Override
        public Set<String> keys() {
            return parent.keys();
        }

        @Override
        public String toString() {
            return parent.toString() + " -> " + currentVariableRef;
        }
    }
}
