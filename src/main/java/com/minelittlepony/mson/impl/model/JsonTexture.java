package com.minelittlepony.mson.impl.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.util.Incomplete;
import com.minelittlepony.mson.util.JsonUtil;
import com.mojang.realmsclient.util.JsonUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class JsonTexture implements Texture {

    public static final Texture EMPTY = new JsonTexture(0, 0, 64, 32);

    private int[] parameters;

    public static Incomplete<Texture> localized(JsonObject json) {
        return JsonUtil.accept(json, "texture")
            .map(JsonTexture::unresolvedMerged)
            .orElse(JsonTexture::unresolvedInherited);
    }

    private static Texture unresolvedInherited(ModelContext.Locals locals) throws InterruptedException, ExecutionException {
        return locals.getTexture().get();
    }

    private static Incomplete<Texture> unresolvedMerged(JsonElement el) {
        if (el.isJsonArray()) {
            return Incomplete.completed(new JsonTexture(el.getAsJsonArray()));
        }
        return (Incomplete<Texture>)(locals -> resolve(el, locals.getTexture()).get());
    }

    public static CompletableFuture<Texture> resolve(JsonElement json, CompletableFuture<Texture> inherited) {
        if (json.isJsonArray()) {
            return CompletableFuture.completedFuture(new JsonTexture(json.getAsJsonArray()));
        }

        return inherited.thenApplyAsync(t -> new JsonTexture(json.getAsJsonObject(), t));
    }

    private JsonTexture(JsonArray arr) {
        JsonUtil.getAsInts(arr.getAsJsonArray(), parameters);
    }

    private JsonTexture(JsonObject tex, Texture inherited) {
        this(inherited.getParameters());
        parameters = new int[] {
                JsonUtils.getIntOr("u", tex, getU()),
                JsonUtils.getIntOr("v", tex, getV()),
                JsonUtils.getIntOr("w", tex, getWidth()),
                JsonUtils.getIntOr("h", tex, getHeight())
        };
    }

    private JsonTexture(int... params) {
        parameters = params;
    }

    @Override
    public int[] getParameters() {
        return parameters;
    }

    @Override
    public int getU() {
        return parameters[0];
    }

    @Override
    public int getV() {
        return parameters[1];
    }

    @Override
    public int getWidth() {
        return parameters[2];
    }

    @Override
    public int getHeight() {
        return parameters[3];
    }
}
