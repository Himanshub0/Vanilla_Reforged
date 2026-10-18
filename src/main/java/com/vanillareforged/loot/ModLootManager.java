package com.vanillareforged.loot;

import com.vanillareforged.config.ModConfig;
import com.vanillareforged.enchantment.ModEnchantments;
import com.vanillareforged.item.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.EnchantedCountIncreaseLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.function.SetEnchantmentsLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

/**
 * Comprehensive loot table management and structure distribution for Vanilla Reforged.
 * Injects custom Phase 1 smithing templates and Phase 2 custom enchantments/curses into
 * world generation chests and structures with authentic vanilla balance.
 */
public final class ModLootManager {

    private ModLootManager() {}

    public static void registerLootModifications() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!source.isBuiltin()) {
                return;
            }

            // =========================================================================
            // Phase 1: Diamond Upgrade Smithing Template Injections
            // =========================================================================
            if (LootTables.ABANDONED_MINESHAFT_CHEST.equals(key)) {
                LootPool.Builder pool = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1.0f))
                    .conditionally(RandomChanceLootCondition.builder(0.20f))
                    .with(ItemEntry.builder(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE));
                tableBuilder.pool(pool);
            } else if (LootTables.SIMPLE_DUNGEON_CHEST.equals(key) || LootTables.DESERT_PYRAMID_CHEST.equals(key)) {
                LootPool.Builder pool = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1.0f))
                    .conditionally(RandomChanceLootCondition.builder(0.05f))
                    .with(ItemEntry.builder(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE));
                tableBuilder.pool(pool);
            }

            // =========================================================================
            // Phase 2: Structure Enchantment & Curse Injections
            // =========================================================================
            RegistryWrapper.Impl<Enchantment> enchLookup = registries.getOrThrow(RegistryKeys.ENCHANTMENT);

            // 1. Ice Aspect in Ancient City Ice Box Chests (100% Guaranteed spawn)
            if (LootTables.ANCIENT_CITY_ICE_BOX_CHEST.equals(key)) {
                RegistryEntry<Enchantment> iceAspect = enchLookup.getOrThrow(ModEnchantments.ICE_ASPECT);
                LootPool.Builder pool = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1.0f))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .apply(new SetEnchantmentsLootFunction.Builder()
                            .enchantment(iceAspect, ConstantLootNumberProvider.create(1.0f))));
                tableBuilder.pool(pool);
            }

            // 2. Breeze Burst & Ice Aspect in Trial Chambers Rewards (15% chance)
            if (LootTables.TRIAL_CHAMBERS_REWARD_CHEST.equals(key) ||
                LootTables.TRIAL_CHAMBERS_REWARD_COMMON_CHEST.equals(key) ||
                LootTables.TRIAL_CHAMBERS_REWARD_RARE_CHEST.equals(key) ||
                LootTables.TRIAL_CHAMBERS_REWARD_UNIQUE_CHEST.equals(key)) {

                RegistryEntry<Enchantment> breezeBurst = enchLookup.getOrThrow(ModEnchantments.BREEZE_BURST);
                RegistryEntry<Enchantment> iceAspect = enchLookup.getOrThrow(ModEnchantments.ICE_ASPECT);

                LootPool.Builder pool = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1.0f))
                    .conditionally(RandomChanceLootCondition.builder(0.15f))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .apply(new SetEnchantmentsLootFunction.Builder()
                            .enchantment(breezeBurst, ConstantLootNumberProvider.create(1.0f))))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .apply(new SetEnchantmentsLootFunction.Builder()
                            .enchantment(iceAspect, ConstantLootNumberProvider.create(1.0f))));
                tableBuilder.pool(pool);
            }

            // 3. Skyguard & Graviole in End City Treasure Chests (20% chance)
            if (LootTables.END_CITY_TREASURE_CHEST.equals(key)) {
                RegistryEntry<Enchantment> skyguard = enchLookup.getOrThrow(ModEnchantments.SKYGUARD);
                RegistryEntry<Enchantment> graviole = enchLookup.getOrThrow(ModEnchantments.GRAVIOLE);

                LootPool.Builder pool = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1.0f))
                    .conditionally(RandomChanceLootCondition.builder(0.20f))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .apply(new SetEnchantmentsLootFunction.Builder()
                            .enchantment(skyguard, ConstantLootNumberProvider.create(1.0f))))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .apply(new SetEnchantmentsLootFunction.Builder()
                            .enchantment(graviole, ConstantLootNumberProvider.create(1.0f))));
                tableBuilder.pool(pool);
            }

            // 4. Crab's Touch in Shipwrecks, Buried Treasure & Ocean Ruins (25% chance)
            if (LootTables.SHIPWRECK_TREASURE_CHEST.equals(key) ||
                LootTables.BURIED_TREASURE_CHEST.equals(key) ||
                LootTables.UNDERWATER_RUIN_BIG_CHEST.equals(key)) {

                RegistryEntry<Enchantment> crabsTouch = enchLookup.getOrThrow(ModEnchantments.CRABS_TOUCH);

                LootPool.Builder pool = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1.0f))
                    .conditionally(RandomChanceLootCondition.builder(0.25f))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .apply(new SetEnchantmentsLootFunction.Builder()
                            .enchantment(crabsTouch, ConstantLootNumberProvider.create(1.0f))));
                tableBuilder.pool(pool);
            }

            // 5. Scorch Walker in Bastion Remnants & Nether Fortresses
            if (LootTables.BASTION_TREASURE_CHEST.equals(key)) {
                RegistryEntry<Enchantment> scorchWalker = enchLookup.getOrThrow(ModEnchantments.SCORCH_WALKER);

                LootPool.Builder pool = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1.0f))
                    .conditionally(RandomChanceLootCondition.builder(0.25f))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .apply(new SetEnchantmentsLootFunction.Builder()
                            .enchantment(scorchWalker, ConstantLootNumberProvider.create(1.0f))));
                tableBuilder.pool(pool);
            } else if (LootTables.BASTION_OTHER_CHEST.equals(key) ||
                       LootTables.BASTION_BRIDGE_CHEST.equals(key) ||
                       LootTables.BASTION_HOGLIN_STABLE_CHEST.equals(key) ||
                       LootTables.NETHER_BRIDGE_CHEST.equals(key)) {

                RegistryEntry<Enchantment> scorchWalker = enchLookup.getOrThrow(ModEnchantments.SCORCH_WALKER);

                LootPool.Builder pool = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1.0f))
                    .conditionally(RandomChanceLootCondition.builder(0.15f))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .apply(new SetEnchantmentsLootFunction.Builder()
                            .enchantment(scorchWalker, ConstantLootNumberProvider.create(1.0f))));
                tableBuilder.pool(pool);
            }

            // 6. Toxic & Websnare in Jungle Temples and Pillager Outposts
            if (LootTables.JUNGLE_TEMPLE_CHEST.equals(key) || LootTables.PILLAGER_OUTPOST_CHEST.equals(key)) {
                RegistryEntry<Enchantment> toxic = enchLookup.getOrThrow(ModEnchantments.TOXIC);
                RegistryEntry<Enchantment> websnare = enchLookup.getOrThrow(ModEnchantments.WEBSNARE);
                float chance = LootTables.JUNGLE_TEMPLE_CHEST.equals(key) ? 0.20f : 0.15f;

                LootPool.Builder pool = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1.0f))
                    .conditionally(RandomChanceLootCondition.builder(chance))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .apply(new SetEnchantmentsLootFunction.Builder()
                            .enchantment(toxic, ConstantLootNumberProvider.create(1.0f))))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .apply(new SetEnchantmentsLootFunction.Builder()
                            .enchantment(websnare, ConstantLootNumberProvider.create(1.0f))));
                tableBuilder.pool(pool);
            }

            // 7. Vitality & Stride in Stronghold Libraries and Ancient City Chests
            if (LootTables.STRONGHOLD_LIBRARY_CHEST.equals(key) || LootTables.ANCIENT_CITY_CHEST.equals(key)) {
                RegistryEntry<Enchantment> vitality = enchLookup.getOrThrow(ModEnchantments.VITALITY);
                RegistryEntry<Enchantment> stride = enchLookup.getOrThrow(ModEnchantments.STRIDE);
                float chance = LootTables.STRONGHOLD_LIBRARY_CHEST.equals(key) ? 0.25f : 0.20f;

                LootPool.Builder pool = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1.0f))
                    .conditionally(RandomChanceLootCondition.builder(chance))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .apply(new SetEnchantmentsLootFunction.Builder()
                            .enchantment(vitality, ConstantLootNumberProvider.create(1.0f))))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .apply(new SetEnchantmentsLootFunction.Builder()
                            .enchantment(stride, ConstantLootNumberProvider.create(1.0f))));
                tableBuilder.pool(pool);
            }

            // 8. Pyrolysis & Gluttony in Abandoned Mineshafts and Simple Dungeons
            if (LootTables.ABANDONED_MINESHAFT_CHEST.equals(key) || LootTables.SIMPLE_DUNGEON_CHEST.equals(key)) {
                RegistryEntry<Enchantment> pyrolysis = enchLookup.getOrThrow(ModEnchantments.PYROLYSIS);
                RegistryEntry<Enchantment> gluttony = enchLookup.getOrThrow(ModEnchantments.GLUTTONY);
                float chance = LootTables.ABANDONED_MINESHAFT_CHEST.equals(key) ? 0.20f : 0.10f;

                LootPool.Builder pool = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1.0f))
                    .conditionally(RandomChanceLootCondition.builder(chance))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .apply(new SetEnchantmentsLootFunction.Builder()
                            .enchantment(pyrolysis, ConstantLootNumberProvider.create(1.0f))))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .apply(new SetEnchantmentsLootFunction.Builder()
                            .enchantment(gluttony, ConstantLootNumberProvider.create(1.0f))));
                tableBuilder.pool(pool);
            }

            // 9. Curses in Ominous Trial Vaults and Dungeons
            if (LootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_CHEST.equals(key) ||
                LootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_COMMON_CHEST.equals(key) ||
                LootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_RARE_CHEST.equals(key) ||
                LootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_UNIQUE_CHEST.equals(key) ||
                LootTables.SIMPLE_DUNGEON_CHEST.equals(key)) {

                RegistryEntry<Enchantment> curseBreaking = enchLookup.getOrThrow(ModEnchantments.CURSE_OF_BREAKING);
                RegistryEntry<Enchantment> curseClumsiness = enchLookup.getOrThrow(ModEnchantments.CURSE_OF_CLUMSINESS);
                RegistryEntry<Enchantment> curseDisplacement = enchLookup.getOrThrow(ModEnchantments.CURSE_OF_DISPLACEMENT);
                RegistryEntry<Enchantment> curseDoubleEdge = enchLookup.getOrThrow(ModEnchantments.CURSE_OF_DOUBLE_EDGE);
                float chance = LootTables.SIMPLE_DUNGEON_CHEST.equals(key) ? 0.10f : 0.15f;

                LootPool.Builder pool = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1.0f))
                    .conditionally(RandomChanceLootCondition.builder(chance))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .apply(new SetEnchantmentsLootFunction.Builder()
                            .enchantment(curseBreaking, ConstantLootNumberProvider.create(1.0f))))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .apply(new SetEnchantmentsLootFunction.Builder()
                            .enchantment(curseClumsiness, ConstantLootNumberProvider.create(1.0f))))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .apply(new SetEnchantmentsLootFunction.Builder()
                            .enchantment(curseDisplacement, ConstantLootNumberProvider.create(1.0f))))
                    .with(ItemEntry.builder(Items.ENCHANTED_BOOK)
                        .apply(new SetEnchantmentsLootFunction.Builder()
                            .enchantment(curseDoubleEdge, ConstantLootNumberProvider.create(1.0f))));
                tableBuilder.pool(pool);
            }

            // =========================================================================
            // Animal Drops: Pigs and Sheep drop Leather (0-2 base, +0-1 per Looting)
            // =========================================================================
            if (ModConfig.PIG_SHEEP_DROP_LEATHER) {
                boolean isPig = (EntityType.PIG.getLootTableKey().isPresent() && EntityType.PIG.getLootTableKey().get().equals(key))
                    || key.getValue().equals(Identifier.ofVanilla("entities/pig"));
                boolean isSheep = (EntityType.SHEEP.getLootTableKey().isPresent() && EntityType.SHEEP.getLootTableKey().get().equals(key))
                    || key.getValue().equals(Identifier.ofVanilla("entities/sheep"));

                if (isPig || isSheep) {
                    LootPool.Builder leatherPool = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1.0f))
                        .with(ItemEntry.builder(Items.LEATHER)
                            .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(0.0f, 2.0f)))
                            .apply(EnchantedCountIncreaseLootFunction.builder(registries, UniformLootNumberProvider.create(0.0f, 1.0f))));
                    tableBuilder.pool(leatherPool);
                }
            }
        });
    }
}
