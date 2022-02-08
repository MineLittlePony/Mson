package com.minelittlepony.mson.impl.skeleton;

import net.minecraft.client.model.ModelPart;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class JsonSkeleton {
    private final JsonObject json;

    public JsonSkeleton(JsonObject json) {
        this.json = json;
    }

    public Skeleton getSkeleton(ModelPart tree) {
        Map<String, ModelPart> elements = PartSkeleton.of(tree).getChildren();
        return traverser -> {
            traverse((parent, child) -> {
                ModelPart p = elements.get(parent);
                ModelPart c = elements.get(child);
                if (p != null && c != null) {
                    traverser.accept(p, c);
                    PartSkeleton.of(c).traverse(traverser);
                }
            });
        };
    }

    public void traverse(BiConsumer<String, String> traverser) {
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
