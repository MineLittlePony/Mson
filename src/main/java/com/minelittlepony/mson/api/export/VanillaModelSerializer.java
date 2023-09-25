package com.minelittlepony.mson.api.export;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.minelittlepony.mson.api.parser.ModelLoader;
import com.minelittlepony.mson.impl.model.bbmodel.BBModelFormat;
import com.minelittlepony.mson.impl.model.json.MsonModelFormat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class VanillaModelSerializer extends ModelSerializer<TexturedModelData> {

    @Nullable
    private final ModelLoader modelLoader;

    public VanillaModelSerializer(ModelLoader modelLoader) {
        this.modelLoader = modelLoader;
    }

    public VanillaModelSerializer() {
        this(null);
    }

    public void exportAll(Path root) {
        ((ModelList)MinecraftClient.getInstance().getEntityModelLoader()).getModelParts().forEach((id, model) -> {
            try {
                Path path = root.resolve(id.getId().getNamespace()).resolve(id.getId().getPath() + ".json");
                writeToFile(path, model);

                if (modelLoader != null) {
                    ((MsonModelFormat)MsonModelFormat.INSTANCE).loadModel(id.getId(), path, modelLoader).ifPresent(content -> {
                        BBModelFormat.INSTANCE.createSerializer().ifPresent(serializer -> {
                            try (serializer) {
                                serializer.writeToFile(
                                        root.resolve(id.getId().getNamespace()).resolve(id.getId().getPath() + ".bbmodel").normalize(),
                                        content
                                );
                            } catch (Exception ex) {
                                throw new AssertionError(ex);
                            }
                        });
                    });
                }
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        });
    }

    @Override
    public JsonElement writeToJsonElement(TexturedModelData content) {
        return JsonBuffer.INSTANCE.write(content);
    }

    public interface ModelList {
        Map<EntityModelLayer, TexturedModelData> getModelParts();
    }

    @Override
    public void close() throws Exception {
    }
}
