package com.minelittlepony.mson.impl.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.exception.FutureAwaitException;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.util.Incomplete;
import com.minelittlepony.mson.util.JsonUtil;
import net.minecraft.client.realms.util.JsonUtils;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class JsonTexture {
    /**
     * Returns an incomplete texture, ready to be resolved against the given context using the supplied json.
     *
     * When resolved the parameters of the json object will
     * be applied on top of the inherited texture parameters.
     */
    public static Incomplete<Texture> localized(Optional<JsonElement> json) {
        return json
            .map(JsonTexture::unresolvedMerged)
            .orElse(JsonTexture::unresolvedInherited);
    }

    private static Incomplete<Texture> unresolvedMerged(JsonElement el) {
        if (el.isJsonArray()) {
            return Incomplete.completed(of(el.getAsJsonArray()));
        }
        return locals -> {
            try {
                return resolve(el, locals.getTexture()).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new FutureAwaitException(e);
            }
        };
    }

    private static Texture unresolvedInherited(ModelContext.Locals locals) throws FutureAwaitException {
        try {
            return locals.getTexture().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new FutureAwaitException(e);
        }
    }

    public static Texture create(JsonElement json) {
        if (json.isJsonArray()) {
            return of(json.getAsJsonArray());
        }
        return of(json.getAsJsonObject(), Texture.EMPTY);
    }

    /**
     * Returns a texture resolved against the current inheritance hierarchy.
     *
     * Elements in the passed json object will
     * be applied on top of the inherited texture parameters.
     *
     * Unlike {@link localized(json)} this texture is only inherited to the current point
     * in the model's hierarchyand may be different from the final texture made available
     * at model construction time.
     *
     * It's recommended to always use the {@link Incomplete<>}
     * variants resolved against a {@link ModelContext} for this reason.
     *
     */
    public static CompletableFuture<Texture> unlocalized(Optional<JsonElement> json, CompletableFuture<Texture> inherited) {
        return json
            .map(el -> resolve(el, inherited))
            .orElse(inherited);
    }

    private static CompletableFuture<Texture> resolve(JsonElement json, CompletableFuture<Texture> inherited) {
        if (json.isJsonArray()) {
            return CompletableFuture.completedFuture(of(json.getAsJsonArray()));
        }

        return inherited.thenApplyAsync(t -> of(json.getAsJsonObject(), t));
    }

    private static Texture of(JsonArray arr) {
        int[] parameters = new int[4];
        JsonUtil.getAsInts(arr.getAsJsonArray(), parameters);
        return new Texture(parameters[0], parameters[1], parameters[2], parameters[2]);
    }

    private static Texture of(JsonObject tex, Texture inherited) {
        return new Texture(
            JsonUtils.getIntOr("u", tex, inherited.u()),
            JsonUtils.getIntOr("v", tex, inherited.v()),
            JsonUtils.getIntOr("w", tex, inherited.width()),
            JsonUtils.getIntOr("h", tex, inherited.height())
        );
    }

}
