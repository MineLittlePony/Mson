package com.minelittlepony.mson.api.model.traversal;

import java.util.function.BiConsumer;

public interface Traversable<T> {
    /**
     * Visits each parent-child linkage defined by this skeleton.
     */
    void traverse(Traverser<T> traverser);

    interface Traverser<T> extends BiConsumer<T, T> {
        @Override
        void accept(T parent, T child);
    }

    interface Visitor<T> {
        void visit(T part);
    }
}
