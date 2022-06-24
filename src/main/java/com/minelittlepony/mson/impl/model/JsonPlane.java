package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.util.Identifier;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.Face;
import com.minelittlepony.mson.api.model.QuadsBuilder;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.concurrent.ExecutionException;

/**
 * A single-face alternative to mson:box
 *
 * @author Sollace
 */
public class JsonPlane implements JsonComponent<Cuboid> {

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

    public JsonPlane(JsonContext context, String name, JsonObject json) {
        position = context.getLocals().get(json, "position", 3);
        size = context.getLocals().get(json, "size", 2);
        texture = JsonTexture.incomplete(JsonUtil.accept(json, "texture"));
        JsonUtil.acceptBooleans(json, "mirror", mirror);
        dilate = context.getLocals().get(json, "dilate", 3);
        face = Face.valueOf(JsonUtil.require(json, "face", " required by mson:plane component in " + context.getLocals().getModelId()).getAsString().toUpperCase());
    }

    @Override
    public Cuboid export(ModelContext context) throws InterruptedException, ExecutionException {
        return new BoxBuilder(context)
            .tex(texture.complete(context))
            .mirror(face.getAxis(), mirror)
            .pos(position.complete(context))
            .size(face.getAxis(), size.complete(context))
            .dilate(dilate.complete(context))
            .build(QuadsBuilder.plane(face));
    }
}
