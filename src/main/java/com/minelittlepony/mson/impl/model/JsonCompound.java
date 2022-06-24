package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.client.realms.util.JsonUtils;
import net.minecraft.util.Identifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.exception.FutureAwaitException;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.PartBuilder;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a transformable node in a model.
 * Compound may contain multiple cubes underneath it as well as other child parts.
 *
 * @author Sollace
 */
public class JsonCompound implements JsonComponent<ModelPart> {
    public static final Identifier ID = new Identifier("mson", "compound");
    private static final float RADS_DEGS_FACTOR = (float)Math.PI / 180F;

    /**
     * The 3D center of rotation of this part.
     */
    private final Incomplete<float[]> pivot;

    /**
     * The 3D dilation of this part along each of the primary axis.
     * These values are inherited by all of this part's children who's own dilation is then taken as an offset of this one.
     */
    protected final Incomplete<float[]> dilate;

    /**
     * The 3D rotation of this part in degrees.
     */
    private final Incomplete<float[]> rotate;

    /**
     * The 3D mirroring of this part's textures along each of the major axis.
     * This value is inherited by  all of this part's children that do not define their own mirroring.
     */
    private final boolean[] mirror = new boolean[3];

    /**
     * The visibility of this part.
     */
    private final boolean visible;

    /**
     * The child components of this part.
     * Can be any component that produces a ModelPart.
     * The default type for a child component is mson:compound (same as the parent).
     */
    private final Map<String, JsonComponent<?>> children = new TreeMap<>();

    /**
     * The cubes that form the visible structure of this part.
     * Can be any component that produces a Cuboid.
     * The default type for cubes is mson:box.
     *
     * @see JsonBox#ID
     */
    private final List<JsonComponent<?>> cubes = new ArrayList<>();

    /**
     * The optional texture with parameters inherited from the slot's outer context.
     * This texture is inherited by any children of this compound and their texture
     * definitions are <b>combined</b> with this one's.
     */
    protected final Incomplete<Texture> texture;

    /**
     * The name that this compound is to be exposed as.
     */
    private final String name;

    public JsonCompound(JsonContext context, String name, JsonObject json) {
        pivot = context.getLocals().get(json, "pivot", 3);
        dilate = context.getLocals().get(json, "dilate", 3);

        rotate = context.getLocals().get(json, "rotate", 3);
        JsonUtil.acceptBooleans(json, "mirror", mirror);
        visible = JsonUtils.getBooleanOr("visible", json, true);
        texture = JsonTexture.incomplete(JsonUtil.accept(json, "texture"));
        this.name = name.isEmpty() ? JsonUtil.accept(json, "name").map(JsonElement::getAsString).orElse("") : name;

        JsonUtil.accept(json, "children").ifPresent(el -> {
            children.putAll(parseChildren(context, el).collect(Collectors.toMap(
                Map.Entry::getKey,
                i -> context.loadComponent(i.getValue(), ID).orElse(null))
            ));
        });
        JsonUtil.accept(json, "cubes").ifPresent(el -> {
            el.getAsJsonArray().forEach(element -> {
                context.loadComponent(element, JsonBox.ID).ifPresent(cubes::add);
            });
        });

        context.addNamedComponent(this.name, this);
    }

    private Stream<Map.Entry<String, JsonElement>> parseChildren(JsonContext context, JsonElement json) {
        if (json.isJsonObject()) {
            return json.getAsJsonObject().entrySet().stream();
        }
        return Stream.empty();
    }

    @Override
    public ModelPart export(ModelContext context) {
        return context.computeIfAbsent(name, key -> {
            final PartBuilder builder = new PartBuilder();
            final ModelContext subContext = context.resolve(builder, new Locals(context.getLocals()));
            return export(subContext, builder).build();
        });
    }

    protected PartBuilder export(ModelContext context, PartBuilder builder) throws FutureAwaitException {
        float[] rotate = this.rotate.complete(context);
        builder
                .hidden(!visible)
                .pivot(this.pivot.complete(context))
                .mirror(mirror)
                .rotate(
                    rotate[0] * RADS_DEGS_FACTOR,
                    rotate[1] * RADS_DEGS_FACTOR,
                    rotate[2] * RADS_DEGS_FACTOR)
                .tex(texture.complete(context));

        children.entrySet().forEach(c -> {
            c.getValue().tryExport(context, ModelPart.class).ifPresent(part -> {
               builder.addChild(c.getKey(), part);
            });
        });
        cubes.forEach(c -> c.tryExport(context, Cuboid.class).ifPresent(builder::addCube));
        return builder;
    }

    class Locals implements ModelContext.Locals {
        private final ModelContext.Locals parent;

        Locals(ModelContext.Locals parent) {
            this.parent = parent;
        }

        @Override
        public Identifier getModelId() {
            return parent.getModelId();
        }

        @Override
        public CompletableFuture<float[]> getDilation() {
            return parent.getDilation().thenApply(inherited -> {
                float[] dilation = dilate.complete(parent);
                return new float[] {
                    inherited[0] + dilation[0],
                    inherited[1] + dilation[1],
                    inherited[2] + dilation[2]
                };
            });
        }

        @Override
        public CompletableFuture<Texture> getTexture() {
            return CompletableFuture.completedFuture(texture.complete(parent));
        }

        @Override
        public CompletableFuture<Float> getLocal(String name) {
            return parent.getLocal(name);
        }

        @Override
        public CompletableFuture<Set<String>> keys() {
            return parent.keys();
        }
    }
}
