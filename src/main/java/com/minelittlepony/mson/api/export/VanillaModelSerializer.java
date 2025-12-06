package com.minelittlepony.mson.api.export;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.LayerDefinition;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.minelittlepony.mson.api.parser.ModelLoader;
import com.minelittlepony.mson.impl.mixin.ModelListAccessor;
import com.minelittlepony.mson.impl.model.bbmodel.BBModelFormat;
import com.minelittlepony.mson.impl.model.json.MsonModelFormat;

import java.io.IOException;
import java.nio.file.Path;

public class VanillaModelSerializer extends ModelSerializer<LayerDefinition> {

    @Nullable
    private final ModelLoader modelLoader;

    public VanillaModelSerializer(ModelLoader modelLoader) {
        this.modelLoader = modelLoader;
    }

    public VanillaModelSerializer() {
        this(null);
    }

    public void exportAll(Path root) {
        ((ModelListAccessor)Minecraft.getInstance().getEntityModels()).getModelParts().forEach((id, model) -> {
            try {
                Path path = root.resolve(id.model().getNamespace()).resolve(id.model().getPath() + ".json");
                writeToFile(path, model);

                if (modelLoader != null) {
                    ((MsonModelFormat)MsonModelFormat.INSTANCE).loadModel(id.model(), path, modelLoader).ifPresent(content -> {
                        BBModelFormat.INSTANCE.createSerializer().ifPresent(serializer -> {
                            try (serializer) {
                                serializer.writeToFile(
                                        root.resolve(id.model().getNamespace()).resolve(id.model().getPath() + ".bbmodel").normalize(),
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
    public JsonElement writeToJsonElement(LayerDefinition content) {
        return JsonBuffer.INSTANCE.write(content);
    }

    @Override
    public void close() throws Exception {
    }
}
