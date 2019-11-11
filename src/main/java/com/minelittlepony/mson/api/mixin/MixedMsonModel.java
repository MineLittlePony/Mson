package com.minelittlepony.mson.api.mixin;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.impl.invoke.MsonModelMixinImpl;

/**
 * A mixin version of the MsonModel.
 *
 * Use this and annotate your super class with {@link Extends}
 * when you can't normally extend those classes, but be weary that
 * the class's method will be applied to this class's instance regardless
 * of inheritance or compatibility.
 *
 * This should only be used on adjacent siblings who share
 * the same parent class where the requested super does not introduce any
 * fields specific to itself.
 *
 * Calling {@link MsonModel#init} will delegate to that class as your super,
 * and can be overridden normally by overriding the interface method.
 *
 */
public interface MixedMsonModel extends MsonModel {
    @Override
    default void init(ModelContext context) {
        MsonModelMixinImpl.getSuper(this).init(context);
    }
}
