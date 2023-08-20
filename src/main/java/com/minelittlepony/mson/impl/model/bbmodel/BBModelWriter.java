package com.minelittlepony.mson.impl.model.bbmodel;

import net.minecraft.client.model.ModelPart;
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
import com.minelittlepony.mson.api.export.ModelFileWriter;
import com.minelittlepony.mson.api.export.ModelSerializer;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.PartBuilder;
import com.minelittlepony.mson.api.model.QuadsBuilder;
import com.minelittlepony.mson.api.model.Rect;
import com.minelittlepony.mson.api.model.QuadsBuilder.QuadBuffer;
import com.minelittlepony.mson.api.model.Vert;
import com.minelittlepony.mson.api.parser.FileContent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

class BBModelWriter extends ModelSerializer<FileContent<?>> implements ModelFileWriter {

    private final JsonBuffer buffer = new JsonBuffer();

    private final List<JsonObject> elements = new ArrayList<>();

    @Nullable
    private Part currentPart;
    private String nextElementName = "";

    @Override
    public JsonElement writeToJsonElement(FileContent<?> content) {
        close();
        return buffer.of(root -> {
            ModelContext context = content.createContext(null, null, content.getLocals().bake());
            buffer.object(root, "meta", buffer.of(meta -> {
                meta.addProperty("format_version", "4.0");
                meta.addProperty("creation_time", System.currentTimeMillis());
                meta.addProperty("model_format", "free");
                meta.addProperty("box_uv", true);
            }));
            root.addProperty("modded_entity_flip_y", true);
            root.addProperty("name", content.getLocals().getModelId().toString());
            buffer.object(root, "resolution", buffer.of(resolution -> {
                resolution.addProperty("width", context.getLocals().getTexture().width());
                resolution.addProperty("height", context.getLocals().getTexture().height());
            }));
            writePart("root", new PartBuilder(), (writer, p) -> {
                writeTree(context, content);
                root.add("outliner", buffer.of(currentPart.children().stream().map(part -> part.toJson(buffer))));
            });
            root.add("elements", buffer.of(elements.stream()));
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
        nextElementName = name;
        element.write(context, this);
        nextElementName = "";
        return this;
    }

    @Override
    public ModelFileWriter writePart(String name, PartBuilder part, BiConsumer<ModelFileWriter, PartBuilder> content) {
        if (name == null || name.isEmpty()) {
            name = nextElementName;
        }
        Part childPart = new Part(UUID.randomUUID(), name, part, new ArrayList<>(), new ArrayList<>(), currentPart);
        if (currentPart != null) {
            currentPart.children().add(childPart);
        }
        currentPart = childPart;
        content.accept(this, part);
        currentPart = Objects.requireNonNull(currentPart, "No part on stack").parent();
        return this;
    }

    @Override
    public ModelFileWriter writeBox(BoxBuilder box) {

        if (box.quads == null) {
            generateStandardCube(box);
        } else {
            generateMesh(box);
        }

        return this;
    }

    private void generateStandardCube(BoxBuilder box) {
        float[] pivot = Objects.requireNonNull(currentPart, "No part on stack").pivot();

        Map<Direction, List<JsonObject>> faces = new EnumMap<>(Direction.class);

        QuadsBuilder.BOX.build(box, new QuadBuffer() {
            @Override
            public boolean getDefaultMirror() {
                return box.mirror[0];
            }

            @Override
            public void quad(float u, float v, float w, float h, Direction direction, boolean mirror, boolean remap, @Nullable Quaternionf rotation, Vert... vertices) {
                faces.computeIfAbsent(direction, d -> new ArrayList<>()).add(buffer.of(face -> {
                    face.add("uv", buffer.of(u - box.u, v - box.v, w - box.u, h - box.v));
                    face.addProperty("texture", 0);
                }));
            }
        });

        faces.values().stream().mapToInt(List::size).max().ifPresent(maxCubes -> {
            for (int i = 0; i < maxCubes; i++) {
                final int ordinal = i;
                var id = UUID.randomUUID();
                currentPart.elements().add(id);

                elements.add(buffer.of(elementJson -> {
                    elementJson.addProperty("name", "Cube_" + id);
                    elementJson.addProperty("type", "cube");
                    elementJson.addProperty("uuid", id.toString());
                    elementJson.addProperty("rescale", false);
                    elementJson.addProperty("locked", false);
                    elementJson.add("from", buffer.of(
                            box.pos[0] + pivot[0] - box.dilate[0],
                           -box.pos[1] - box.size[1] - pivot[1] - box.dilate[1],
                            box.pos[2] + pivot[2] - box.dilate[2]
                    ));
                    elementJson.add("to", buffer.of(
                            box.pos[0] + box.size[0] + pivot[0] + box.dilate[0],
                           -box.pos[1] - pivot[1] + box.dilate[1],
                            box.pos[2] + box.size[2] + pivot[2] + box.dilate[2]
                    ));
                    elementJson.add("uv_offset", buffer.of(box.u, box.v));
                    buffer.object(elementJson, "faces", buffer.of(facesJson -> {
                        BoxBuilder.ALL_DIRECTIONS.forEach(direction -> {
                            facesJson.add(direction.name().toLowerCase(Locale.ROOT), faces.getOrDefault(direction, List.of()).stream()
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

    private void generateMesh(BoxBuilder box) {
        float[] pivot = Objects.requireNonNull(currentPart, "No part on stack").pivot();

        // fix coordinates
        float[] size = { box.size[0], box.size[1], box.size[2] };
        box.pos(
                box.pos[0] + pivot[0],
               -box.pos[1] - size[1] - pivot[1],
                box.pos[2] + pivot[2]
        );

        var id = UUID.randomUUID();
        currentPart.elements().add(id);

        elements.add(buffer.of(elementJson -> {
            elementJson.addProperty("name", "Cube_" + id);
            elementJson.addProperty("type", "mesh");
            elementJson.addProperty("uuid", id.toString());
            elementJson.addProperty("rescale", false);
            elementJson.addProperty("locked", false);
            elementJson.add("uv_offset", buffer.of(box.u, box.v));

            Map<Vert, UUID> verticesCache = new HashMap<>();
            buffer.object(elementJson, "faces", buffer.of(facesJson -> {
                box.quads.build(box, new QuadBuffer() {
                    private final ModelPart.Vertex emptyVertex = new ModelPart.Vertex(0, 0, 0, 0, 0);
                    private final ModelPart.Vertex[] defaultVertices = {emptyVertex, emptyVertex, emptyVertex, emptyVertex};

                    @Override
                    public boolean getDefaultMirror() {
                        return box.mirror[0];
                    }

                    @Override
                    public void quad(float u, float v, float w, float h, Direction direction, boolean mirror, boolean remap, @Nullable Quaternionf rotation, Vert... vertices) {
                        ModelPart.Vertex[] verts = new ModelPart.Vertex[vertices.length];
                        System.arraycopy(vertices, 0, verts, 0, vertices.length);

                        Rect rect = (Rect)new ModelPart.Quad(
                                remap ? verts : defaultVertices,
                                u,         v,
                                u + w, v + h,
                                box.parent.texture.width(), box.parent.texture.height(),
                                mirror,
                                direction);
                        if (!remap) {
                            rect.setVertices(mirror, vertices);
                        }
                        if (rotation != null) {
                            rect.rotate(rotation);
                        }

                        Vert[] finalVertices = new Vert[rect.vertexCount()];
                        for (int i = 0; i < rect.vertexCount(); i++) {
                            finalVertices[i] = rect.getVertex(i);
                        }

                        List<UUID> vertexIds = Arrays.stream(finalVertices).map(vert -> verticesCache.computeIfAbsent(vert, vv -> UUID.randomUUID())).toList();

                        buffer.object(facesJson, UUID.randomUUID().toString(), buffer.of(faceJson -> {
                            buffer.object(faceJson, "uv", buffer.of(uvJson -> {
                                for (int i = 0; i < finalVertices.length; i++) {
                                    uvJson.add(vertexIds.get(i).toString(), buffer.of(
                                            finalVertices[i].getU() * box.parent.texture.width(),
                                            finalVertices[i].getV() * box.parent.texture.height()
                                    ));
                                }
                            }));
                            faceJson.add("vertices", buffer.of(vertexIds.stream().map(UUID::toString).map(JsonPrimitive::new)));
                            faceJson.addProperty("texture", 0);
                        }));
                    }
                });
            }));
            buffer.object(elementJson, "vertices", buffer.of(verticesJson -> {
                verticesCache.forEach((vert, vertId) -> {
                    verticesJson.add(vertId.toString(), buffer.of(vert.getPos().x(), vert.getPos().y(), vert.getPos().z()));
                });
            }));
        }));
    }

    private void rotate(Vert[] verts, @Nullable Quaternionf rotation) {
        if (rotation != null) {
            Arrays.stream(verts).forEach(vert -> vert.rotate(rotation));
        }
    }

    @Override
    public ModelFileWriter writeTree(String name, FileContent<?> content, ModelContext context) {
        return writePart(name, new PartBuilder(), (writer, part) -> writeTree(context, content));
    }

    private final void writeTree(ModelContext context, FileContent<?> content) {
        try {
            for (var name : content.getComponentNames().get()) {
                write(name, context, content.getComponent(name).get());
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
                float[] pivot = pivot();
                elementJson.add("origin", buffer.of(
                        pivot[0],
                       -pivot[1],
                        pivot[2]
                ));
                float[] rotate = rotate();
                elementJson.add("rotation", buffer.of(
                       -rotate[0] / MathHelper.RADIANS_PER_DEGREE,
                       -rotate[1] / MathHelper.RADIANS_PER_DEGREE,
                       -rotate[2] / MathHelper.RADIANS_PER_DEGREE
                ));
                elementJson.addProperty("autouv", 0);
                elementJson.add("children", buffer.of(Streams.concat(
                        elements.stream().map(UUID::toString).map(JsonPrimitive::new),
                        children.stream().map(c -> c.toJson(buffer))
                )));
            });
        }

        float[] pivot() {
            float[] pivot = parent == null ? new float[3] : parent.pivot();
            pivot[0] += part.pivot[0];
            pivot[1] += part.pivot[1];
            pivot[2] += part.pivot[2];
            return pivot;
        }

        float[] rotate() {
            float[] rotation = parent == null ? new float[3] : parent.rotate();
            rotation[0] += part.rotate[0];
            rotation[1] += part.rotate[1];
            rotation[2] += part.rotate[2];
            return rotation;
        }
    }

    @Override
    public void close() {
        elements.clear();
        currentPart = null;
    }
}
