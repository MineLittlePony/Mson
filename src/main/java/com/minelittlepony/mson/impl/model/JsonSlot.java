package com.minelittlepony.mson.impl.model;

import net.minecraft.util.Identifier;

import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.impl.ModelLocalsImpl;
import com.minelittlepony.mson.impl.JsonLocalsImpl;
import com.minelittlepony.mson.impl.Local;
import com.minelittlepony.mson.impl.key.ReflectedModelKey;
import com.minelittlepony.mson.util.JsonUtil;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class JsonSlot<T> implements JsonComponent<T> {
    public static final Identifier ID = new Identifier("mson", "slot");

    private final ReflectedModelKey<T> implementation;

    private final CompletableFuture<JsonContext> data;

    private final Local.Block locals;

    private final Optional<Texture> texture;

    @Nullable
    private String name;

    @Override
    public <K> Optional<K> tryExportTreeNodes(ModelContext context, Class<K> type) {
        if (!implementation.isCompatible(type)) {
            return Optional.empty();
        }
        return tryExport(context, type);
    }

    public JsonSlot(JsonContext context, String name, JsonObject json) {
        implementation = ReflectedModelKey.fromJson(json);
        data = context.resolve(json.get("data"));
        this.name = name.isEmpty() ? JsonUtil.require(json, "name", " required by mson:slot component in " + context.getLocals().getModelId()).getAsString() : name;
        texture = JsonUtil.accept(json, "texture").map(JsonTexture::of);
        context.addNamedComponent(this.name, this);

        locals = Local.of(JsonUtil.accept(json, "locals"));
    }

    @Override
    public T export(ModelContext context) {
        return context.computeIfAbsent(name, key -> {
            JsonContext jsContext = data.get();
            ModelContext subContext = jsContext.createContext(context.getModel(), new ModelLocalsImpl(implementation.getId(), new Locals(jsContext)));

            T inst = implementation.createModel(subContext);
            if (inst instanceof MsonModel) {
                ((MsonModel)inst).init(subContext.resolve(context.getContext()));
            }

            return inst;
        });
    }

    private class Locals implements JsonLocalsImpl {
        private final JsonContext.Locals parent;

        Locals(JsonContext parent) {
            this.parent = parent.getLocals();
        }

        @Override
        public Identifier getModelId() {
            return parent.getModelId();
        }

        @Override
        public CompletableFuture<float[]> getDilation() {
            return parent.getDilation();
        }

        @Override
        public CompletableFuture<Texture> getTexture() {
            return texture
                    .map(CompletableFuture::completedFuture)
                    .orElseGet(parent::getTexture);
        }

        @Override
        public CompletableFuture<Incomplete<Float>> getLocal(String name) {
            return locals.get(name).orElseGet(() -> parent.getLocal(name));
        }

        @Override
        public CompletableFuture<Set<String>> keys() {
            return parent.keys().thenApplyAsync(locals::appendKeys);
        }
    }
}
