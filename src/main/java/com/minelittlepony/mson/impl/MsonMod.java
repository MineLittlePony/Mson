package com.minelittlepony.mson.impl;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceType;

public final class MsonMod implements ClientModInitializer {
    public static final boolean DEBUG = Boolean.getBoolean("mson.debug");

    @Override
    public void onInitializeClient() {
        ResourceLoader.get(ResourceType.CLIENT_RESOURCES).registerReloader(MsonImpl.RELOADER_ID, MsonImpl.INSTANCE);
        if (FabricLoader.getInstance().isModLoaded("physicsmod")) {
            FabricLoader.getInstance().getEntrypoints("mson:api/physicsmod", ClientModInitializer.class).forEach(item -> {
                item.onInitializeClient();
            });
        }

        if (DEBUG) {
            Test.init();
        }
    }
}
