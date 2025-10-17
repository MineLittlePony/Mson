package com.minelittlepony.mson.api.parser;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.Cuboid;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.export.ModelFileWriter;
import com.minelittlepony.mson.api.model.BoxBuilder;

/**
 * A json component.
 *
 * Consumes data and "exports" a concrete model instance, or a piece of a model.
 */
public interface ModelBoxComponent extends ModelComponent<ModelPart.Cuboid> {
    BoxBuilder builder(ModelContext context);

    @Override
    default Cuboid export(ModelContext context) {
        return builder(context).build();
    }

    @Override
    default void write(ModelContext context, ModelFileWriter writer) {
        writer.writeBox(builder(context));
    }
}