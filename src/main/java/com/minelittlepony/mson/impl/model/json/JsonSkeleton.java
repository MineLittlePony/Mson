package com.minelittlepony.mson.impl.model.json;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.model.traversal.Traversable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class JsonSkeleton implements Traversable<String> {
    private final JsonObject json;

    public JsonSkeleton(JsonObject json) {
        this.json = json;
    }

    @Override
    public void traverse(Traverser<String> traverser) {
        innerTraverse(null, json, traverser, new HashSet<>());
    }

    private void innerTraverse(@Nullable String parent, JsonObject children, BiConsumer<String, String> traverser, Set<String> stack) {
        children.entrySet().forEach(child -> {
            if (parent != null) {
                Preconditions.checkState(stack.add(child.getKey()), "Cyclic reference: " + stack + "," + child.getKey());
                traverser.accept(parent, child.getKey());
            }
            innerTraverse(child.getKey(), child.getValue().getAsJsonObject(), traverser, stack);
        });
    }

}
