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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

public class BbPart implements ModelComponent<ModelPart> {
    public static final Identifier ID = new Identifier("blockbench", "part");

    private final float[] origin = new float[3];
    private final float[] rotation = new float[3];
    private final boolean visibility;

    private final Map<String, ModelComponent<?>> children = new TreeMap<>();

    private final String name;

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

        context.addNamedComponent(this.name, this);
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
