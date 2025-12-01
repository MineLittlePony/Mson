package com.minelittlepony.mson.impl.mixin;

import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.api.export.JsonBuffer;
import com.minelittlepony.mson.impl.MsonImpl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelCuboidData;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TextureDimensions;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.util.math.Vector2f;

@Mixin(TexturedModelData.class)
abstract class MixinTexturedModelData implements MsonImpl.KeyHolder, JsonBuffer.JsonConvertable {
    private Optional<ModelKey<?>> key = Optional.empty();

    @Shadow private @Final ModelData data;
    @Shadow private @Final TextureDimensions dimensions;

    @Override
    public void setKey(ModelKey<?> key) {
        this.key = Optional.of(key);
    }

    @Inject(method = "createModel", at = @At("HEAD"), cancellable = true)
    public void createModel(CallbackInfoReturnable<ModelPart> info) {
        key.flatMap(ModelKey::createTree).ifPresent(info::setReturnValue);
    }

    @Override
    public JsonElement toJson(JsonBuffer exporter) {
        return exporter.of(json -> {
            exporter.object(json, "data", ((JsonObject)exporter.write(data)).get("children"));
            exporter.object(json, "texture", exporter.of(js -> {
                js.addProperty("w", ((MixinTextureDimensions)dimensions).getWidth());
                js.addProperty("h", ((MixinTextureDimensions)dimensions).getHeight());
            }));
        });
    }
}
@Mixin(ModelPartData.class)
abstract class MixinModelPartData implements JsonBuffer.JsonConvertable {
    @Shadow private @Final List<ModelCuboidData> cuboidData;
    @Shadow private @Final ModelTransform transform;
    @Shadow private @Final Map<String, ModelPartData> children;
    @Override
    public JsonObject toJson(JsonBuffer exporter) {
        return exporter.of(json -> {
            if (!cuboidData.isEmpty()) json.add("cubes", exporter.of(cuboidData.stream().map(exporter::write)));
            if (!children.isEmpty()) json.add("children", exporter.of(js -> {
                children.forEach((key, value) -> js.add(key, exporter.write(value)));
            }));
            if (transform != ModelTransform.NONE) {
                exporter.array(json, "pivot", transform.x(), transform.y(), transform.z());
                exporter.array(json, "rotate", transform.pitch(), transform.yaw(), transform.roll());
            }
        });
    }
}
@Mixin(ModelCuboidData.class)
abstract class MixinModelCuboidData implements JsonBuffer.JsonConvertable {
    @Shadow private @Final Vector3fc offset;
    @Shadow private @Final Vector3fc dimensions;
    @Shadow private @Final Dilation extraSize;
    @Shadow private @Final boolean mirror;
    @Shadow private @Final Vector2f textureUV;
    @Shadow private @Final Vector2f textureScale;
    @Override
    public JsonObject toJson(JsonBuffer exporter) {
        return exporter.of(json -> {
            exporter.array(json, "from", offset);
            exporter.array(json, "size", dimensions);
            exporter.array(json, "dilate", ((MixinDilation)extraSize).getX(), ((MixinDilation)extraSize).getY(), ((MixinDilation)extraSize).getZ());
            if (mirror) json.addProperty("mirror", mirror);
            exporter.object(json, "texture", exporter.of(js -> {
                if (textureUV.x() != 0) js.addProperty("u", textureUV.x());
                if (textureUV.y() != 0) js.addProperty("v", textureUV.y());
                if (textureScale.x() != 1) js.addProperty("su", textureScale.x());
                if (textureScale.y() != 1) js.addProperty("sv", textureScale.y());
            }));
        });
    }
}