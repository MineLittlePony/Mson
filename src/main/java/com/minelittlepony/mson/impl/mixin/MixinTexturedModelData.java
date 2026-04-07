package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDefinition;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MaterialDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.UVPair;

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

@Mixin(LayerDefinition.class)
abstract class MixinTexturedModelData implements MsonImpl.KeyHolder, JsonBuffer.JsonConvertable {
    private Optional<ModelKey<?>> key = Optional.empty();

    @Shadow private @Final MeshDefinition mesh;
    @Shadow private @Final MaterialDefinition material;

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
            exporter.object(json, "data", ((JsonObject)exporter.write(mesh.getRoot())).get("children"));
            exporter.object(json, "texture", exporter.of(js -> {
                js.addProperty("w", ((MixinTextureDimensions)material).getWidth());
                js.addProperty("h", ((MixinTextureDimensions)material).getHeight());
            }));
        });
    }
}
@Mixin(PartDefinition.class)
abstract class MixinModelPartData implements JsonBuffer.JsonConvertable {
    @Shadow private @Final List<CubeDefinition> cubes;
    @Shadow private @Final PartPose partPose;
    @Shadow private @Final Map<String, PartDefinition> children;
    @Override
    public JsonObject toJson(JsonBuffer exporter) {
        return exporter.of(json -> {
            if (!cubes.isEmpty()) json.add("cubes", exporter.of(cubes.stream().map(exporter::write)));
            if (!children.isEmpty()) json.add("children", exporter.of(js -> {
                children.forEach((key, value) -> js.add(key, exporter.write(value)));
            }));
            if (partPose != PartPose.ZERO) {
                exporter.array(json, "pivot", partPose.x(), partPose.y(), partPose.z());
                exporter.array(json, "rotate", partPose.xRot(), partPose.yRot(), partPose.zRot());
            }
        });
    }
}
@Mixin(CubeDefinition.class)
abstract class MixinModelCuboidData implements JsonBuffer.JsonConvertable {
    @Shadow private @Final Vector3fc origin;
    @Shadow private @Final Vector3fc dimensions;
    @Shadow private @Final CubeDeformation grow;
    @Shadow private @Final boolean mirror;
    @Shadow private @Final UVPair texCoord;
    @Shadow private @Final UVPair texScale;
    @Override
    public JsonObject toJson(JsonBuffer exporter) {
        return exporter.of(json -> {
            exporter.array(json, "from", origin);
            exporter.array(json, "size", dimensions);
            exporter.array(json, "dilate", ((MixinDilation)grow).getX(), ((MixinDilation)grow).getY(), ((MixinDilation)grow).getZ());
            if (mirror) json.addProperty("mirror", mirror);
            exporter.object(json, "texture", exporter.of(js -> {
                if (texCoord.u() != 0) js.addProperty("u", texCoord.u());
                if (texCoord.v() != 0) js.addProperty("v", texCoord.v());
                if (texScale.u() != 1) js.addProperty("su", texScale.u());
                if (texScale.v() != 1) js.addProperty("sv", texScale.v());
            }));
        });
    }
}