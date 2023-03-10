package com.minelittlepony.mson.impl.model.bbmodel.elements;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.exception.FutureAwaitException;
import com.minelittlepony.mson.api.model.PartBuilder;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.api.parser.ModelComponent;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

/**
 * Parses an outline component from a blockbench model;
 *
 * {
 *   "name": "left_leg",
 *   "origin": [ -2, 4, -2 ],
 *   "color": 0,
 *   "uuid": "e08ce902-937f-081b-497c-cf15cfa3c064",
 *   "export": true,
 *   "isOpen": true,
 *   "locked": false,
 *   "visibility": true,
 *   "autouv": 0,
 *   "children": [
 *     "069a2cdf-17e3-298d-4f5b-7ecb47b8311d", // uuid of a cube
 *     "2fa542b7-1d48-2ec7-5bfc-e91b0ae1cf5b", // uuid of a cube
 *     {
 *       "name": "head",
 *       "origin": [ 0, 10, -1 ],
 *       "color": 0,
 *       "uuid": "aab8a56b-cb2a-1e2f-9537-d79bb864f7d2",
 *       "export": true,
 *       "isOpen": true,
 *       "locked": false,
 *       "visibility": true,
 *       "autouv": 0,
 *       "children": [
 *         "41e95aa5-f8d4-300e-a3b3-48ab41c2b2d2",
 *         "be698c52-35a0-ac8b-88bd-7627f20c2dde",
 *         "acbc9432-a7da-7286-2576-c6b2d0031a47",
 *         "5df5e652-5bb0-545c-de2c-767249eb31c7",
 *         "7889c267-9eab-935f-e4b0-4aefc26ba273"
 *       ]
 *     } // a child element
 *   ]
 * }
 */
public class BbPart implements ModelComponent<ModelPart> {
    public static final Identifier ID = new Identifier("blockbench", "part");

    private final float[] origin = new float[3];
    private final float[] rotation = new float[3];
    private final boolean visibility;

    private final Map<String, ModelComponent<?>> children = new TreeMap<>();

    public final String name;

    private final List<ModelComponent<?>> cubes = new ArrayList<>();

    public BbPart(FileContent<JsonElement> context, String name, JsonElement json) {
        this(context, name, json.getAsJsonObject());
    }

    public BbPart(FileContent<JsonElement> context, String name, JsonObject json) {
        this.name = JsonHelper.getString(json, "name", name);
        JsonUtil.acceptFloats(json, "origin", origin);
        JsonUtil.acceptFloats(json, "rotation", rotation);
        visibility = JsonHelper.getBoolean(json, "visibility", true);

        JsonUtil.accept(json, "children").map(JsonElement::getAsJsonArray)
            .stream()
            .flatMap(a -> a.asList().stream())
            .forEach(child -> {
               if (child.isJsonObject()) {
                   context.loadComponent(child, ID).ifPresent(component -> {
                       if (((ModelComponent<?>)component) instanceof BbPart part) {
                           children.put(part.name, part);
                       } else {
                           cubes.add(component);
                       }
                   });
               } else {
                   context.loadComponent(child, BbCube.ID).ifPresent(cubes::add);
               }
            });
    }

    public BbPart(Collection<ModelComponent<?>> cubes, String name) {
        this.name = name;
        this.visibility = true;
        this.cubes.addAll(cubes);
    }

    @Override
    public ModelPart export(ModelContext context) throws InterruptedException, ExecutionException {
        return context.computeIfAbsent(name, key -> {
            final PartBuilder builder = new PartBuilder();
            final ModelContext subContext = context.resolve(builder, context.getLocals());
            return export(subContext, builder).build();
        });
    }

    @Override
    public <K> Optional<K> exportToType(ModelContext context, MsonModel.Factory<K> customType) throws InterruptedException, ExecutionException {
        return Optional.of(context.computeIfAbsent(name, key -> {
            final PartBuilder builder = new PartBuilder();
            final ModelContext subContext = context.resolve(builder, context.getLocals());
            return customType.create(export(subContext, builder).build());
        }));
    }

    protected PartBuilder export(ModelContext context, PartBuilder builder) throws FutureAwaitException, InterruptedException, ExecutionException {
        builder
                .hidden(!visibility)
                .pivot(origin)
                .rotate(
                    rotation[0] * MathHelper.RADIANS_PER_DEGREE,
                    rotation[1] * MathHelper.RADIANS_PER_DEGREE,
                    rotation[2] * MathHelper.RADIANS_PER_DEGREE)
                .tex(context.getLocals().getTexture().get());

        children.entrySet().forEach(c -> {
            c.getValue().tryExport(context, ModelPart.class).ifPresent(part -> {
               builder.addChild(c.getKey(), part);
            });
        });
        cubes.forEach(c -> c.tryExport(context, Cuboid.class).ifPresent(builder::addCube));
        return builder;
    }

}
