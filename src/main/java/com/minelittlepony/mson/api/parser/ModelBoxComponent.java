package com.minelittlepony.mson.api.parser;

import net.minecraft.client.model.geom.ModelPart;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.export.ModelFileWriter;
import com.minelittlepony.mson.api.model.BoxBuilder;

/**
 * A json component.
 *
 * Consumes data and "exports" a concrete model instance, or a piece of a model.
 */
public interface ModelBoxComponent extends ModelComponent<ModelPart.Cube> {
    BoxBuilder builder(ModelContext context);

    @Override
    default ModelPart.Cube export(ModelContext context) {
        return builder(context).build();
    }

    @Override
    default void write(ModelContext context, ModelFileWriter writer) {
        writer.writeBox(builder(context));
    }
}