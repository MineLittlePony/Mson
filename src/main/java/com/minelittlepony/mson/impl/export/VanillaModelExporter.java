package com.minelittlepony.mson.impl.export;

import net.minecraft.client.model.ModelCuboidData;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.util.math.Vec3f;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Consumer;
import java.util.stream.Stream;

public class VanillaModelExporter {
    public JsonObject of(Consumer<JsonObject> initializer) {
        JsonObject json = new JsonObject();
        initializer.accept(json);
        return json;
    }

    public JsonObject object(JsonObject json, String name, JsonObject obj) {
        if (obj != null && obj.size() > 0) {
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

    public JsonObject array(JsonObject json, String name, Vec3f vec) {
        if (vec != null && !Vec3f.ZERO.equals(vec)) {
            json.add(name, of(vec));
        }
        return json;
    }

    public JsonArray of(Vec3f vec) {
        return of(vec.getX(), vec.getY(), vec.getZ());
    }

    public <T extends JsonElement> JsonArray of(Stream<T> stream) {
        JsonArray arr = new JsonArray();
        stream.forEach(arr::add);
        return arr;
    }

    public JsonObject export(TexturedModelData model) {
        return export((JsonConvertable)(Object)model);
    }

    public JsonObject export(ModelData part) {
        return export(part.getRoot());
    }

    public JsonObject export(ModelPartData part) {
        return export((JsonConvertable)(Object)part);
    }

    public JsonObject export(ModelCuboidData part) {
        return export((JsonConvertable)(Object)part);
    }

    public JsonObject export(JsonConvertable part) {
        return part.toJson(this);
    }

    public interface JsonConvertable {
        JsonObject toJson(VanillaModelExporter exporter);
    }
}
