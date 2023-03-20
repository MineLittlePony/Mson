package com.minelittlepony.mson.impl.model.json.elements;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.realms.util.JsonUtils;
import net.minecraft.util.Identifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.InstanceCreator;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.exception.FutureAwaitException;
import com.minelittlepony.mson.api.model.PartBuilder;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.api.parser.ModelComponent;
import com.minelittlepony.mson.api.parser.locals.Local;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Contains the common properties for parsing a ModelPart.
 * Actual content (cubes, children) are handled by implementations of this class.
 *
 * @author Sollace
 */
public abstract class AbstractJsonParent implements ModelComponent<ModelPart> {
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
     * The optional texture with parameters inherited from the slot's outer context.
     * This texture is inherited by any children of this compound and their texture
     * definitions are <b>combined</b> with this one's.
     */
    protected final Incomplete<Texture> texture;

    /**
     * The name that this compound is to be exposed as.
     */
    protected final String name;

    public AbstractJsonParent(FileContent<JsonElement> context, String name, JsonObject json) {
        pivot = Local.array(json, "pivot", 3, context.getLocals().getModelId());
        dilate = Local.array(json, "dilate", 3, context.getLocals().getModelId());

        rotate = Local.array(json, "rotate", 3, context.getLocals().getModelId());
        JsonUtil.acceptBooleans(json, "mirror", mirror);
        visible = JsonUtils.getBooleanOr("visible", json, true);
        texture = JsonTexture.incomplete(JsonUtil.accept(json, "texture"));
        this.name = name.isEmpty() ? JsonUtil.accept(json, "name").map(JsonElement::getAsString).orElse("") : name;

        context.addNamedComponent(this.name, this);
    }

    @Override
    public ModelPart export(ModelContext context) {
        return context.computeIfAbsent(name, key -> {
            final PartBuilder builder = new PartBuilder();
            return export(context.bind(builder, Locals::new), builder).build();
        });
    }

    @Override
    public <K> Optional<K> exportToType(ModelContext context, InstanceCreator<K> customType) throws InterruptedException, ExecutionException {
        final PartBuilder builder = new PartBuilder();
        return Optional.of(customType.createInstance(context.bind(builder, Locals::new), ctx -> export(ctx, builder).build()));
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

        return builder;
    }

    protected class Locals implements ModelContext.Locals {
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
        public CompletableFuture<Float> getLocal(String name, float defaultValue) {
            return parent.getLocal(name, defaultValue);
        }

        @Override
        public CompletableFuture<Set<String>> keys() {
            return parent.keys();
        }
    }
}
