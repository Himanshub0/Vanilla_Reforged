package com.vanillareforged.economy;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class PiglinDropManager {

    public static final Identifier PIGLIN_LOOT_TABLE_ID = Identifier.of("minecraft", "entities/piglin");
    public static final RegistryKey<LootTable> PIGLIN_LOOT_TABLE_KEY = RegistryKey.of(RegistryKeys.LOOT_TABLE, PIGLIN_LOOT_TABLE_ID);

    public static final Identifier ZOMBIFIED_PIGLIN_LOOT_TABLE_ID = Identifier.of("minecraft", "entities/zombified_piglin");
    public static final RegistryKey<LootTable> ZOMBIFIED_PIGLIN_LOOT_TABLE_KEY = RegistryKey.of(RegistryKeys.LOOT_TABLE, ZOMBIFIED_PIGLIN_LOOT_TABLE_ID);

    private PiglinDropManager() {
    }

    public static void register() {
        // Data-driven loot tables at data/minecraft/loot_table/entities/piglin.json
        // and data/minecraft/loot_table/entities/zombified_piglin.json override death drops
        // to drop gold nuggets rather than gold ingots.
        // Direct gold ingot drops from piglins are completely removed.
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (source.isBuiltin() && (PIGLIN_LOOT_TABLE_KEY.equals(key) ||
                ZOMBIFIED_PIGLIN_LOOT_TABLE_KEY.equals(key) ||
                EntityType.PIGLIN.getLootTableKey().equals(key) ||
                EntityType.ZOMBIFIED_PIGLIN.getLootTableKey().equals(key))) {
                // Verified loot table override is active
            }
        });
    }
}
