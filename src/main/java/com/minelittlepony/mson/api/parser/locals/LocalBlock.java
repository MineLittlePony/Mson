package com.minelittlepony.mson.api.parser.locals;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.Incomplete;
import com.minelittlepony.mson.api.ModelContext;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public interface LocalBlock {

    Set<String> appendKeys(Set<String> output);

    Optional<CompletableFuture<Incomplete<Float>>> get(String name);

    public static LocalBlock of(Optional<JsonElement> json) {
        return new Impl(json
                .map(JsonElement::getAsJsonObject)
                .map(JsonObject::entrySet)
                .orElseGet(() -> new HashSet<>())
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Local.create(e.getValue()))));
    }

    default LocalBlock bind(ModelContext.Locals locals) {
        LocalBlock self = this;
        return new LocalBlock() {
            @Override
            public Set<String> appendKeys(Set<String> output) {
                return self.appendKeys(output);
            }

            @Override
            public Optional<CompletableFuture<Incomplete<Float>>> get(String name) {
                return self.get(name).map(local -> local.thenApply(incomplete -> {
                    return ctx -> incomplete.complete(locals);
                }));
            }
        };
    }

    class Impl implements LocalBlock {
        private final Map<String, Incomplete<Float>> locals;

        Impl(Map<String, Incomplete<Float>> locals) {
            this.locals = locals;
        }

        @Override
        public Set<String> appendKeys(Set<String> output) {
            output.addAll(locals.keySet());
            return output;
        }

        @Override
        public Optional<CompletableFuture<Incomplete<Float>>> get(String name) {
            if (locals.containsKey(name)) {
                return Optional.of(CompletableFuture.completedFuture(locals.get(name)));
            }
            return Optional.empty();
        }

    }
}