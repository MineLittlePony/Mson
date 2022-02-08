package com.minelittlepony.mson.impl.skeleton;

import net.minecraft.client.model.ModelPart;

import java.util.Map;

public interface PartSkeleton extends Skeleton {
    static PartSkeleton of(ModelPart part) {
        return (PartSkeleton)(Object)part;
    }

    ModelPart getSelf();

    Map<String, ModelPart> getChildren();

    int getTotalDirectCubes();

    @Override
    default void traverse(Traverser traverser) {
        getChildren().forEach((key, value) -> {
            traverser.accept(getSelf(), value);
            of(value).traverse(traverser);
        });
    }
}
