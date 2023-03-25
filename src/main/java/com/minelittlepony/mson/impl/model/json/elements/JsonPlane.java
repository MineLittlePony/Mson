package com.minelittlepony.mson.impl.model.json.elements;

import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.util.Identifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.export.ModelFileWriter;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.Face;
import com.minelittlepony.mson.api.model.QuadsBuilder;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.api.parser.ModelComponent;
import com.minelittlepony.mson.api.parser.locals.Local;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.util.JsonUtil;

/**
 * A single-face alternative to mson:box
 *
 * @author Sollace
 */
public class JsonPlane implements ModelComponent<Cuboid> {

    public static final Identifier ID = new Identifier("mson", "plane");

    /**
     * The 3D position where this plane will appear.
     */
    private final Incomplete<float[]> position;

    /**
     * The 2D dimensions of the plane.
     */
    private final Incomplete<float[]> size;

    /**
     * The 2D dilation of the place along the two major axis
     */
    private final Incomplete<float[]> dilate;

    /**
     * The texturing to be applied to the plane.
     * If defined, will combine this values with what was inherited,
     * otherwise only the inherited texture is used.
     */
    private final Incomplete<Texture> texture;

    /**
     * The 2D mirroring of this plane's texture along the two major axis.
     */
    private final boolean[] mirror = new boolean[2];

    /**
     * The orientation of this plane.
     * Can be either of the 6 faces of a cube, and will be used to inform which axis this
     * plane runs perpendicular to in 3D space.
     */
    private final Face face;

    public JsonPlane(FileContent<JsonElement> context, String name, JsonElement json) {
        this(context, name, json.getAsJsonObject());
    }

    public JsonPlane(FileContent<JsonElement> context, String name, JsonObject json) {
        position = Local.array(json, "position", 3, context.getLocals().getModelId());
        size = Local.array(json, "size", 2, context.getLocals().getModelId());
        texture = JsonTexture.incomplete(JsonUtil.accept(json, "texture"));
        JsonUtil.acceptBooleans(json, "mirror", mirror);
        dilate = Local.array(json, "dilate", 3, context.getLocals().getModelId());
        face = Face.valueOf(JsonUtil.require(json, "face", ID, context.getLocals().getModelId()).getAsString().toUpperCase());
    }

    @Override
    public Cuboid export(ModelContext context) {
        return new BoxBuilder(context)
            .tex(texture.complete(context))
            .mirror(face.getAxis(), mirror)
            .pos(position.complete(context))
            .size(face.getAxis(), size.complete(context))
            .dilate(dilate.complete(context))
            .quads(QuadsBuilder.plane(face))
            .build();
    }

    @Override
    public void write(ModelContext context, ModelFileWriter writer) {
        writer.writeBox(new BoxBuilder(context)
            .tex(texture.complete(context))
            .mirror(face.getAxis(), mirror)
            .pos(position.complete(context))
            .size(face.getAxis(), size.complete(context))
            .dilate(dilate.complete(context))
            .quads(QuadsBuilder.plane(face)));
    }
}
