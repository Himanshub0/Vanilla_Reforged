package com.vanillareforged.client;

import com.vanillareforged.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VanillaReforgedClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("vanilla_reforged_client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Vanilla Reforged Client — Enchantment Tooltips & Overhaul");
        EnchantmentTooltipHandler.register();

        // Register Phase 3 Entity Renderers
        EntityRendererRegistry.register(ModEntities.KING_ZOMBIE, ZombieEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.DIRE_WOLF, DireWolfEntityRenderer::new);

        // Register Projectile Renderers
        EntityRendererRegistry.register(ModEntities.AMETHYST_SHARD, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.SONIC_BOOM, EmptyEntityRenderer::new);
    }
}

