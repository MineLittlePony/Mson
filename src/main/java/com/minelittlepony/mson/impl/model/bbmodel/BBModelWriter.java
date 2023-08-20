package com.minelittlepony.mson.impl.model.bbmodel;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.export.JsonBuffer;
import com.minelittlepony.mson.api.export.JsonBuffer.JsonConvertable;
import com.minelittlepony.mson.api.export.ModelFileWriter;
import com.minelittlepony.mson.api.export.ModelSerializer;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.PartBuilder;
import com.minelittlepony.mson.api.model.QuadsBuilder;
import com.minelittlepony.mson.api.model.QuadsBuilder.QuadBuffer;
import com.minelittlepony.mson.api.model.Vert;
import com.minelittlepony.mson.api.parser.FileContent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

class BBModelWriter extends ModelSerializer<FileContent<?>> implements ModelFileWriter {

    private final List<JsonConvertable> elements = new ArrayList<>();

    private PartStack stack = new PartStack();

    @Override
    public JsonElement writeToJsonElement(FileContent<?> content) {
        close();
        JsonBuffer buffer = JsonBuffer.INSTANCE;
        return buffer.of(root -> {
            buffer.object(root, "meta", buffer.of(meta -> {
                meta.addProperty("format_version", "4.0");
                meta.addProperty("creation_time", System.currentTimeMillis());
                meta.addProperty("model_format", "free");
                meta.addProperty("box_uv", true);
            }));
            root.addProperty("modded_entity_flip_y", true);
            root.addProperty("name", content.getLocals().getModelId().toString());
            ModelContext context = content.createContext(null, null, content.getLocals().bake());
            buffer.object(root, "resolution", buffer.of(resolution -> {
                resolution.addProperty("width", context.getLocals().getTexture().width());
                resolution.addProperty("height", context.getLocals().getTexture().height());
            }));
            writePart("root", new PartBuilder(), (writer, p) -> {
                writeTree(context, content);
                root.add("outliner", buffer.of(stack.part().children().stream().map(part -> part.toJson(buffer))));
            });
            root.add("elements", buffer.of(elements));
            root.add("textures", buffer.of());
        });
    }


    @Override
    public ModelFileWriter write(ModelContext context, Writeable element) {
        element.write(context, this);
        return this;
    }

    @Override
    public ModelFileWriter write(String name, ModelContext context, Writeable element) {
        if (stack.shouldWrite(element)) {
            stack.pushName(name);
            element.write(context, this);
            stack.popName();
        }
        return this;
    }

    @Override
    public ModelFileWriter writePart(String name, PartBuilder part, BiConsumer<ModelFileWriter, PartBuilder> content) {
        stack.pushPart(name, part);
        content.accept(this, part);
        stack.popPart();
        return this;
    }

    @Override
    public ModelFileWriter writeBox(BoxBuilder box) {

        if (box.quads.getId() == QuadsBuilder.CUBE) {
            generateStandardCube(box.quads.getId(), box);
        //} else if (box.quads.getId() == QuadsBuilder.PLANE) {
        //    generatePlane(box.quads.getId(), box);
        } else {
            generateMesh(box.quads.getId(), box);
        }

        return this;
    }

