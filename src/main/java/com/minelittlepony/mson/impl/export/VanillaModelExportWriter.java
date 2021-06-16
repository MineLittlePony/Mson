package com.minelittlepony.mson.impl.export;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Stack;

public class VanillaModelExportWriter {
    private final VanillaModelExporter exporter = new VanillaModelExporter();

    public void exportAll(Path root) {
        ((ModelList)MinecraftClient.getInstance().getEntityModelLoader()).getModelParts().forEach((id, model) -> {
            writeToFile(root.resolve(id.getId().getNamespace()).resolve(id.getId().getPath() + ".json"), model);
        });
    }

    public String toJsonString(TexturedModelData model) {
        try {
            return writeIndented(exporter.export(model), new StringWriter()).toString();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public void writeToFile(Path path, TexturedModelData model) {
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        try (var writer = Files.newBufferedWriter(path)) {
            writeIndented(exporter.export(model), writer).flush();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private <T extends Writer> T writeIndented(JsonElement json, T writer) throws IOException {
        Streams.write(json, new JsWriter(writer));
        return writer;
    }

    class JsWriter extends JsonWriter {
        private static final String INDENT = "  ";
        private static final String NO_INDENT = "";

        private String currentIndent = INDENT;
        private final Stack<String> indentStack = new Stack<>();

        public JsWriter(Writer out) {
            super(out);
            setLenient(true);
            setIndent(currentIndent);
        }

        @Override
        public JsonWriter beginArray() throws IOException {
            super.beginArray();
            return pushIndent(NO_INDENT);
        }

        @Override
        public JsonWriter endArray() throws IOException {
            super.endArray();
            return popIndent();
        }

        @Override
        public JsonWriter beginObject() throws IOException {
            super.beginObject();
            return pushIndent(INDENT);
        }

        @Override
        public JsonWriter endObject() throws IOException {
            super.endObject();
            return popIndent();
        }

        private JsonWriter pushIndent(@Nullable String indent) {
            indentStack.push(currentIndent);
            currentIndent = indent;
            setIndent(indent);
            return this;
        }
        private JsonWriter popIndent() {
            currentIndent = indentStack.pop();
            setIndent(currentIndent);
            return this;
        }
    }

    public interface ModelList {
        Map<EntityModelLayer, TexturedModelData> getModelParts();
    }
}
