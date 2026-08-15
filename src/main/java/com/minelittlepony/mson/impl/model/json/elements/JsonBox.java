package com.minelittlepony.mson.impl.model.json.elements;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.model.BoxBuilder;
import com.minelittlepony.mson.api.model.Face;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.api.model.Face.Axis;
import com.minelittlepony.mson.api.model.QuadsBuilder;
import com.minelittlepony.mson.api.parser.locals.Local;
import com.minelittlepony.mson.impl.MsonImpl;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.api.parser.ModelBoxComponent;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a simple 3D cube.
 *
 * @author Sollace
 */
public class JsonBox implements ModelBoxComponent {
    public static final Identifier ID = MsonImpl.id("box");

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

    /**
     * The optional set of faces to keep visible. Default is "all" when unspecified.
     */
    protected final Optional<Set<Face>> faces;

    public JsonBox(FileContent<JsonElement> context, String name, JsonElement json) {
        this(context, name, json.getAsJsonObject());
    }

    public JsonBox(FileContent<JsonElement> context, String name, JsonObject json) {
        from = Local.array(json, "from", 3, context.locals().modelId());
        size = Local.array(json, "size", 3, context.locals().modelId());
        texture = JsonTexture.incomplete(JsonUtil.accept(json, "texture"));
        mirror = JsonUtil.acceptBoolean(json, "mirror");
        dilate = Local.array(json, "dilate", 3, context.locals().modelId());
        faces = JsonUtil.acceptSet(json.get("faces"), Face.JSON_FUNC, Face.NONE);
    }

    protected Set<Direction> enabledSides() {
        return faces.map(i -> i.stream().map(face -> face.getNormal()).collect(Collectors.toUnmodifiableSet())).orElse(BoxBuilder.ALL_DIRECTIONS);
    }

    protected QuadsBuilder quads(ModelContext context) {
        return QuadsBuilder.cube(enabledSides());
    }

    @Override
    public BoxBuilder builder(ModelContext context) {
        return new BoxBuilder(context)
            .tex(texture.complete(context))
            .pos(from.complete(context))
            .size(size.complete(context))
            .dilate(dilate.complete(context))
            .mirror(Axis.X, mirror)
            .quads(quads(context));
    }
}
