package com.minelittlepony.mson.impl.model.json.elements;

import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.exception.FutureAwaitException;
import com.minelittlepony.mson.api.export.ModelFileWriter;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.Face.Axis;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.api.parser.locals.Local;
import com.minelittlepony.mson.impl.MsonImpl;
import com.minelittlepony.mson.impl.model.FixtureImpl;
import com.minelittlepony.mson.api.model.Face;
import com.minelittlepony.mson.api.model.PartBuilder;
import com.minelittlepony.mson.api.model.QuadsBuilder;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * A specialisation of a compound that allows for adding
 * separate planes to it's list of cubes in a compact manner.
 *
 * @author Sollace
 * @credit killjoy for the suggestion
 */
public class JsonPlanar extends JsonCompound {
    public static final Identifier ID = MsonImpl.id("planar");

    private final Map<Face, JsonFaceSet> faces = new EnumMap<>(Face.class);

    public JsonPlanar(FileContent<JsonElement> context, String name, JsonElement json) {
        this(context, name, json.getAsJsonObject());
    }

    public JsonPlanar(FileContent<JsonElement> context, String name, JsonObject json) {
        super(context, name, json);

        Face.VALUES.forEach(face -> {
            JsonUtil.accept(json, face.name().toLowerCase())
                .map(JsonElement::getAsJsonArray)
                .ifPresent(el -> faces.put(face, new JsonFaceSet(context, el, face)));
        });
    }

    @Override
    protected void export(ModelContext context, PartBuilder builder) throws FutureAwaitException {
        super.export(context, builder);
        faces.values()
            .stream()
            .flatMap(face -> face.export(context))
            .forEach(builder::addCube);
    }

    @Override
    protected void write(ModelContext context, PartBuilder builder, ModelFileWriter writer) {
        super.write(context, builder, writer);
        faces.values()
            .stream()
            .flatMap(face -> face.export(context))
            .forEach(writer::writeBox);
    }

    record JsonFaceSet(Face face, List<JsonFace> elements) {
        public JsonFaceSet(FileContent<JsonElement> context, JsonArray json, Face face) {
            this(face, new ArrayList<>());

            if (json.get(0).isJsonArray()) {
                for (int i = 0; i < json.size(); i++) {
                    elements.add(new JsonFace(face, context, json.get(i).getAsJsonArray()));
                }
            } else {
                elements.add(new JsonFace(face, context, json));
            }
        }

        Stream<BoxBuilder> export(ModelContext subContext) {
            Fixtures fixtures = new Fixtures(subContext);
            return elements.stream().map(face -> face.builder(subContext, fixtures));
        }

        class Fixtures extends FixtureImpl {
            private final Map<Axis, List<Vec3>> lockedVectors = new HashMap<>();

            Fixtures(ModelContext context) throws FutureAwaitException {
                for (Axis axis : Axis.values()) {
                    if (axis != face.getAxis()) {
                        for (JsonFace i : elements) {
                            face.getVertices(i.position.complete(context), i.size.complete(context), axis, 0.5F).forEach(vertex -> {

                                List<Vec3> locked = getLockedVectors(axis);

                                if (locked.contains(vertex.normal())) {
                                    return;
                                }

                                for (JsonFace f : elements) {
                                    if (f != i && face.isInside(f.position.complete(context), f.size.complete(context), vertex.stretched())) {
                                        locked.add(vertex.normal());
                                        break;
                                    }
                                }
                            });
                        }
                    }
                }
            }

            List<Vec3> getLockedVectors(Axis axis) {
                return lockedVectors.computeIfAbsent(axis, _ -> new ArrayList<>());
            }

            @Override
            protected boolean isFixed(Axis axis, float x, float y, float z) {
                return getLockedVectors(axis).contains(new Vec3(x, y, z));
            }
        }

        record JsonFace (
                Face face,
                Incomplete<float[]> position,
                Incomplete<float[]> size,
                Incomplete<Texture> texture,
                boolean[] mirror
        ) {

            public JsonFace(Face face, FileContent<JsonElement> context, JsonArray json) {
                this(face,
                    Local.array(json.get(0).getAsJsonPrimitive(), json.get(1).getAsJsonPrimitive(), json.get(2).getAsJsonPrimitive()),
                    Local.array(json.get(3).getAsJsonPrimitive(), json.get(4).getAsJsonPrimitive()),
                    json.size() > 6 ? createTexture(
                            Local.ref(json.get(5).getAsJsonPrimitive()),
                            Local.ref(json.get(6).getAsJsonPrimitive())
                        ) : JsonTexture::fromParent,
                    json.size() > 8 ? new boolean[] {
                            json.get(7).getAsBoolean(),
                            json.get(8).getAsBoolean()
                        } : new boolean[2]
                );
            }

            public BoxBuilder builder(ModelContext context, Fixtures fixtures) {
                return new BoxBuilder(context)
                    .fix(fixtures)
                    .tex(texture.complete(context))
                    .mirror(face.getAxis(), mirror)
                    .pos(position.complete(context))
                    .size(face.getAxis(), size.complete(context))
                    .quads(QuadsBuilder.plane(face));
            }

            private static Incomplete<Texture> createTexture(Incomplete<Float> u, Incomplete<Float> v) {
                return locals -> {
                    Texture parent = locals.getTexture();
                    return new Texture(
                            u.complete(locals).intValue(),
                            v.complete(locals).intValue(),
                            parent.width(),
                            parent.height()
                    );
                };
            }
        }
    }
}
