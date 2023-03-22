package com.minelittlepony.mson.impl.model.json.elements;

import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.exception.FutureAwaitException;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.Face.Axis;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.api.parser.locals.Local;
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
    public static final Identifier ID = new Identifier("mson", "planar");

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
    protected PartBuilder export(ModelContext context, PartBuilder builder) throws FutureAwaitException {
        super.export(context, builder);
        faces.values()
            .stream()
            .flatMap(face -> face.export(context))
            .forEach(builder::addCube);

        return builder;
    }

    class JsonFaceSet {

        private final Face face;

        private final List<JsonFace> elements = new ArrayList<>();

        public JsonFaceSet(FileContent<JsonElement> context, JsonArray json, Face face) {
            this.face = face;

            if (json.get(0).isJsonArray()) {
                for (int i = 0; i < json.size(); i++) {
                    elements.add(new JsonFace(context, json.get(i).getAsJsonArray()));
                }
            } else {
                elements.add(new JsonFace(context, json));
            }
        }

        Stream<Cuboid> export(ModelContext subContext) throws FutureAwaitException {
            Fixtures fixtures = new Fixtures(subContext);
            return elements.stream().map(face -> face.export(subContext, fixtures));
        }

        class Fixtures extends FixtureImpl {
            private final Map<Axis, List<Vec3d>> lockedVectors = new HashMap<>();

            Fixtures(ModelContext context) throws FutureAwaitException {
                for (Axis axis : Axis.values()) {
                    if (axis != face.getAxis()) {
                        for (JsonFace i : elements) {
                            face.getVertices(i.position.complete(context), i.size.complete(context), axis, 0.5F).forEach(vertex -> {

                                List<Vec3d> locked = getLockedVectors(axis);

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

            List<Vec3d> getLockedVectors(Axis axis) {
                return lockedVectors.computeIfAbsent(axis, a -> new ArrayList<>());
            }

            @Override
            protected boolean isFixed(Axis axis, float x, float y, float z) {
                return getLockedVectors(axis).contains(new Vec3d(x, y, z));
            }
        }

        class JsonFace {

            final Incomplete<float[]> position;
            final Incomplete<float[]> size;

            private final Incomplete<Texture> texture;

            private final boolean[] mirror;

            public JsonFace(FileContent<JsonElement> context, JsonArray json) {
                position = Local.array(
                        json.get(0).getAsJsonPrimitive(),
                        json.get(1).getAsJsonPrimitive(),
                        json.get(2).getAsJsonPrimitive()
                );
                size = Local.array(
                        json.get(3).getAsJsonPrimitive(),
                        json.get(4).getAsJsonPrimitive()
                );

                if (json.size() > 6) {
                    texture = createTexture(
                        Local.ref(json.get(5).getAsJsonPrimitive()),
                        Local.ref(json.get(6).getAsJsonPrimitive())
                    );
                } else {
                    texture = JsonTexture::fromParent;
                }

                if (json.size() > 8) {
                    mirror = new boolean[] {
                        json.get(7).getAsBoolean(),
                        json.get(8).getAsBoolean()
                    };
                } else {
                    mirror = new boolean[2];
                }
            }

            public Cuboid export(ModelContext context, Fixtures fixtures) throws FutureAwaitException {
                return new BoxBuilder(context)
                    .fix(fixtures)
                    .tex(texture.complete(context))
                    .mirror(face.getAxis(), mirror)
                    .pos(position.complete(context))
                    .size(face.getAxis(), size.complete(context))
                    .build(QuadsBuilder.plane(face));
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
