package com.minelittlepony.mson.api.model.traversal;

import net.minecraft.client.model.ModelPart;

import java.util.Map;

public interface PartSkeleton extends Traversable<ModelPart> {
    static PartSkeleton of(ModelPart part) {
        return (PartSkeleton)(Object)part;
    }

    ModelPart getSelf();

    Map<String, ModelPart> getChildren();

    @Deprecated
    int getTotalDirectCubes();

    @Override
    default void traverse(Traverser<ModelPart> traverser) {
        getChildren().forEach((key, value) -> {
            traverser.accept(getSelf(), value);
            of(value).traverse(traverser);
        });
    }

    static Traversable<ModelPart> of(ModelPart tree, Traversable<String> traversalOrder) {
        Map<String, ModelPart> elements = PartSkeleton.of(tree).getChildren();
        return traverser -> {
            traversalOrder.traverse((parent, child) -> {
                ModelPart p = elements.get(parent);
                ModelPart c = elements.get(child);
                if (p != null && c != null) {
                    traverser.accept(p, c);
                    PartSkeleton.of(c).traverse(traverser);
                }
            });
        };
    }
}
