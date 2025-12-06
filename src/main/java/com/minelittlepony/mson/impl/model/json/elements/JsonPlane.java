package com.minelittlepony.mson.impl.model.json.elements;

import net.minecraft.resources.Identifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.Face;
import com.minelittlepony.mson.api.model.QuadsBuilder;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.api.parser.locals.Local;
import com.minelittlepony.mson.impl.MsonImpl;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.api.parser.ModelBoxComponent;
import com.minelittlepony.mson.util.JsonUtil;

/**
 * A single-face alternative to mson:box
 *
 * @author Sollace
 */
public record JsonPlane (
        /**
         * The 3D position where this plane will appear.
         */
        Incomplete<float[]> position,
        /**
         * The 2D dimensions of the plane.
         */
        Incomplete<float[]> size,

        /**
         * The 2D dilation of the place along the two major axis
         */
        Incomplete<float[]> dilate,

        /**
         * The texturing to be applied to the plane.
         * If defined, will combine this values with what was inherited,
         * otherwise only the inherited texture is used.
         */
        Incomplete<Texture> texture,
        /**
         * The 2D mirroring of this plane's texture along the two major axis.
         */
        boolean[] mirror,
        /**
         * The orientation of this plane.
         * Can be either of the 6 faces of a cube, and will be used to inform which axis this
         * plane runs perpendicular to in 3D space.
         */
        Face face
    ) implements ModelBoxComponent {
    public static final Identifier ID = MsonImpl.id("plane");

    public JsonPlane(FileContent<JsonElement> context, String name, JsonElement json) {
        this(context, name, json.getAsJsonObject());
    }

    public JsonPlane(FileContent<JsonElement> context, String name, JsonObject json) {
        this(
            Local.array(json, "position", 3, context.locals().modelId()),
            Local.array(json, "size", 2, context.locals().modelId()),
            Local.array(json, "dilate", 3, context.locals().modelId()),
            JsonTexture.incomplete(JsonUtil.accept(json, "texture")),
            JsonUtil.acceptBooleans(json, "mirror", 2),
            Face.valueOf(JsonUtil.require(json, "face", ID, context.locals().modelId()).getAsString().toUpperCase())
        );
    }

    @Override
    public BoxBuilder builder(ModelContext context) {
        return new BoxBuilder(context)
            .tex(texture.complete(context))
            .mirror(face.getAxis(), mirror)
            .pos(position.complete(context))
            .size(face.getAxis(), size.complete(context))
            .dilate(dilate.complete(context))
            .quads(QuadsBuilder.plane(face));
    }
}
