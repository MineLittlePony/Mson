package com.minelittlepony.mson.impl.model;

import net.minecraft.client.model.Model;
import net.minecraft.util.Identifier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelContext;
import com.minelittlepony.mson.api.MsonModel;
import com.minelittlepony.mson.api.json.JsonComponent;
import com.minelittlepony.mson.api.json.JsonContext;
import com.minelittlepony.mson.api.json.JsonVariables;
import com.minelittlepony.mson.api.json.Variables;
import com.minelittlepony.mson.api.model.Texture;
import com.minelittlepony.mson.impl.LocalsImpl;
import com.minelittlepony.mson.impl.key.ReflectedModelKey;
import com.minelittlepony.mson.util.Incomplete;
import com.minelittlepony.mson.util.JsonUtil;

import javax.annotation.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class JsonSlot<T extends Model> implements JsonComponent<T> {

    public static final Identifier ID = new Identifier("mson", "slot");

    private final ReflectedModelKey<T> implementation;

    private final CompletableFuture<JsonContext> content;

    private final Map<String, Incomplete<Float>> locals;

    private final Optional<Texture> texture;

    @Nullable
    private String name;

    public JsonSlot(JsonContext context, String name, JsonObject json) {
        implementation = ReflectedModelKey.fromJson(json);
        content = context.resolve(json.get("content"));
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
                        e -> LocalsImpl.createLocal(e.getValue())));
    }

    @Override
    public T export(ModelContext context) {
        return context.computeIfAbsent(name, key -> {
            JsonContext jsContext = content.get();
            ModelContext subContext = jsContext
                    // the contents of a slot have their own variable lookup tree distinct from this one
                    // but locals defined within the slot element are appended over what the file defines
                    // i.e
                    //              root_1
                    //               |
                    //               \/         root_2
                    //              parent      |
                    //               |         parent_2
                    //               \/         |
                    //              main_file  \/
                    //               |        imported_file
                    //               |       /
                    //               |-slot\/
                    //              self
                    .createContext(context.getModel(), new LocalsImpl(implementation.getId(), new Vars(jsContext)))
                    .resolve(context.getContext());

            T inst = implementation.createModel(subContext);
            if (inst instanceof MsonModel) {
                ((MsonModel)inst).init(subContext);
            }

            return inst;
        });
    }

    class Vars implements JsonVariables {

        private final JsonVariables parent;

        Vars(JsonVariables parent) {
            this.parent = parent;
        }

        @Override
        public CompletableFuture<Texture> getTexture() {
            return texture
                    .map(CompletableFuture::completedFuture)
                    .orElseGet(parent::getTexture);
        }

        @Override
        public Variables getVarLookup() {
            return parent.getVarLookup();
        }

        @Override
        public CompletableFuture<Incomplete<Float>> getLocalVariable(String name) {
            if (locals.containsKey(name)) {
                return CompletableFuture.completedFuture(locals.get(name));
            }
            return parent.getLocalVariable(name);
        }

        @Override
        public CompletableFuture<Set<String>> getVariableNames() {
            return parent.getVariableNames().thenApplyAsync(output -> {
               output.addAll(locals.keySet());
               return output;
            });
        }
    }
}
