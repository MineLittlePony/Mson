package com.minelittlepony.mson.impl.model.json.elements;

import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.util.Identifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.export.ModelFileWriter;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.api.model.Face.Axis;
import com.minelittlepony.mson.api.parser.ModelComponent;
import com.minelittlepony.mson.api.parser.locals.Local;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.Optional;

/**
 * Represents a simple 3D cube.
 *
 * @author Sollace
 */
public class JsonBox implements ModelComponent<Cuboid> {
    public static final Identifier ID = new Identifier("mson", "box");

    /**
     * The 3D coordinate of where the box should begin.
     */
    protected final Incomplete<float[]> from;

    /**
     * The 3D size of the box.
     */
    protected final Incomplete<float[]> size;

    /**
     * The 3D dilation of the box along all of the major axis.
     * If not defined, will use what is inherited from the parent context.
     */
    protected final Incomplete<float[]> dilate;

    /**
     * The optional texture mirroring of the box along the X axis (mojang behaviour).
     * If not defined, will use what is inherited from the parent context.
     */
    protected final Optional<Boolean> mirror;

    /**
     * The Optional texture overrides.
     * If not defined, will use what is inherited from the parent context.
     */
    protected final Incomplete<Texture> texture;

    public JsonBox(FileContent<JsonElement> context, String name, JsonElement json) {
        this(context, name, json.getAsJsonObject());
    }

    public JsonBox(FileContent<JsonElement> context, String name, JsonObject json) {
        from = Local.array(json, "from", 3, context.getLocals().getModelId());
        size = Local.array(json, "size", 3, context.getLocals().getModelId());
        texture = JsonTexture.incomplete(JsonUtil.accept(json, "texture"));
        mirror = JsonUtil.acceptBoolean(json, "mirror");
        dilate = Local.array(json, "dilate", 3, context.getLocals().getModelId());
    }

    @Override
    public Cuboid export(ModelContext context) {
        return new BoxBuilder(context)
            .tex(texture.complete(context))
            .pos(from.complete(context))
            .size(size.complete(context))
            .dilate(dilate.complete(context))
            .mirror(Axis.X, mirror)
            .build();
    }

    @Override
    public void write(ModelContext context, ModelFileWriter writer) {
        writer.writeBox(new BoxBuilder(context)
                .tex(texture.complete(context))
                .pos(from.complete(context))
                .size(size.complete(context))
                .dilate(dilate.complete(context))
                .mirror(Axis.X, mirror));
    }
}
