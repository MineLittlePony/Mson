package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.mson.api.ModelKey;
import com.minelittlepony.mson.impl.MsonImpl;

import java.util.Map;
import java.util.Optional;

@Mixin(EntityModelLoader.class)
abstract class MixinEntityModelLoader implements SynchronousResourceReloader {
    @Shadow
    private Map<EntityModelLayer, TexturedModelData> modelParts;

    @Inject(method = "reload(Lnet/minecraft/resource/ResourceManager;)V", at = @At("RETURN"))
    public void onReload(ResourceManager resourceManager, CallbackInfo info) {
        MsonImpl.INSTANCE.registerVanillaModels(modelParts);
    }
}
@Mixin(TexturedModelData.class)
abstract class MixinTexturedModelData implements MsonImpl.KeyHolder {
    private Optional<ModelKey<?>> key = Optional.empty();

    @Override
    public void setKey(ModelKey<?> key) {
        this.key = Optional.of(key);
    }

    @Inject(method = "createModel", at = @At("HEAD"))
    public void createModel(CallbackInfoReturnable<ModelPart> info) {
        key.flatMap(ModelKey::createTree).ifPresent(info::setReturnValue);
    }
}
