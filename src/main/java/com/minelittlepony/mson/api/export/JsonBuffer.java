package com.minelittlepony.mson.api.export;

import net.minecraft.client.model.geom.builders.CubeDefinition;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import org.joml.Vector3fc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * An exporter that accepts vanilla minecraft's models and outputs them to
 * a mson formatted json file.
 */
public final class JsonBuffer {
    public static final JsonBuffer INSTANCE = new JsonBuffer();

    private JsonBuffer() {}

    public JsonObject of(Consumer<JsonObject> initializer) {
        JsonObject json = new JsonObject();
        initializer.accept(json);
        return json;
    }

    public JsonObject object(JsonObject json, String name, JsonElement obj) {
        if (obj != null && obj.isJsonObject() && obj.getAsJsonObject().size() > 0) {
            json.add(name, obj);
        }
        return json;
    }

    public JsonObject array(JsonObject json, String name, float...pars) {
        for (int i = 0; i < pars.length; i++) {
            if (pars[0] != 0) {
                json.add(name, of(pars));
                break;
            }
        }
        return json;
    }

    public JsonArray of(float...pars) {
        JsonArray arr = new JsonArray();
        for (int i = 0; i < pars.length; i++) {
            arr.add(pars[i]);
        }
        return arr;
    }

    public JsonObject array(JsonObject json, String name, Vector3fc vec) {
        if (vec != null && vec.length() != 0) {
            json.add(name, of(vec));
        }
        return json;
    }

    public JsonArray of(Vector3fc vec) {
        return of(vec.x(), vec.y(), vec.z());
    }

    public <T extends JsonConvertable> JsonArray of(Iterable<T> values) {
        JsonArray arr = new JsonArray();
        values.forEach(i -> arr.add(i.toJson(this)));
        return arr;
    }

    public <T extends JsonElement> JsonArray of(Stream<T> stream) {
        JsonArray arr = new JsonArray();
        stream.forEach(arr::add);
        return arr;
    }

    public JsonElement write(LayerDefinition model) {
        return write((JsonConvertable)(Object)model);
    }

    public JsonElement write(MeshDefinition part) {
        return write(part.getRoot());
    }

    public JsonElement write(PartDefinition part) {
        return write((JsonConvertable)(Object)part);
    }

    public JsonElement write(CubeDefinition part) {
        return write((JsonConvertable)(Object)part);
    }

    public JsonElement write(JsonConvertable part) {
        return part.toJson(this);
    }

    public interface JsonConvertable {
        JsonElement toJson(JsonBuffer exporter);
    }
}
