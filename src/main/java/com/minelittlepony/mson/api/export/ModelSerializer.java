package com.minelittlepony.mson.api.export;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Stack;

public abstract class ModelSerializer<T> implements AutoCloseable {

    public abstract JsonElement writeToJsonElement(T content);

    public String writeToString(T content) {
        try {
            return writeIndented(writeToJsonElement(content), new StringWriter()).toString();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public void writeToFile(Path path, T content) throws IOException {
        Files.createDirectories(path.getParent());

        try (var w = Files.newBufferedWriter(path)) {
            writeIndented(writeToJsonElement(content), w).flush();
        }
    }

    private <W extends Writer> W writeIndented(JsonElement json, W writer) throws IOException {
        Streams.write(json, new JsWriter(writer));
        return writer;
    }

    private static class JsWriter extends JsonWriter {
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
}
