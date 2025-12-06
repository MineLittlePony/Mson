package com.minelittlepony.mson.api.model.traversal;

import net.minecraft.client.model.geom.ModelPart;

import java.util.Map;

public interface PartSkeleton extends Traversable<ModelPart> {
    /**
     * @deprecated Will be removed in MC1.22
     */
    @Deprecated(forRemoval = true)
    static PartSkeleton of(ModelPart part) {
        return part;
    }

    ModelPart getSelf();

    Map<String, ModelPart> getChildren();

    /**
     * @deprecated Will be removed in MC1.22
     */
    @Deprecated(forRemoval = true)
    int getTotalDirectCubes();

    @Override
    default void traverse(Traverser<ModelPart> traverser) {
        getChildren().forEach((key, value) -> {
            traverser.accept(getSelf(), value);
            value.traverse(traverser);
        });
    }

    default Traversable<ModelPart> ordered(Traversable<String> traversalOrder) {
        Map<String, ModelPart> elements = getChildren();
        return traverser -> {
            traversalOrder.traverse((parent, child) -> {
                ModelPart p = elements.get(parent);
                ModelPart c = elements.get(child);
                if (p != null && c != null) {
                    traverser.accept(p, c);
                    c.traverse(traverser);
                }
            });
        };
    }

    static Traversable<ModelPart> of(PartSkeleton tree, Traversable<String> traversalOrder) {
        return tree.ordered(traversalOrder);
    }
}
