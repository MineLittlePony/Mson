package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.util.profiler.Profiler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.mson.impl.MsonImpl;

@Mixin(BakedModelManager.class)
abstract class MixinBakedModelManager {
    @Inject(method = "upload(Lnet/minecraft/client/render/model/BakedModelManager$BakingResult;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("TAIL"))
    private void upload(@Coerce Object bakingResult, Profiler profiler, CallbackInfo info) {
        MsonImpl.INSTANCE.registerVanillaModels();
    }
}
