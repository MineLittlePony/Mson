package com.minelittlepony.mson.impl.mixin;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.mson.impl.MsonImpl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ModelManager.class)
abstract class MixinBakedModelManager implements PreparableReloadListener {

    @Shadow
    private EntityModelSet entityModelSet;

    @ModifyReturnValue(method = "reload", at = @At("RETURN"))
    private CompletableFuture<Void> onReload(CompletableFuture<Void> future) {
        MsonImpl.INSTANCE.onVanillaModelsPrepared(future);
        return future;
    }

    @Inject(method = "apply(Lnet/minecraft/client/resources/model/ModelManager$ReloadState;)V", at = @At("TAIL"))
    private void onUpload(@Coerce Object bakingResult, CallbackInfo info) {
        MsonImpl.INSTANCE.onVanillaModelsApplied(entityModelSet);
    }
}
