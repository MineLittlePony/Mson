package com.minelittlepony.mson.api.model;

public interface ParametersTransformation {
    static ParametersTransformation UNIT = ctx -> ctx.parameters;

    BoxParameters getBoxParameters(BoxBuilder ctx);
}
