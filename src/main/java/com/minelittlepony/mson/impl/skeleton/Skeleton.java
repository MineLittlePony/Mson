package com.minelittlepony.mson.impl.skeleton;

import net.minecraft.client.model.ModelPart;

public interface Skeleton {
    /**
     * Visits each parent-child linkage defined by this skeleton.
     */
    void traverse(Traverser traverser);

    interface Traverser {
        void accept(ModelPart parent, ModelPart child);
    }

    interface Visitor {
        void visit(ModelPart part);
    }
}
