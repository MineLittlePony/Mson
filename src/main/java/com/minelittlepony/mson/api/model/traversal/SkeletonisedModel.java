package com.minelittlepony.mson.api.model.traversal;

import net.minecraft.client.model.ModelPart;

public interface SkeletonisedModel {
    Traversable<ModelPart> getSkeleton();

    void setSkeleton(Traversable<ModelPart> skeleton);
}
