package com.minelittlepony.mson.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.mson.impl.key.AbstractModelKeyImpl;
import com.minelittlepony.mson.impl.mixin.ModelListAccessor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MsonModelReloadManager implements PreparableReloadListener {
    private static final Logger LOGGER = LogManager.getLogger("MsonModelReloadManager");
    public static final Identifier ID = MsonImpl.id("models");

    @Nullable
    private volatile CompletableFuture<Void> vanillaModelsReloadTask = null;

    private final MsonImpl mson;

    public MsonModelReloadManager(MsonImpl mson) {
        this.mson = mson;
    }

    public void onVanillaModelsPrepared(CompletableFuture<Void> reloadTask) {
        synchronized (this) {
            vanillaModelsReloadTask = reloadTask;
        }
    }

    public void onVanillaModelsApplied(EntityModelSet entityModelSet) {
        synchronized (this) {
            ((ModelListAccessor)entityModelSet).getModelParts().forEach((layer, vanilla) -> {
                Identifier id = layer.model().withPath(p -> String.format("mson/%s", p));
                ((AbstractModelKeyImpl.Holder)vanilla).setKey(mson.registeredModels.computeIfAbsent(id, i -> new AbstractModelKeyImpl.Value<>(i, mson.foundry)));
            });

            if (MsonMod.DEBUG) {
                Test.exportVanillaModels(mson.foundry.get());
            }
        }
    }

    private CompletableFuture<Void> requireVanillaModels(SharedState store, Executor computeExecutor, PreparationBarrier sync, Executor applyExecutor) {
        synchronized (this) {
            if (vanillaModelsReloadTask == null) {
                LOGGER.info("Vanilla models are not ready, preparing them ourselves...");
                vanillaModelsReloadTask = Minecraft.getInstance().getModelManager().reload(store, computeExecutor, sync, applyExecutor);
            }
            return vanillaModelsReloadTask;
        }
    }

    @Override
    public CompletableFuture<Void> reload(SharedState store, Executor computeExecutor, PreparationBarrier sync, Executor applyExecutor) {
        ModelFoundry loadingFoundry = new ModelFoundry(mson).setWorker(LoadWorker.async(computeExecutor));

        return loadingFoundry.load()
                .thenCompose(sync::wait)
                .thenComposeAsync(_ -> requireVanillaModels(store, computeExecutor, sync, applyExecutor), computeExecutor)
                .thenRunAsync(() -> {
                    mson.foundry.set(loadingFoundry.setWorker(LoadWorker.sync()));
                    mson.getEntityRendererRegistry().initialize();

                    if (MsonMod.DEBUG) {
                        Test.exportBbModels(mson.registeredModels.values());
                    }

                }, applyExecutor);
    }
}
