package com.minelittlepony.mson.impl.model.json.elements;

import net.minecraft.resources.Identifier;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.exception.FutureAwaitException;
import com.minelittlepony.mson.api.export.ModelFileWriter;
import com.minelittlepony.mson.api.parser.FileContent;
import com.minelittlepony.mson.impl.MsonImpl;
import com.minelittlepony.mson.api.model.PartBuilder;

/**
 * A specialisation of a compound that allows for adding
 * separate planes to it's list of cubes in a compact manner.
 *
 * @author Sollace
 * @credit killjoy for the suggestion
 *
 * @see com.minelittlepony.mson.impl.model.json.elements.JsonPlanarCube
 * @deprecated Planars are now their own type of cube. See {@link com.minelittlepony.mson.impl.model.json.elements.JsonPlanarCube}
 */
@Deprecated
public class JsonPlanar extends JsonCompound {
    @Deprecated
    public static final Identifier ID = MsonImpl.id("planar");

    private JsonPlanarCube faces;

    @Deprecated
    public JsonPlanar(FileContent<JsonElement> context, String name, JsonElement json) {
        this(context, name, json.getAsJsonObject());
    }

    @Deprecated
    public JsonPlanar(FileContent<JsonElement> context, String name, JsonObject json) {
        super(context, name, json);

        this.faces = new JsonPlanarCube(context, json);
    }

    @Deprecated
    @Override
    protected void export(ModelContext context, PartBuilder builder) throws FutureAwaitException {
        super.export(context, builder);
        if (!faces.faces().isEmpty()) {
            builder.addCube(faces.builder(context));
        }
    }

    @Deprecated
    @Override
    protected void write(ModelContext context, PartBuilder builder, ModelFileWriter writer) {
        super.write(context, builder, writer);
        if (!faces.faces().isEmpty()) {
            writer.writeBox(faces.builder(context));
        }
    }
}