    private void generateStandardCube(Identifier type, BoxBuilder box) {
        PartStack.Part part = stack.part();

        Map<Direction, List<JsonConvertable>> faces = new EnumMap<>(Direction.class);

        float[] emptyDilation = new float[box.dilate.length];
        boolean isAxisDilated = isUsingPerAxisDilation(box.dilate);

        float[] dilate = isAxisDilated ? box.dilate : emptyDilation;
        float inflate = isAxisDilated ? 0 : box.dilate[0];
        box.dilate = emptyDilation;
        boolean[] mirroring = {false};

        QuadsBuilder.BOX.build(box, new QuadBuffer() {
            @Override
            public boolean getDefaultMirror() {
                return box.mirror[0];
            }

            @Override
            public void quad(float u, float v, float w, float h, Direction direction, boolean mirror, boolean remap, @Nullable Quaternionf rotation, Vert... vertices) {
                mirroring[0] |= mirror;
                faces.computeIfAbsent(direction, d -> new ArrayList<>()).add(buffer -> buffer.of(face -> {
                    face.add("uv", buffer.of(u - box.u, v - box.v, w - box.u, h - box.v));
                    face.addProperty("texture", 0);
                }));
            }
        });

        faces.values().stream().mapToInt(List::size).max().ifPresent(maxCubes -> {
            for (int i = 0; i < maxCubes; i++) {
                final int ordinal = i;
                var id = UUID.randomUUID();
                part.elements().add(id);

                elements.add(buffer -> buffer.of(elementJson -> {
                    elementJson.addProperty("name", part.isRedundant() ? part.name() : type.getPath());
                    elementJson.addProperty("type", "cube");
                    elementJson.addProperty("uuid", id.toString());
                    elementJson.addProperty("rescale", false);
                    elementJson.addProperty("locked", false);
                    elementJson.addProperty("inflate", inflate);
                    elementJson.addProperty("mirror_uv", mirroring[0]);
                    elementJson.addProperty("visibility", !part.hidden());
                    if (part.isRedundant()) {
                        part.writeTransform(elementJson, buffer);
                    }

                    float[] pivot = part.pivot();
                    elementJson.add("from", buffer.of(
                            box.pos[0] + pivot[0] - dilate[0],
                           -box.pos[1] - box.size[1] - pivot[1] - dilate[1],
                            box.pos[2] + pivot[2] - dilate[2]
                    ));
                    elementJson.add("to", buffer.of(
                            box.pos[0] + box.size[0] + pivot[0] + dilate[0],
                           -box.pos[1] - pivot[1] + dilate[1],
                            box.pos[2] + box.size[2] + pivot[2] + dilate[2]
                    ));
                    elementJson.add("uv_offset", buffer.of(box.u, box.v));
                    buffer.object(elementJson, "faces", buffer.of(facesJson -> {
                        BoxBuilder.ALL_DIRECTIONS.forEach(direction -> {
                            facesJson.add(direction.name().toLowerCase(Locale.ROOT), faces.getOrDefault(direction, List.of())
                                    .stream()
                                    .map(face -> face.toJson(buffer))
                                .skip(ordinal)
                                .findFirst()
                                .orElseGet(() -> buffer.of(face -> {
                                    buffer.array(face, "uv", 0, 0, 0, 0);
                                    face.addProperty("texture", 0);
                                })));
                        });
                    }));
                }));
            }
        });
    }

    private boolean isUsingPerAxisDilation(float[] dilate) {
        for (int i = 0; i < dilate.length; i++) {
            if (dilate[i] != dilate[0]) {
                return true;
            }
        }
        return false;
    }

    private void generateMesh(Identifier type, BoxBuilder box) {
        PartStack.Part part = stack.part();
        float[] pivot = part.pivot();

        // fix coordinates
        float[] size = { box.size[0], box.size[1], box.size[2] };
        box.pos(
                box.pos[0] + pivot[0],
               -box.pos[1] - size[1] - pivot[1],
                box.pos[2] + pivot[2]
        );

        var id = UUID.randomUUID();
        part.meshes().add(id);

        elements.add(buffer -> buffer.of(elementJson -> {

            List<BoxBuilder.Quad> quads = box.collectQuads();

            List<Direction> directions = quads.stream().map(BoxBuilder.Quad::direction)
                    .distinct().toList();

            elementJson.addProperty("name", part.isRedundant() ? part.name() : (directions.size() == 1 ? directions.get(0).asString() + "_" : "") + type.getPath());
            elementJson.addProperty("type", "mesh");
            elementJson.addProperty("uuid", id.toString());
            elementJson.addProperty("rescale", false);
            elementJson.addProperty("locked", false);
            elementJson.addProperty("visibility", !part.hidden());
            elementJson.add("uv_offset", buffer.of(box.u, box.v));

            Map<Vert, UUID> verticesCache = quads.stream()
                .flatMap(quad -> Arrays.stream(quad.rect().getVertices()))
                .collect(Collectors.toMap(Function.identity(), vv -> UUID.randomUUID()));

            buffer.object(elementJson, "faces", buffer.of(facesJson -> {
                buffer.object(facesJson, UUID.randomUUID().toString(), buffer.of(faceJson -> {
                    buffer.object(faceJson, "uv", buffer.of(uvJson -> {
                        verticesCache.forEach((vert, vertId) -> {
                            uvJson.add(vertId.toString(), buffer.of(
                                    vert.getU() * box.parent.texture.width(),
                                    vert.getV() * box.parent.texture.height()
                            ));
                        });
                    }));
                    faceJson.add("vertices", buffer.of(verticesCache.values().stream().map(UUID::toString).map(JsonPrimitive::new)));
                    faceJson.addProperty("texture", 0);
                }));
            }));
            buffer.object(elementJson, "vertices", buffer.of(verticesJson -> {
                verticesCache.forEach((vert, vertId) -> {
                    if (part.isRedundant()) {
                        verticesJson.add(vertId.toString(), buffer.of(
                                vert.getPos().x() + part.part().pivot[0],
                                vert.getPos().y() + part.part().pivot[1],
                                vert.getPos().z() + part.part().pivot[2]
                        ));
                    } else {
                        verticesJson.add(vertId.toString(), buffer.of(vert.getPos().x(), vert.getPos().y(), vert.getPos().z()));
                    }
                });
            }));
        }));
    }

