package com.minelittlepony.mson.api.export;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;

import com.google.gson.JsonElement;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class VanillaModelSerializer extends ModelSerializer<TexturedModelData> {
    private final JsonBuffer buffer = new JsonBuffer();

    public void exportAll(Path root) {
        ((ModelList)MinecraftClient.getInstance().getEntityModelLoader()).getModelParts().forEach((id, model) -> {
            try {
                writeToFile(root.resolve(id.getId().getNamespace()).resolve(id.getId().getPath() + ".json"), model);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        });
    }

    @Override
    public JsonElement writeToJsonElement(TexturedModelData content) {
        return buffer.write(content);
    }

    public interface ModelList {
        Map<EntityModelLayer, TexturedModelData> getModelParts();
    }

    @Override
    public void close() throws Exception {
    }
}
