package com.minelittlepony.mson.impl.model;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.impl.ModelLocalsImpl;

public interface FileContentLocalsImpl extends FileContent.Locals {
    @Override
    default ModelContext.Locals bake() {
        return new ModelLocalsImpl(this);
    }
}
