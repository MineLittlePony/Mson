package com.minelittlepony.mson.impl;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceType;

public final class MsonMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(MsonImpl.INSTANCE);
        if (FabricLoader.getInstance().isModLoaded("physicsmod")) {
            FabricLoader.getInstance().getEntrypoints("mson:api/physicsmod", ClientModInitializer.class).forEach(item -> {
                item.onInitializeClient();
            });
        }

        Test.init();
    }
}
