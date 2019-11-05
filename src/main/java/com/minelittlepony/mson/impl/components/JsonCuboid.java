package com.minelittlepony.mson.impl.components;

import net.minecraft.client.model.Cuboid;
import net.minecraft.util.Identifier;

import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;

public class JsonCuboid implements JsonComponent<Cuboid> {
    public static final Identifier ID = new Identifier("mson", "compound");

    public JsonCuboid(JsonContext context, JsonObject json) {

    }

    @Override
    public Cuboid export() {
        // TODO Auto-generated method stub
        return null;
    }
}
