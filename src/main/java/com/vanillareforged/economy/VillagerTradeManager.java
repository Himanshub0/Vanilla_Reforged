package com.vanillareforged.economy;

import com.vanillareforged.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.TradedItem;
import net.minecraft.village.VillagerProfession;

import java.util.List;
import java.util.Optional;

public final class VillagerTradeManager {

    public static final java.util.Set<Item> DIAMOND_EQUIPMENT = java.util.Set.of(
        Items.DIAMOND_SWORD,
        Items.DIAMOND_PICKAXE,
        Items.DIAMOND_AXE,
        Items.DIAMOND_SHOVEL,
        Items.DIAMOND_HOE,
        Items.DIAMOND_HELMET,
        Items.DIAMOND_CHESTPLATE,
        Items.DIAMOND_LEGGINGS,
        Items.DIAMOND_BOOTS
    );

    public static boolean isDiamondEquipment(ItemStack stack) {
        return stack != null && !stack.isEmpty() && DIAMOND_EQUIPMENT.contains(stack.getItem());
    }

    private VillagerTradeManager() {
    }

    public static void registerTrades() {
        registerArmorerTrades();
        registerToolsmithTrades();
        registerWeaponsmithTrades();
        registerLibrarianTrades();
    }

    /**
     * Armorer trades:
     * Focus on Gold Ingots and Enchanted Golden Armor.
     * Diamond armor is completely excluded and prevented.
     */
    private static void registerArmorerTrades() {
        // Level 1: Novice
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.ARMORER, 1, factories -> {
            // Sell 4 Gold Ingots for 1 Emerald (Player sells gold for currency)
            factories.add(createSellItemForEmeraldTrade(Items.GOLD_INGOT, 4, 16, 2));
            // Buy Enchanted Golden Helmet with 5 Gold Ingots
            factories.add(createEnchantedGoldTrade(Items.GOLDEN_HELMET, 5, 5, 15, 1));
            // Buy Enchanted Golden Boots with 4 Gold Ingots
            factories.add(createEnchantedGoldTrade(Items.GOLDEN_BOOTS, 4, 5, 15, 1));
        });

