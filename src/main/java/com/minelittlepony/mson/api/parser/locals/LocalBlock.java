package com.minelittlepony.mson.api.parser.locals;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.Incomplete;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class LocalBlock {
    private final Map<String, Incomplete<Float>> locals;

    LocalBlock(Map<String, Incomplete<Float>> locals) {
        this.locals = locals;
    }

    public Map<String, Incomplete<Float>> entries() {
        return locals;
    }

    public Set<String> appendKeys(Set<String> output) {
        output.addAll(locals.keySet());
        return output;
    }

    public Optional<CompletableFuture<Incomplete<Float>>> get(String name) {
        if (locals.containsKey(name)) {
            return Optional.of(CompletableFuture.completedFuture(locals.get(name)));
        }
        return Optional.empty();
    }

    public static LocalBlock of(Optional<JsonElement> json) {
        return new LocalBlock(json
                .map(JsonElement::getAsJsonObject)
                .map(JsonObject::entrySet)
                .orElseGet(() -> new HashSet<>())
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Local.create(e.getValue()))));
    }
}