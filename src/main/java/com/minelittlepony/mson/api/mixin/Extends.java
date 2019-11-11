package com.minelittlepony.mson.api.mixin;

import com.minelittlepony.mson.api.MsonModel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used on implementors of the MsonModelMixin to indicate which type
 * should be looked at as their "super class" when calling {@link MsonModel#init}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Extends {
    /**
     * The class to serve as the super class.
     */
    Class<? extends MsonModel> value();

    /**
     * True to force application of this extends.
     *
     * Inheritance checks are bypassed, however this may cause errors if used unwisely.
     */
    boolean force() default false;
}
