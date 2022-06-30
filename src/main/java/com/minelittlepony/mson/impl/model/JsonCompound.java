package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.util.Identifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.exception.FutureAwaitException;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.PartBuilder;
import com.minelittlepony.mson.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a transformable node in a model.
 * Compound may contain multiple cubes underneath it as well as other child parts.
 *
 * @author Sollace
 */
public class JsonCompound extends AbstractJsonParent {
    public static final Identifier ID = new Identifier("mson", "compound");

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

    public JsonCompound(JsonContext context, String name, JsonObject json) {
        super(context, name, json);
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
    }

    private Stream<Map.Entry<String, JsonElement>> parseChildren(JsonContext context, JsonElement json) {
        if (json.isJsonObject()) {
            return json.getAsJsonObject().entrySet().stream();
        }
        return Stream.empty();
    }

    @Override
    protected PartBuilder export(ModelContext context, PartBuilder builder) throws FutureAwaitException {
        super.export(context, builder);

        children.entrySet().forEach(c -> {
            c.getValue().tryExport(context, ModelPart.class).ifPresent(part -> {
               builder.addChild(c.getKey(), part);
            });
        });
        cubes.forEach(c -> c.tryExport(context, Cuboid.class).ifPresent(builder::addCube));
        return builder;
    }
}
