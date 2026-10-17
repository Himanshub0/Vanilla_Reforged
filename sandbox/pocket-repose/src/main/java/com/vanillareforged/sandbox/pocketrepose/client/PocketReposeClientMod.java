package com.vanillareforged.sandbox.pocketrepose.client;

import com.vanillareforged.sandbox.pocketrepose.PocketReposeMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class PocketReposeClientMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        PocketReposeMod.LOGGER.info("Pocket Repose Client Initialized.");
    }
}
