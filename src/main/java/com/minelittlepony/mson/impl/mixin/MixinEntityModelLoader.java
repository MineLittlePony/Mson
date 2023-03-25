package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.mson.api.export.VanillaModelSerializer;
import com.minelittlepony.mson.impl.MsonImpl;

import java.util.Map;

@Mixin(EntityModelLoader.class)
abstract class MixinEntityModelLoader implements SynchronousResourceReloader, VanillaModelSerializer.ModelList {
    @Override
    @Accessor("modelParts")
    public abstract Map<EntityModelLayer, TexturedModelData> getModelParts();

    @Inject(method = "reload(Lnet/minecraft/resource/ResourceManager;)V", at = @At("RETURN"))
    public void onReload(ResourceManager resourceManager, CallbackInfo info) {
        MsonImpl.INSTANCE.registerVanillaModels(getModelParts());
    }
}
