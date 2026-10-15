package com.vanillareforged;

import com.vanillareforged.balance.DurabilityManager;
import com.vanillareforged.enchantment.ArmorEnchantmentHandler;
import com.vanillareforged.enchantment.CombatEnchantmentHandler;
import com.vanillareforged.enchantment.MiningEnchantmentHandler;
import com.vanillareforged.enchantment.ModEnchantments;
import com.vanillareforged.enchantment.VanillaEnchantmentModifier;
import com.vanillareforged.item.ModItems;
import com.vanillareforged.loot.ModLootManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VanillaReforgedMod implements ModInitializer {
    public static final String MOD_ID = "vanilla_reforged";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Vanilla Reforged — Phase 1 Progression Overhaul");

        // 1. Register items (Diamond Crystal, Diamond Upgrade Smithing Template)
        ModItems.registerItems();

        // 2. Apply centralized durability balancing (Gold & Copper armor, Gold tools)
        DurabilityManager.applyDurabilityChanges();

        // 3. Register structure loot injections (Mineshaft chests, etc.)
        ModLootManager.registerLootModifications();

        // 4. Initialize Phase 2 Enchantment system
        ModEnchantments.initialize();
        CombatEnchantmentHandler.register();
        MiningEnchantmentHandler.register();
        ArmorEnchantmentHandler.register();
        VanillaEnchantmentModifier.initialize();

        // 5. Initialize Villager Economy and Piglin Drops
        com.vanillareforged.economy.VillagerTradeManager.registerTrades();
        com.vanillareforged.economy.PiglinDropManager.register();

        // 6. Register Entities (King Zombie, Dire Wolf)
        com.vanillareforged.entity.ModEntities.registerEntities();

        // 7. Register Entity Lifecycle Events
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof net.minecraft.entity.mob.ZombieEntity) {
                com.vanillareforged.entity.ZombieReinforcementManager.cleanupOnEntityDeath(entity.getUuid());
            }
        });

        LOGGER.info("Vanilla Reforged Phase 1, Phase 2 & Phase 3 successfully initialized");
    }
}
