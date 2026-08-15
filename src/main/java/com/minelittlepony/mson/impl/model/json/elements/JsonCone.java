package com.minelittlepony.mson.impl.model.json.elements;

import net.minecraft.resources.Identifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.api.parser.locals.Local;
import com.minelittlepony.mson.impl.MsonImpl;
import com.minelittlepony.mson.api.model.QuadsBuilder;

/**
 * Specialisation of a cube with a tapered end.
 *
 * @author Sollace
 */
public class JsonCone extends JsonBox {
    public static final Identifier ID = MsonImpl.id("cone");

    /**
     * The amount by which the box must taper.
     * A value of 0 will produce the same result as a normal cube.
     */
    private final Incomplete<Float> taper;

    public JsonCone(FileContent<JsonElement> context, String name, JsonElement json) {
        this(context, name, json.getAsJsonObject());
    }

    public JsonCone(FileContent<JsonElement> context, String name, JsonObject json) {
        super(context, name, json);
        taper = Local.ref(json, "taper", context.locals().modelId());
    }

    @Override
    protected QuadsBuilder quads(ModelContext context) {
        return QuadsBuilder.cone(taper.complete(context), enabledSides());
    }
}
