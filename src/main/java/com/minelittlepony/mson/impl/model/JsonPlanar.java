package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.exception.FutureAwaitException;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.Face.Axis;
import com.minelittlepony.mson.api.model.Face;
import com.minelittlepony.mson.api.model.PartBuilder;
import com.minelittlepony.mson.api.model.QuadsBuilder;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.impl.FixtureImpl;
import com.minelittlepony.mson.impl.MsonImpl;
import com.minelittlepony.mson.util.Incomplete;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class JsonPlanar extends JsonCuboid {
    public static final Identifier ID = new Identifier("mson", "planar");

    private final Map<Face, JsonFaceSet> faces = new EnumMap<>(Face.class);

    protected final Incomplete<float[]> dilate;

    public JsonPlanar(JsonContext context, String name, JsonObject json) {
        super(context, name, json);
        if (json.has("stretch")) {
            MsonImpl.LOGGER.warn("Model {} is using the `stretch` property. This is deprecated and will be removed in 1.18. Please use `dilate`.", context.getLocals().getModelId());
            dilate = context.getLocals().get(json, "stretch", 3);
        } else {
            dilate = context.getLocals().get(json, "dilate", 3);
        }

        Face.VALUES.forEach(face -> {
            JsonUtil.accept(json, face.name().toLowerCase())
                .map(JsonElement::getAsJsonArray)
                .ifPresent(el -> faces.put(face, new JsonFaceSet(context, el, face)));
        });
    }

    @Override
    protected PartBuilder export(ModelContext context, PartBuilder builder) throws FutureAwaitException {
        super.export(context, builder);
        ModelContext subContext = context.resolve(builder);
        faces.values().forEach(face -> {
            face.export(subContext);
        });

        return builder;
    }

    class JsonFaceSet {

        private final Face face;

        private final List<JsonFace> elements = new ArrayList<>();

        public JsonFaceSet(JsonContext context, JsonArray json, Face face) {
            this.face = face;

            if (json.get(0).isJsonArray()) {
                for (int i = 0; i < json.size(); i++) {
                    elements.add(new JsonFace(context, json.get(i).getAsJsonArray()));
                }
            } else {
                elements.add(new JsonFace(context, json));
            }
        }

        void export(ModelContext subContext) throws FutureAwaitException {
            Fixtures fixtures = new Fixtures(subContext);
            elements.stream().forEach(face -> {
                face.export(subContext, fixtures);
            });
        }

        class Fixtures extends FixtureImpl {
            private final Map<Axis, List<Vec3d>> lockedVectors = new HashMap<>();

            Fixtures(ModelContext context) throws FutureAwaitException {
                for (Axis axis : Axis.values()) {
                    if (axis != face.getAxis()) {
                        for (JsonFace i : elements) {
                            face.getVertices(i.position.complete(context), i.size.complete(context), axis, 0.5F).forEach(vertex -> {

                                List<Vec3d> locked = getLockedVectors(axis);

                                if (locked.contains(vertex.normal)) {
                                    return;
                                }

                                for (JsonFace f : elements) {
                                    if (f != i && face.isInside(f.position.complete(context), f.size.complete(context), vertex.stretched)) {
                                        locked.add(vertex.normal);
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

            private final Incomplete<Optional<Texture>> texture;

            public JsonFace(JsonContext context, JsonArray json) {
                position = context.getLocals().get(
                        json.get(0).getAsJsonPrimitive(),
                        json.get(1).getAsJsonPrimitive(),
                        json.get(2).getAsJsonPrimitive()
                );
                size = context.getLocals().get(
                        json.get(3).getAsJsonPrimitive(),
                        json.get(4).getAsJsonPrimitive()
                );

                if (json.size() > 6) {
                    texture = createTexture(
                            context.getLocals().get(json.get(5).getAsJsonPrimitive()),
                            context.getLocals().get(json.get(6).getAsJsonPrimitive())
                    );
                } else {
                    texture = Incomplete.completed(Optional.empty());
                }
            }

            private Incomplete<Optional<Texture>> createTexture(Incomplete<Float> u, Incomplete<Float> v) {
                return locals -> {
                    try {
                        Texture parent = locals.getTexture().get();
                        return Optional.of(new Texture(
                                u.complete(locals).intValue(),
                                v.complete(locals).intValue(),
                                parent.width(),
                                parent.height()
                        ));
                    } catch (InterruptedException | ExecutionException e) {
                        throw new FutureAwaitException(e);
                    }
                };
            }

            public Cuboid export(ModelContext context, Fixtures fixtures) throws FutureAwaitException {
                return new BoxBuilder(context)
                    .dilate(dilate.complete(context))
                    .fix(fixtures)
                    .tex(texture.complete(context))
                    .pos(position.complete(context))
                    .size(face.getAxis(), size.complete(context))
                    .build(QuadsBuilder.plane(face));
            }

        }
    }
}
