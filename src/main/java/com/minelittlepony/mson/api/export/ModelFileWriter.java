package com.minelittlepony.mson.api.export;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.PartBuilder;
import com.minelittlepony.mson.api.parser.FileContent;

import java.util.function.BiConsumer;

public interface ModelFileWriter {

    ModelFileWriter write(ModelContext context, Writeable element);

    ModelFileWriter write(String name, ModelContext context, Writeable element);

    ModelFileWriter writePart(String name, PartBuilder part, BiConsumer<ModelFileWriter, PartBuilder> content);

    ModelFileWriter writeBox(BoxBuilder box);

    ModelFileWriter writeTree(String name, FileContent<?> content, ModelContext context);

    public interface Writeable {
        void write(ModelContext context, ModelFileWriter writer);

        default Writeable replace(ModelContext context, FileContent<?> content) {
            return this;
        }
    }
}
