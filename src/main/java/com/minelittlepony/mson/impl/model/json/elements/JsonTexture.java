package com.minelittlepony.mson.impl.model.json.elements;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.model.Texture;
import net.minecraft.client.realms.util.JsonUtils;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class JsonTexture {
    /**
     * Returns an incomplete texture, ready to be resolved against a model context using the supplied json.
     *
     * When resolved the parameters of the json object will
     * be applied on top of the inherited texture parameters.
     */
    public static Incomplete<Texture> incomplete(Optional<JsonElement> json) {
        return json
            .map(JsonTexture::merged)
            .orElse(JsonTexture::fromParent);
    }

    private static Incomplete<Texture> merged(JsonElement el) {
        return locals -> resolved(el, locals.getTexture());
    }

    public static Texture fromParent(ModelContext.Locals locals) {
        return locals.getTexture();
    }

    /**
     * Returns a texture resolved against the current inheritance hierarchy.
     *
     * Elements in the passed json object will
     * be applied on top of the inherited texture parameters.
     *
     * Unlike {@link localized(json)} this texture is only inherited to the current point
     * in the model's hierarchy and may be different from the final texture made available
     * at model construction time.
     *
     * It's recommended to always use the {@link Incomplete<>}
     * variants resolved against a {@link ModelContext} for this reason.
     *
     */
    public static CompletableFuture<Texture> unlocalized(Optional<JsonElement> json, CompletableFuture<Texture> inherited) {
        return inherited.thenApply(i -> json.map(el -> resolved(el, i)).orElse(i));
    }

    private static Texture resolved(JsonElement json, Texture inherited) {
        return of(json, inherited);
    }

    public static Texture of(JsonElement json) {
        return of(json, Texture.EMPTY);
    }

    private static Texture of(JsonElement json, Texture inherited) {
        JsonObject tex = json.getAsJsonObject();
        return new Texture(
            JsonUtils.getIntOr("u", tex, inherited.u()),
            JsonUtils.getIntOr("v", tex, inherited.v()),
            JsonUtils.getIntOr("w", tex, inherited.width()),
            JsonUtils.getIntOr("h", tex, inherited.height())
        );
    }
}
