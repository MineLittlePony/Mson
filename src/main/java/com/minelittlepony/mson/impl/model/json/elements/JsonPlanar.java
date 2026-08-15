package com.minelittlepony.mson.impl.model.json.elements;

import net.minecraft.resources.Identifier;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.exception.FutureAwaitException;
import com.minelittlepony.mson.api.export.ModelFileWriter;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.impl.MsonImpl;
import com.minelittlepony.mson.api.model.Face;
import com.minelittlepony.mson.api.model.PartBuilder;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.EnumMap;
import java.util.Map;

/**
 * A specialisation of a compound that allows for adding
 * separate planes to it's list of cubes in a compact manner.
 *
 * @author Sollace
 * @credit killjoy for the suggestion
 */
public class JsonPlanar extends JsonCompound {
    public static final Identifier ID = MsonImpl.id("planar");

    private final Map<Face, JsonPlanarCube.JsonFaceSet> faces = new EnumMap<>(Face.class);

    public JsonPlanar(FileContent<JsonElement> context, String name, JsonElement json) {
        this(context, name, json.getAsJsonObject());
    }

    public JsonPlanar(FileContent<JsonElement> context, String name, JsonObject json) {
        super(context, name, json);

        Face.VALUES.forEach(face -> {
            JsonUtil.accept(json, face.name().toLowerCase())
                .map(JsonElement::getAsJsonArray)
                .ifPresent(el -> faces.put(face, new JsonPlanarCube.JsonFaceSet(context, el, face)));
        });
    }

    @Override
    protected void export(ModelContext context, PartBuilder builder) throws FutureAwaitException {
        super.export(context, builder);
        faces.values()
            .stream()
            .flatMap(face -> face.exportMulti(context))
            .forEach(builder::addCube);
    }

    @Override
    protected void write(ModelContext context, PartBuilder builder, ModelFileWriter writer) {
        super.write(context, builder, writer);
        faces.values()
            .stream()
            .flatMap(face -> face.exportMulti(context))
            .forEach(writer::writeBox);
    }
}
