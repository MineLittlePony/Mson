package com.minelittlepony.mson.impl.model;

import net.minecraft.util.Identifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.impl.ModelLocalsImpl;
import com.minelittlepony.mson.impl.MsonImpl;
import com.minelittlepony.mson.impl.JsonLocalsImpl;
import com.minelittlepony.mson.impl.Local;
import com.minelittlepony.mson.impl.key.ReflectedModelKey;
import com.minelittlepony.mson.util.JsonUtil;

import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class JsonSlot<T> implements JsonComponent<T> {
    public static final Identifier ID = new Identifier("mson", "slot");

    private final ReflectedModelKey<T> implementation;

    private final CompletableFuture<JsonContext> data;

    private final Map<String, Incomplete<Float>> locals;

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
        if (json.has("content")) {
            MsonImpl.LOGGER.warn("Model {} is using a slot with the `content` property. This is deprecated and will be removed in 1.18. Use `data` instead", context.getLocals().getModelId());
            data = context.resolve(json.get("content"));
        } else {
            data = context.resolve(json.get("data"));
        }
        this.name = name.isEmpty() ? JsonUtil.require(json, "name").getAsString() : name;
        texture = JsonUtil.accept(json, "texture").map(JsonTexture::create);
        context.addNamedComponent(this.name, this);

        locals = JsonUtil.accept(json, "locals")
                .map(JsonElement::getAsJsonObject)
                .map(JsonObject::entrySet)
                .orElseGet(() -> new HashSet<>())
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Local.create(e.getValue())));
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
            if (locals.containsKey(name)) {
                return CompletableFuture.completedFuture(locals.get(name));
            }
            return parent.getLocal(name);
        }

        @Override
        public CompletableFuture<Set<String>> keys() {
            return parent.keys().thenApplyAsync(output -> {
               output.addAll(locals.keySet());
               return output;
            });
        }
    }
}
