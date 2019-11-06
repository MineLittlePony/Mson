package com.minelittlepony.mson.impl.model;

import net.minecraft.util.Identifier;

import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.impl.key.ReflectedModelKey;
import com.minelittlepony.mson.util.JsonUtil;

import javax.annotation.Nullable;

import java.util.concurrent.CompletableFuture;

public class JsonSlot<T extends MsonModel> implements JsonComponent<T> {

    public static final Identifier ID = new Identifier("mson", "slot");

    private final ModelKey<T> implementation;

    private final CompletableFuture<JsonContext> content;

    @Nullable
    private String name;

    public JsonSlot(JsonContext context, JsonObject json) {
        implementation = ReflectedModelKey.fromJson(json);
        content = context.resolve(json.get("content"));
        name = JsonUtil.require(json, "name").getAsString();
        context.addNamedComponent(name, this);
    }

    @Override
    public T export(ModelContext context) {
        return context.computeIfAbsent(name, key -> {
            T inst = implementation.createModel();

            inst.init(content.get().createContext(context.getModel()));

            return inst;
        });
    }
}
