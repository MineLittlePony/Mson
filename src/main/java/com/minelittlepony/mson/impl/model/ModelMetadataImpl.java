package com.minelittlepony.mson.impl.model;

import net.minecraft.util.Identifier;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.ModelMetadata;
import com.minelittlepony.mson.api.exception.FutureAwaitException;
import com.minelittlepony.mson.api.model.Texture;

import java.util.concurrent.ExecutionException;

public class ModelMetadataImpl implements ModelMetadata {

    private final ModelContext.Locals locals;

    public ModelMetadataImpl(ModelContext.Locals locals) {
        this.locals = locals;
    }

    public ModelContext.Locals getUnchecked() {
        return locals;
    }

    @Override
    public Identifier getModelId() {
        // TODO Auto-generated method stub
        return locals.getModelId();
    }

    @Override
    public Texture getTexture() {
        try {
            return locals.getTexture().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new FutureAwaitException(e);
        }
    }

    @Override
    public float[] getDilation() {
        try {
            return locals.getDilation().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new FutureAwaitException(e);
        }
    }
}