        // Level 2: Apprentice
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.ARMORER, 2, factories -> {
            // Buy Enchanted Golden Chestplate with 7 Gold Ingots
            factories.add(createEnchantedGoldTrade(Items.GOLDEN_CHESTPLATE, 7, 10, 20, 5));
            // Buy Enchanted Golden Leggings with 6 Gold Ingots
            factories.add(createEnchantedGoldTrade(Items.GOLDEN_LEGGINGS, 6, 10, 20, 5));
        });

        // Level 3: Journeyman
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.ARMORER, 3, factories -> {
            // Sell 3 Gold Ingots for 1 Emerald
            factories.add(createSellItemForEmeraldTrade(Items.GOLD_INGOT, 3, 16, 10));
            // Mid-tier Enchanted Golden Helmet (lvl 15-25)
            factories.add(createEnchantedGoldTrade(Items.GOLDEN_HELMET, 6, 15, 25, 10));
            // Mid-tier Enchanted Golden Boots (lvl 15-25)
            factories.add(createEnchantedGoldTrade(Items.GOLDEN_BOOTS, 5, 15, 25, 10));
        });

        // Level 4: Expert
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.ARMORER, 4, factories -> {
            // High-tier Enchanted Golden Leggings (lvl 20-30)
            factories.add(createEnchantedGoldTrade(Items.GOLDEN_LEGGINGS, 8, 20, 30, 15));
            // High-tier Enchanted Golden Chestplate (lvl 20-30)
            factories.add(createEnchantedGoldTrade(Items.GOLDEN_CHESTPLATE, 9, 20, 30, 15));
        });

        // Level 5: Master
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.ARMORER, 5, factories -> {
            // Masterwork Golden Chestplate (Level 30 enchantment) for 10 Gold Ingots + 1 Diamond
            factories.add(createHighLevelEnchantedGoldTrade(Items.GOLDEN_CHESTPLATE, 10, 1, 30, 30));
            // Masterwork Golden Leggings (Level 30 enchantment) for 8 Gold Ingots + 1 Diamond
            factories.add(createHighLevelEnchantedGoldTrade(Items.GOLDEN_LEGGINGS, 8, 1, 30, 30));
        });
    }

    /**
     * Toolsmith trades:
     * Focus on Gold Ingots and Enchanted Golden Tools.
     * Diamond tools are completely excluded and prevented.
     */
    private static void registerToolsmithTrades() {
        // Level 1: Novice
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.TOOLSMITH, 1, factories -> {
            // Sell 4 Gold Ingots for 1 Emerald
            factories.add(createSellItemForEmeraldTrade(Items.GOLD_INGOT, 4, 16, 2));
            // Buy Enchanted Golden Pickaxe with 3 Gold Ingots
            factories.add(createEnchantedGoldTrade(Items.GOLDEN_PICKAXE, 3, 5, 15, 1));
            // Buy Enchanted Golden Shovel with 2 Gold Ingots
            factories.add(createEnchantedGoldTrade(Items.GOLDEN_SHOVEL, 2, 5, 15, 1));
        });

        // Level 2: Apprentice
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.TOOLSMITH, 2, factories -> {
            // Buy Enchanted Golden Axe with 3 Gold Ingots
            factories.add(createEnchantedGoldTrade(Items.GOLDEN_AXE, 3, 10, 20, 5));
            // Buy Enchanted Golden Hoe with 2 Gold Ingots
            factories.add(createEnchantedGoldTrade(Items.GOLDEN_HOE, 2, 10, 20, 5));
        });

        // Level 3: Journeyman
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.TOOLSMITH, 3, factories -> {
            // Sell 3 Gold Ingots for 1 Emerald
            factories.add(createSellItemForEmeraldTrade(Items.GOLD_INGOT, 3, 16, 10));
            // Mid-tier Enchanted Golden Pickaxe (lvl 15-25)
            factories.add(createEnchantedGoldTrade(Items.GOLDEN_PICKAXE, 4, 15, 25, 10));
            // Mid-tier Enchanted Golden Shovel (lvl 15-25)
            factories.add(createEnchantedGoldTrade(Items.GOLDEN_SHOVEL, 3, 15, 25, 10));
        });

        // Level 4: Expert
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.TOOLSMITH, 4, factories -> {
            // High-tier Enchanted Golden Axe (lvl 20-30)
            factories.add(createEnchantedGoldTrade(Items.GOLDEN_AXE, 5, 20, 30, 15));
            // High-tier Enchanted Golden Hoe (lvl 20-30)
            factories.add(createEnchantedGoldTrade(Items.GOLDEN_HOE, 4, 20, 30, 15));
        });

        // Level 5: Master
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.TOOLSMITH, 5, factories -> {
            // Masterwork Golden Pickaxe (Level 30 enchantment) for 8 Gold Ingots + 1 Diamond
            factories.add(createHighLevelEnchantedGoldTrade(Items.GOLDEN_PICKAXE, 8, 1, 30, 30));
            // Masterwork Golden Axe (Level 30 enchantment) for 8 Gold Ingots + 1 Diamond
            factories.add(createHighLevelEnchantedGoldTrade(Items.GOLDEN_AXE, 8, 1, 30, 30));
        });
    }

    /**
     * Weaponsmith trades:
     * Focus on Gold Ingots and Enchanted Golden Weapons.
     * Diamond weapons are completely excluded and prevented.
     */
    private static void registerWeaponsmithTrades() {
        // Level 1: Novice
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.WEAPONSMITH, 1, factories -> {
            // Sell 4 Gold Ingots for 1 Emerald
            factories.add(createSellItemForEmeraldTrade(Items.GOLD_INGOT, 4, 16, 2));
            // Buy Enchanted Golden Sword with 4 Gold Ingots
            factories.add(createEnchantedGoldTrade(Items.GOLDEN_SWORD, 4, 5, 15, 1));
        });

        // Level 2: Apprentice
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.WEAPONSMITH, 2, factories -> {
            // Buy Enchanted Golden Axe with 5 Gold Ingots
            factories.add(createEnchantedGoldTrade(Items.GOLDEN_AXE, 5, 10, 20, 5));
        });

        // Level 3: Journeyman
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.WEAPONSMITH, 3, factories -> {
            // Sell 3 Gold Ingots for 1 Emerald
            factories.add(createSellItemForEmeraldTrade(Items.GOLD_INGOT, 3, 16, 10));
            // Mid-tier Enchanted Golden Sword (lvl 15-25)
            factories.add(createEnchantedGoldTrade(Items.GOLDEN_SWORD, 6, 15, 25, 10));
        });

        // Level 4: Expert
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.WEAPONSMITH, 4, factories -> {
            // High-tier Enchanted Golden Axe (lvl 20-30)
            factories.add(createEnchantedGoldTrade(Items.GOLDEN_AXE, 7, 20, 30, 15));
            // High-tier Enchanted Golden Sword (lvl 20-30)
            factories.add(createEnchantedGoldTrade(Items.GOLDEN_SWORD, 8, 20, 30, 15));
        });

        // Level 5: Master
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.WEAPONSMITH, 5, factories -> {
            // Masterwork Golden Sword (Level 30 enchantment) for 10 Gold Ingots + 1 Diamond
            factories.add(createHighLevelEnchantedGoldTrade(Items.GOLDEN_SWORD, 10, 1, 30, 30));
            // Masterwork Golden Axe (Level 30 enchantment) for 10 Gold Ingots + 1 Diamond
            factories.add(createHighLevelEnchantedGoldTrade(Items.GOLDEN_AXE, 10, 1, 30, 30));
        });
    }

    /**
     * Librarian trades:
     * Only trades with Gold Ingots instead of Emeralds.
     * Custom enchanted book offers added for Phase 2 enchantments whose acquisition rules specify librarian trades.
     */
    private static void registerLibrarianTrades() {
        // Level 1: Novice - Early utility & combat enchantments + paper for gold
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.LIBRARIAN, 1, factories -> {
            // Sell 24 Paper for 1 Gold Ingot
            factories.add(createSellItemForGoldTrade(Items.PAPER, 24, 1, 16, 2));
            // Buy Level 1 Utility/Combat Enchanted Book for 8 Gold Ingots + 1 Book
            factories.add(createGoldEnchantedBookTrade(
                List.of(
                    ModEnchantments.GLUTTONY,
                    ModEnchantments.KINETIC_PROTECTION,
                    ModEnchantments.OUTREACH,
                    ModEnchantments.SWIFT_STRIKE
                ),
                1, 1, 8, 1
            ));
        });

        // Level 2: Apprentice - Armor & tool enchantments + books for gold + lantern
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.LIBRARIAN, 2, factories -> {
            // Sell 4 Books for 1 Gold Ingot
            factories.add(createSellItemForGoldTrade(Items.BOOK, 4, 1, 12, 5));
            // Buy Lantern for 2 Gold Ingots
            factories.add(createBuyItemForGoldTrade(Items.LANTERN, 1, 2, 12, 5));
            // Buy Level 1 Tool/Armor Enchanted Book for 10 Gold Ingots + 1 Book
            factories.add(createGoldEnchantedBookTrade(
                List.of(
                    ModEnchantments.CRABS_TOUCH,
                    ModEnchantments.STRIDE,
                    ModEnchantments.RETRIEVAL,
                    ModEnchantments.VITALITY
                ),
                1, 1, 10, 5
            ));
        });

        // Level 3: Journeyman - Advanced combat & mining + ink sac for gold + glass
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.LIBRARIAN, 3, factories -> {
            // Sell 5 Ink Sacs for 1 Gold Ingot
            factories.add(createSellItemForGoldTrade(Items.INK_SAC, 5, 1, 12, 10));
            // Buy 4 Glass for 1 Gold Ingot
            factories.add(createBuyItemForGoldTrade(Items.GLASS, 4, 1, 12, 10));
            // Buy Level 1-2 Advanced Enchanted Book for 14 Gold Ingots + 1 Book
            factories.add(createGoldEnchantedBookTrade(
                List.of(
                    ModEnchantments.BREEZE_BURST,
                    ModEnchantments.PYROLYSIS,
                    ModEnchantments.WEBSNARE,
                    ModEnchantments.TOXIC,
                    ModEnchantments.SWIFT_STRIKE
                ),
                1, 2, 14, 10
            ));
        });

        // Level 4: Expert - High tier enchantments + compass & clock
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.LIBRARIAN, 4, factories -> {
            // Sell 2 Writable Books for 1 Gold Ingot
            factories.add(createSellItemForGoldTrade(Items.WRITABLE_BOOK, 2, 1, 12, 15));
            // Buy Compass for 4 Gold Ingots
            factories.add(createBuyItemForGoldTrade(Items.COMPASS, 1, 4, 12, 15));
            // Buy Clock for 4 Gold Ingots
            factories.add(createBuyItemForGoldTrade(Items.CLOCK, 1, 4, 12, 15));
            // Buy Level 1-2 High Tier Enchanted Book for 18 Gold Ingots + 1 Book
            factories.add(createGoldEnchantedBookTrade(
                List.of(
                    ModEnchantments.SCORCH_WALKER,
                    ModEnchantments.CRABS_TOUCH,
                    ModEnchantments.GLUTTONY,
                    ModEnchantments.VITALITY,
                    ModEnchantments.OUTREACH
                ),
                1, 2, 18, 15
            ));
        });

        // Level 5: Master - Max tier custom enchantments + name tag
        TradeOfferHelper.registerVillagerOffers(VillagerProfession.LIBRARIAN, 5, factories -> {
            // Buy Name Tag for 6 Gold Ingots
            factories.add(createBuyItemForGoldTrade(Items.NAME_TAG, 1, 6, 12, 30));
            // Buy Masterwork Enchanted Book for 24 Gold Ingots + 1 Book
            factories.add(createGoldEnchantedBookTrade(
                List.of(
                    ModEnchantments.SCORCH_WALKER,
                    ModEnchantments.CRABS_TOUCH,
                    ModEnchantments.GLUTTONY,
                    ModEnchantments.VITALITY,
                    ModEnchantments.SWIFT_STRIKE
                ),
                2, 5, 24, 30
            ));
        });
    }

    private static TradeOffers.Factory createSellItemForEmeraldTrade(Item item, int count, int maxUses, int exp) {
        return (world, entity, random) -> new TradeOffer(
            new TradedItem(item, count),
            new ItemStack(Items.EMERALD, 1),
            maxUses, exp, 0.05f
        );
    }

    private static TradeOffers.Factory createBuyItemForEmeraldTrade(Item item, int count, int emeraldCount, int maxUses, int exp) {
        return (world, entity, random) -> new TradeOffer(
            new TradedItem(Items.EMERALD, emeraldCount),
            new ItemStack(item, count),
            maxUses, exp, 0.05f
        );
    }

    private static TradeOffers.Factory createSellItemForGoldTrade(Item item, int count, int goldCount, int maxUses, int exp) {
        return (world, entity, random) -> new TradeOffer(
            new TradedItem(item, count),
            new ItemStack(Items.GOLD_INGOT, goldCount),
            maxUses, exp, 0.05f
        );
    }

    private static TradeOffers.Factory createBuyItemForGoldTrade(Item item, int count, int goldCost, int maxUses, int exp) {
        return (world, entity, random) -> new TradeOffer(
            new TradedItem(Items.GOLD_INGOT, goldCost),
            new ItemStack(item, count),
            maxUses, exp, 0.05f
        );
    }

    private static TradeOffers.Factory createEnchantedGoldTrade(Item item, int goldCount, int minLevel, int maxLevel, int exp) {
        return (world, entity, random) -> {
            ItemStack outputStack = new ItemStack(item);
            int enchantLevel = minLevel + random.nextInt(Math.max(1, maxLevel - minLevel + 1));
            outputStack = EnchantmentHelper.enchant(random, outputStack, enchantLevel, world.getRegistryManager(), Optional.empty());
            return new TradeOffer(
                new TradedItem(Items.GOLD_INGOT, goldCount),
                outputStack,
                12, exp, 0.05f
            );
        };
    }

    private static TradeOffers.Factory createHighLevelEnchantedGoldTrade(Item item, int goldCount, int diamondCount, int enchantLevel, int exp) {
        return (world, entity, random) -> {
            ItemStack outputStack = new ItemStack(item);
            outputStack = EnchantmentHelper.enchant(random, outputStack, enchantLevel, world.getRegistryManager(), Optional.empty());
            return new TradeOffer(
                new TradedItem(Items.GOLD_INGOT, goldCount),
                Optional.of(new TradedItem(Items.DIAMOND, diamondCount)),
                outputStack,
                12, exp, 0.05f
            );
        };
    }

    public static TradeOffers.Factory createCustomEnchantedBookTrade(
        List<RegistryKey<Enchantment>> possibleEnchantments,
        int minLevel,
        int maxLevel,
        int baseGold,
        int exp
    ) {
        return createGoldEnchantedBookTrade(possibleEnchantments, minLevel, maxLevel, baseGold, exp);
    }

    public static TradeOffers.Factory createGoldEnchantedBookTrade(
        List<RegistryKey<Enchantment>> possibleEnchantments,
        int minLevel,
        int maxLevel,
        int baseGoldCost,
        int exp
    ) {
        return (world, entity, random) -> {
            Registry<Enchantment> registry = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
            RegistryKey<Enchantment> chosenKey = possibleEnchantments.get(random.nextInt(possibleEnchantments.size()));
            Optional<RegistryEntry.Reference<Enchantment>> entry = registry.getEntry(chosenKey.getValue());
            if (entry.isEmpty()) {
                return null;
            }
            RegistryEntry<Enchantment> enchEntry = entry.get();
            int enchMin = enchEntry.value().getMinLevel();
            int enchMax = enchEntry.value().getMaxLevel();
            int selectedLevel = MathHelper.clamp(
                minLevel + random.nextInt(Math.max(1, maxLevel - minLevel + 1)),
                enchMin,
                enchMax
            );
            ItemStack book = EnchantmentHelper.getEnchantedBookWith(new EnchantmentLevelEntry(enchEntry, selectedLevel));
            int goldPrice = Math.min(64, baseGoldCost + (selectedLevel - 1) * 4 + random.nextInt(3));
            return new TradeOffer(
                new TradedItem(Items.GOLD_INGOT, goldPrice),
                Optional.of(new TradedItem(Items.BOOK, 1)),
                book,
                12, exp, 0.2f
            );
        };
    }
}
