package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.BoxBuilder.ContentAccessor;
import com.minelittlepony.mson.api.model.Face.Axis;
import com.minelittlepony.mson.api.model.Face;
import com.minelittlepony.mson.api.model.QuadsBuilder;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.impl.FixtureImpl;
import com.minelittlepony.mson.impl.exception.FutureAwaitException;
import com.minelittlepony.mson.util.Incomplete;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class JsonPlanar extends JsonCuboid {
    public static final Identifier ID = new Identifier("mson", "planar");

    private final Map<Face, JsonFaceSet> faces = new EnumMap<>(Face.class);

    private final float[] stretch = new float[3];

    public JsonPlanar(JsonContext context, JsonObject json) {
        super(context, json);
        JsonUtil.getFloats(json, "stretch", stretch);

        Face.VALUES.forEach(face -> {
            JsonUtil.accept(json, face.name().toLowerCase())
                .map(JsonElement::getAsJsonArray)
                .ifPresent(el -> faces.put(face, new JsonFaceSet(context, el, face)));
        });
    }

    @Override
    public void export(ModelContext context, ModelPart cuboid) throws InterruptedException, ExecutionException {
        super.export(context , cuboid);

        ModelContext subContext = context.resolve(cuboid);
        faces.values().forEach(face -> {
            face.export(subContext, ((ContentAccessor)cuboid).cubes());
        });
    }

    class JsonFaceSet extends FixtureImpl {

        private final Face face;

        private final List<JsonFace> elements = new ArrayList<>();

        private final Map<Axis, List<Vec3d>> lockedVectors = new HashMap<>();

        public JsonFaceSet(JsonContext context, JsonArray json, Face face) {
            this.face = face;

            if (json.get(0).isJsonArray()) {
                for (int i = 0; i < json.size(); i++) {
                    elements.add(new JsonFace(context, json.get(i).getAsJsonArray()));
                }
            } else {
                elements.add(new JsonFace(context, json));
            }

            for (Axis axis : Axis.values()) {
                if (axis != face.getAxis()) {
                    for (JsonFace i : elements) {
                        face.getVertices(i.position, i.size, axis, 0.5F).forEach(vertex -> {

                            List<Vec3d> locked = getLockedVectors(axis);

                            if (locked.contains(vertex.normal)) {
                                return;
                            }

                            for (JsonFace f : elements) {
                                if (f != i && face.isInside(f.position, f.size, vertex.stretched)) {
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

        void export(ModelContext subContext, List<Cuboid> cubes) {
            cubes.addAll(elements.stream().map(face -> {
                try {
                    return face.export(subContext);
                } catch (InterruptedException | ExecutionException e) {
                    throw new FutureAwaitException(e);
                }
            }).collect(Collectors.toList()));
        }

        @Override
        protected boolean isFixed(Axis axis, float x, float y, float z) {
            return getLockedVectors(axis).contains(new Vec3d(x, y, z));
        }

        class JsonFace implements JsonComponent<Cuboid> {

            final float[] position = new float[3];
            final int[] size = new int[2];

            private final Incomplete<Optional<Texture>> texture;

            public JsonFace(JsonContext context, JsonArray json) {
                position[0] =  json.get(0).getAsFloat();
                position[1] =  json.get(1).getAsFloat();
                position[2] =  json.get(2).getAsFloat();
                size[0] = (int)json.get(3).getAsFloat();
                size[1] = (int)json.get(4).getAsFloat();

                if (json.size() > 6) {
                    texture = createTexture(
                            context.getVarLookup().getFloat(json.get(5).getAsJsonPrimitive()),
                            context.getVarLookup().getFloat(json.get(6).getAsJsonPrimitive())
                    );
                } else {
                    texture = Incomplete.completed(Optional.empty());
                }
            }

            private Incomplete<Optional<Texture>> createTexture(Incomplete<Float> u, Incomplete<Float> v) {
                return locals -> {
                    Texture parent = locals.getTexture().get();

                    return Optional.of(new JsonTexture(
                            u.complete(locals).intValue(),
                            v.complete(locals).intValue(),
                            parent.getWidth(),
                            parent.getHeight()
                    ));
                };
            }

            @Override
            public Cuboid export(ModelContext context) throws InterruptedException, ExecutionException {
                return new BoxBuilder(context)
                    .stretch(stretch)
                    .fix(JsonFaceSet.this)
                    .tex(texture.complete(context))
                    .pos(position)
                    .size(face.getAxis(), size)
                    .build(QuadsBuilder.plane(face));
            }

        }
    }
}
