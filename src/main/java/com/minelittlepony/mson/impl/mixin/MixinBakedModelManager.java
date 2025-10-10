package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.resource.ResourceReloader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.mson.impl.MsonImpl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(BakedModelManager.class)
abstract class MixinBakedModelManager {
    @Inject(method = "reload", at = @At("RETURN"))
    private void onReload(ResourceReloader.Store store, Executor computeExecutor, ResourceReloader.Synchronizer synchronizer, Executor applyExecutor,
            CallbackInfoReturnable<CompletableFuture<Void>> info) {
        MsonImpl.INSTANCE.onVanillaModelsPrepared(info.getReturnValue());
    }

    @Inject(method = "upload(Lnet/minecraft/client/render/model/BakedModelManager$BakingResult;)V", at = @At("TAIL"))
    private void onUpload(@Coerce Object bakingResult, CallbackInfo info) {
        MsonImpl.INSTANCE.onVanillaModelsApplied();
    }
}
