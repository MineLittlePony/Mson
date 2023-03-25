package com.minelittlepony.mson.api.export;

import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.PartBuilder;
import com.minelittlepony.mson.api.parser.FileContent;

import java.util.function.Consumer;

public interface ModelFileWriter {
    ModelFileWriter writePart(String name, PartBuilder part, Consumer<ModelFileWriter> content);

    ModelFileWriter writeBox(BoxBuilder box);

    ModelFileWriter writeTree(String name, FileContent<?> content, ModelContext context);

    public interface Writeable {
        void write(ModelContext context, ModelFileWriter writer);
    }
}