    @Override
    public ModelFileWriter writeTree(String name, FileContent<?> content, ModelContext context) {
        return writePart(name, new PartBuilder(), (writer, part) -> writeTree(context, content));
    }

    private final void writeTree(ModelContext context, FileContent<?> content) {
        try {
            stack.pushFile(content);
            for (var name : content.getComponentNames().get()) {
                write(name, context, content.getComponent(name).get());
            }
        } catch (Exception e) {
            throw new JsonParseException(e);
        } finally {
            stack.popFile();
        }
    }

    @Override
    public void close() {
        stack = new PartStack();
        elements.clear();
    }

    record PartStack (Stack<ContentRoot> currentFile, Stack<Part> currentPart, Stack<String> currentName) {
        public PartStack() {
            this(new Stack<>(), new Stack<>(), new Stack<>());
        }

        public void pushName(String name) {
            currentName.push(name);
        }

        public void popName() {
            currentName.pop();
        }

        public void pushFile(FileContent<?> content) {
            currentFile.push(new ContentRoot(content, new Stack<>(), new HashSet<>()));
        }

        public void popFile() {
            currentFile.pop();
        }

        public void pushPart(String name, PartBuilder part) {
            if (name == null || name.isEmpty()) {
                name = name();
            }
            Part childPart = new Part(UUID.randomUUID(), name, part, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), currentPart.empty() ? null : part());
            if (childPart.parent() != null) {
                childPart.parent().children().add(childPart);
            }

            currentPart.push(childPart);
            if (!currentFile.empty()) {
                currentFile.peek().localPart().push(childPart);
            }
        }

        public void popPart() {
            currentPart.pop();
            if (!currentFile.empty()) {
                currentFile.peek().localPart().pop();
            }
        }

        public Part part() {
            return currentPart.peek();
        }

        public FileContent<?> file() {
            return currentFile.peek().content();
        }

        public String name() {
            return currentName.empty() ? "" : currentName.peek();
        }

        public boolean shouldWrite(Writeable element) {
            return currentFile.empty() || currentFile.peek().shouldWrite(element);
        }

        record ContentRoot(FileContent<?> content, Stack<Part> localPart, Set<Writeable> writtenElements) {
            public boolean isAtRoot() {
                return localPart.size() < 2;
            }

            public boolean shouldWrite(Writeable element) {
                return writtenElements.add(element) || !isAtRoot();
            }
        }

        record Part(UUID id, String name, PartBuilder part, List<UUID> elements, List<UUID> meshes, List<Part> children, @Nullable Part parent) implements JsonBuffer.JsonConvertable {
            @Override
            public JsonElement toJson(JsonBuffer buffer) {
                if (isRedundant()) {
                    return new JsonPrimitive(elements.get(0).toString());
                }
                return buffer.of(elementJson -> {
                    elementJson.addProperty("name", name);
                    elementJson.addProperty("color", 0);
                    elementJson.addProperty("uuid", id.toString());
                    elementJson.addProperty("export", true);
                    elementJson.addProperty("isOpen", false);
                    elementJson.addProperty("locked", false);
                    elementJson.addProperty("visibility", !hidden());
                    writeTransform(elementJson, buffer);
                    elementJson.addProperty("autouv", 0);
                    elementJson.add("children", buffer.of(Streams.concat(
                            elements.stream().map(UUID::toString).map(JsonPrimitive::new),
                            meshes.stream().map(UUID::toString).map(JsonPrimitive::new),
                            children.stream().map(c -> c.toJson(buffer))
                    )));
                });
            }

            void writeTransform(JsonObject elementJson, JsonBuffer buffer) {
                float[] pivot = pivot();
                elementJson.add("origin", buffer.of(
                        pivot[0],
                       -pivot[1],
                        pivot[2]
                ));
                float[] rotate = rotate();
                elementJson.add("rotation", buffer.of(
                       -rotate[0] / MathHelper.RADIANS_PER_DEGREE,
                        rotate[1] / MathHelper.RADIANS_PER_DEGREE,
                       -rotate[2] / MathHelper.RADIANS_PER_DEGREE
                ));
            }

            boolean isRedundant() {
                return elements.size() == 1 && children.isEmpty() && meshes.isEmpty();
            }

            boolean hidden() {
                return part.hidden || (parent != null && parent.hidden());
            }

            float[] pivot() {
                float[] pivot = parent == null ? new float[3] : parent.pivot();
                pivot[0] += part.pivot[0];
                pivot[1] += part.pivot[1];
                pivot[2] += part.pivot[2];
                return pivot;
            }

            float[] rotate() {
                float[] rotation = new float[3];//parent == null ? new float[3] : parent.rotate();
                rotation[0] += part.rotate[0];
                rotation[1] += part.rotate[1];
                rotation[2] += part.rotate[2];
                return rotation;
            }
        }
    }
}
