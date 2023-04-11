package com.minelittlepony.mson.api;

import net.minecraft.client.model.ModelPart;

import java.util.function.Function;

/**
 * Special instance of a model that directly handle's Mson-supplied values.
 *
 * Implementing this adds the init(context) method that will allow modders to reference custom objects.
 * <p>
 * i.e. MineLittlePony's "parts" or the results of a slot with a non-tree output type.
 */
public interface MsonModel {

    @Deprecated
    default void init(ModelContext context) {}

    /**
     * Called to initialise this model with all of its contents.
     *
     * @param view A view into the model contents this component was created from.
     */
    default void init(ModelView view) {
        init((ModelContext)view);
    }

    /**
     * Constructor to create a new mson model.
     */
    public interface Factory<T> extends Function<ModelPart, T> {
        Factory<ModelPart> IDENTITY = tree -> tree;

        T create(ModelPart tree);

        @Override
        default T apply(ModelPart tree) {
            return create(tree);
        }
    }
}
