package com.minelittlepony.mson.impl.model.bbmodel;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.export.JsonBuffer;
import com.minelittlepony.mson.api.export.ModelFileWriter;
import com.minelittlepony.mson.api.export.ModelSerializer;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.PartBuilder;
import com.minelittlepony.mson.api.parser.FileContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

class BBModelWriter extends ModelSerializer<FileContent<?>> implements ModelFileWriter {

    private final JsonBuffer buffer = new JsonBuffer();

    private final List<JsonObject> elements = new ArrayList<>();

    @Nullable
    private Part currentPart;

    @Override
    public JsonElement writeToJsonElement(FileContent<?> content) {
        close();
        return buffer.of(root -> {
            ModelContext context = content.createContext(null, null, content.getLocals().bake());
            buffer.object(root, "meta", buffer.of(meta -> {
                meta.addProperty("format_version", "4.0");
                meta.addProperty("creation_time", System.currentTimeMillis());
                meta.addProperty("model_format", "modded_entity");
                meta.addProperty("box_uv", true);
            }));
            root.addProperty("name", content.getLocals().getModelId().toString());
            buffer.object(root, "resolution", buffer.of(resolution -> {
                resolution.addProperty("width", context.getLocals().getTexture().width());
                resolution.addProperty("height", context.getLocals().getTexture().height());
            }));
            writePart("root", new PartBuilder(), writer -> {
                writeTree(context, content);
                root.add("outliner", buffer.of(currentPart.children().stream().map(part -> part.toJson(buffer))));
            });
            buffer.object(root, "elements", buffer.of(elements.stream()));

            root.add("textures", buffer.of());
        });
    }

    @Override
    public ModelFileWriter writePart(String name, PartBuilder part, Consumer<ModelFileWriter> content) {
        Part childPart = new Part(UUID.randomUUID(), name, part, new ArrayList<>(), new ArrayList<>(), currentPart);
        if (currentPart != null) {
            currentPart.children().add(childPart);
        }
        currentPart = childPart;
        content.accept(this);
        currentPart = Objects.requireNonNull(currentPart, "No part on stack").parent();
        return this;
    }

    @Override
    public ModelFileWriter writeBox(BoxBuilder box) {
        var id = UUID.randomUUID();
        Objects.requireNonNull(currentPart, "No part on stack").elements().add(id);
        elements.add(buffer.of(elementJson -> {
            elementJson.addProperty("name", id.toString());
            elementJson.addProperty("uuid", id.toString());
            elementJson.addProperty("rescale", false);
            elementJson.addProperty("locked", false);
            buffer.array(elementJson, "origin", 0, 0, 0);
            buffer.array(elementJson, "from", box.pos);
            buffer.array(elementJson, "to",
                    box.size[0] - box.pos[0],
                    box.size[1] - box.pos[1],
                    box.size[2] - box.pos[2]
            );
            buffer.array(elementJson, "uv_offset", box.u, box.v);
        }));
        return this;
    }

    @Override
    public ModelFileWriter writeTree(String name, FileContent<?> content, ModelContext context) {
        return writePart(name, new PartBuilder(), writer -> writeTree(context, content));
    }

    private final void writeTree(ModelContext context, FileContent<?> content) {
        try {
            for (var name : content.getComponentNames().get()) {
                content.getComponent(name).get().get().write(context, this);
            }
        } catch (Exception e) {
            throw new JsonParseException(e);
        }
    }

    record Part(UUID id, String name, PartBuilder part, List<UUID> elements, List<Part> children, @Nullable Part parent) implements JsonBuffer.JsonConvertable {
        @Override
        public JsonObject toJson(JsonBuffer buffer) {
            return buffer.of(elementJson -> {
                elementJson.addProperty("name", name);
                elementJson.addProperty("color", 0);
                elementJson.addProperty("uuid", id.toString());
                elementJson.addProperty("export", true);
                elementJson.addProperty("isOpen", false);
                elementJson.addProperty("locked", false);
                elementJson.addProperty("visibility", !part.hidden);
                elementJson.addProperty("autouv", 0);
                elementJson.add("children", buffer.of(Streams.concat(
                        elements.stream().map(UUID::toString).map(JsonPrimitive::new),
                        children.stream().map(c -> c.toJson(buffer))
                )));
            });
        }
    }

    @Override
    public void close() {
        elements.clear();
        currentPart = null;
    }
}
