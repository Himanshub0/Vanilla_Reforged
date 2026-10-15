package com.vanillareforged.item;

import com.vanillareforged.VanillaReforgedMod;
import com.vanillareforged.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.List;

public final class ModItems {
    public static final Identifier DIAMOND_CRYSTAL_ID = Identifier.of(VanillaReforgedMod.MOD_ID, "diamond_crystal");
    public static final Identifier DIAMOND_UPGRADE_SMITHING_TEMPLATE_ID = Identifier.of(VanillaReforgedMod.MOD_ID, "diamond_upgrade_smithing_template");

    public static final RegistryKey<Item> DIAMOND_CRYSTAL_KEY = RegistryKey.of(RegistryKeys.ITEM, DIAMOND_CRYSTAL_ID);
    public static final RegistryKey<Item> DIAMOND_UPGRADE_SMITHING_TEMPLATE_KEY = RegistryKey.of(RegistryKeys.ITEM, DIAMOND_UPGRADE_SMITHING_TEMPLATE_ID);

    public static final Item DIAMOND_CRYSTAL = new Item(
        new Item.Settings().registryKey(DIAMOND_CRYSTAL_KEY)
    );

    public static final Item DIAMOND_UPGRADE_SMITHING_TEMPLATE = new SmithingTemplateItem(
        Text.translatable("item.vanilla_reforged.smithing_template.diamond_upgrade.applies_to").formatted(Formatting.BLUE),
        Text.translatable("item.vanilla_reforged.smithing_template.diamond_upgrade.ingredients").formatted(Formatting.BLUE),
        Text.translatable("item.vanilla_reforged.smithing_template.diamond_upgrade.base_slot_description"),
        Text.translatable("item.vanilla_reforged.smithing_template.diamond_upgrade.additions_slot_description"),
        getDiamondUpgradeEmptyBaseSlotTextures(),
        getDiamondUpgradeEmptyAdditionsSlotTextures(),
        new Item.Settings().registryKey(DIAMOND_UPGRADE_SMITHING_TEMPLATE_KEY).rarity(Rarity.RARE)
    );

    public static final Identifier KING_ZOMBIE_SPAWN_EGG_ID = Identifier.of(VanillaReforgedMod.MOD_ID, "king_zombie_spawn_egg");
    public static final Identifier DIRE_WOLF_SPAWN_EGG_ID = Identifier.of(VanillaReforgedMod.MOD_ID, "dire_wolf_spawn_egg");

    public static final RegistryKey<Item> KING_ZOMBIE_SPAWN_EGG_KEY = RegistryKey.of(RegistryKeys.ITEM, KING_ZOMBIE_SPAWN_EGG_ID);
    public static final RegistryKey<Item> DIRE_WOLF_SPAWN_EGG_KEY = RegistryKey.of(RegistryKeys.ITEM, DIRE_WOLF_SPAWN_EGG_ID);

    public static final Item KING_ZOMBIE_SPAWN_EGG = new ModSpawnEggItem(
        com.vanillareforged.entity.ModEntities.KING_ZOMBIE,
        new Item.Settings().registryKey(KING_ZOMBIE_SPAWN_EGG_KEY)
    );

    public static final Item DIRE_WOLF_SPAWN_EGG = new ModSpawnEggItem(
        com.vanillareforged.entity.ModEntities.DIRE_WOLF,
        new Item.Settings().registryKey(DIRE_WOLF_SPAWN_EGG_KEY)
    );

    public static final Identifier GENERAL_GROUP_ID = Identifier.of(VanillaReforgedMod.MOD_ID, "general");
    public static final RegistryKey<ItemGroup> GENERAL_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, GENERAL_GROUP_ID);

    public static final Identifier ENCHANTMENTS_GROUP_ID = Identifier.of(VanillaReforgedMod.MOD_ID, "enchantments");
    public static final RegistryKey<ItemGroup> ENCHANTMENTS_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, ENCHANTMENTS_GROUP_ID);

    public static final List<RegistryKey<Enchantment>> COMBAT_ENCHANTMENTS = List.of(
        // Curses
        ModEnchantments.CURSE_OF_BREAKING,
        ModEnchantments.CURSE_OF_CLUMSINESS,
        ModEnchantments.CURSE_OF_DISPLACEMENT,
        ModEnchantments.CURSE_OF_DOUBLE_EDGE,
        // Weapons & Ranged
        ModEnchantments.BREEZE_BURST,
        ModEnchantments.ICE_ASPECT,
        ModEnchantments.OUTREACH,
        ModEnchantments.SWIFT_STRIKE,
        ModEnchantments.TOXIC,
        ModEnchantments.PRECISION,
        ModEnchantments.WEBSNARE,
        // Combat Armor
        ModEnchantments.KINETIC_PROTECTION,
        ModEnchantments.SKYGUARD,
        ModEnchantments.VITALITY
    );

    public static final List<RegistryKey<Enchantment>> TOOL_ENCHANTMENTS = List.of(
        ModEnchantments.CRABS_TOUCH,
        ModEnchantments.GLUTTONY,
        ModEnchantments.PYROLYSIS,
        ModEnchantments.LUMINOSITY,
        ModEnchantments.RETRIEVAL,
        ModEnchantments.SCORCH_WALKER,
        ModEnchantments.STRIDE,
        ModEnchantments.GRAVIOLE
    );

    public static final List<RegistryKey<Enchantment>> ALL_CUSTOM_ENCHANTMENTS = List.of(
        // Curses
        ModEnchantments.CURSE_OF_BREAKING,
        ModEnchantments.CURSE_OF_CLUMSINESS,
        ModEnchantments.CURSE_OF_DISPLACEMENT,
        ModEnchantments.CURSE_OF_DOUBLE_EDGE,
        // Combat
        ModEnchantments.BREEZE_BURST,
        ModEnchantments.ICE_ASPECT,
        ModEnchantments.OUTREACH,
        ModEnchantments.SWIFT_STRIKE,
        ModEnchantments.TOXIC,
        ModEnchantments.PRECISION,
        ModEnchantments.WEBSNARE,
        // Mining
        ModEnchantments.CRABS_TOUCH,
        ModEnchantments.GLUTTONY,
        ModEnchantments.PYROLYSIS,
        // Armor & Survival
        ModEnchantments.KINETIC_PROTECTION,
        ModEnchantments.LUMINOSITY,
        ModEnchantments.RETRIEVAL,
        ModEnchantments.SCORCH_WALKER,
        ModEnchantments.SKYGUARD,
        ModEnchantments.STRIDE,
        ModEnchantments.VITALITY,
        // Elytra
        ModEnchantments.GRAVIOLE
    );

    // Dedicated Page 1: Items & Entities
    public static final ItemGroup GENERAL_GROUP = FabricItemGroup.builder()
        .icon(() -> new ItemStack(DIAMOND_CRYSTAL))
        .displayName(Text.translatable("itemGroup.vanilla_reforged.general"))
        .entries((displayContext, entries) -> {
            entries.add(DIAMOND_CRYSTAL);
            entries.add(DIAMOND_UPGRADE_SMITHING_TEMPLATE);
            entries.add(KING_ZOMBIE_SPAWN_EGG);
            entries.add(DIRE_WOLF_SPAWN_EGG);
        })
        .build();

    // Dedicated Page 2: Custom Enchanted Books
    public static final ItemGroup ENCHANTMENTS_GROUP = FabricItemGroup.builder()
        .icon(() -> new ItemStack(Items.ENCHANTED_BOOK))
        .displayName(Text.translatable("itemGroup.vanilla_reforged.enchantments"))
        .entries((displayContext, entries) -> {
            displayContext.lookup().getOptional(RegistryKeys.ENCHANTMENT).ifPresent(lookup -> {
                for (RegistryKey<Enchantment> key : ALL_CUSTOM_ENCHANTMENTS) {
                    lookup.getOptional(key).ifPresent(entry -> {
                        int minLevel = entry.value().getMinLevel();
                        int maxLevel = entry.value().getMaxLevel();
                        for (int lvl = minLevel; lvl <= maxLevel; lvl++) {
                            entries.add(EnchantmentHelper.getEnchantedBookWith(new EnchantmentLevelEntry(entry, lvl)));
                        }
                    });
                }
            });
        })
        .build();

    private static List<Identifier> getDiamondUpgradeEmptyBaseSlotTextures() {
        return List.of(
            Identifier.ofVanilla("container/slot/helmet"),
            Identifier.ofVanilla("container/slot/sword"),
            Identifier.ofVanilla("container/slot/chestplate"),
            Identifier.ofVanilla("container/slot/pickaxe"),
            Identifier.ofVanilla("container/slot/leggings"),
            Identifier.ofVanilla("container/slot/axe"),
            Identifier.ofVanilla("container/slot/boots"),
            Identifier.ofVanilla("container/slot/hoe"),
            Identifier.ofVanilla("container/slot/shovel"),
            Identifier.ofVanilla("container/slot/nautilus_armor"),
            Identifier.ofVanilla("container/slot/spear")
        );
    }

    private static List<Identifier> getDiamondUpgradeEmptyAdditionsSlotTextures() {
        return List.of(Identifier.ofVanilla("container/slot/diamond"));
    }


    public static void registerItems() {
        Registry.register(Registries.ITEM_GROUP, GENERAL_GROUP_KEY, GENERAL_GROUP);
        Registry.register(Registries.ITEM_GROUP, ENCHANTMENTS_GROUP_KEY, ENCHANTMENTS_GROUP);

        Registry.register(Registries.ITEM, DIAMOND_CRYSTAL_KEY, DIAMOND_CRYSTAL);
        Registry.register(Registries.ITEM, DIAMOND_UPGRADE_SMITHING_TEMPLATE_KEY, DIAMOND_UPGRADE_SMITHING_TEMPLATE);
        Registry.register(Registries.ITEM, KING_ZOMBIE_SPAWN_EGG_KEY, KING_ZOMBIE_SPAWN_EGG);
        Registry.register(Registries.ITEM, DIRE_WOLF_SPAWN_EGG_KEY, DIRE_WOLF_SPAWN_EGG);

        // Vanilla Ingredients tab
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(DIAMOND_CRYSTAL);
            entries.add(DIAMOND_UPGRADE_SMITHING_TEMPLATE);
        });

        // Vanilla Spawn Eggs tab
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.add(KING_ZOMBIE_SPAWN_EGG);
            entries.add(DIRE_WOLF_SPAWN_EGG);
        });

        // Vanilla Combat tab
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.getContext().lookup().getOptional(RegistryKeys.ENCHANTMENT).ifPresent(lookup -> {
                for (RegistryKey<Enchantment> key : COMBAT_ENCHANTMENTS) {
                    lookup.getOptional(key).ifPresent(entry -> {
                        int minLevel = entry.value().getMinLevel();
                        int maxLevel = entry.value().getMaxLevel();
                        for (int lvl = minLevel; lvl <= maxLevel; lvl++) {
                            entries.add(EnchantmentHelper.getEnchantedBookWith(new EnchantmentLevelEntry(entry, lvl)));
                        }
                    });
                }
            });
        });

        // Vanilla Tools & Utilities tab
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.getContext().lookup().getOptional(RegistryKeys.ENCHANTMENT).ifPresent(lookup -> {
                for (RegistryKey<Enchantment> key : TOOL_ENCHANTMENTS) {
                    lookup.getOptional(key).ifPresent(entry -> {
                        int minLevel = entry.value().getMinLevel();
                        int maxLevel = entry.value().getMaxLevel();
                        for (int lvl = minLevel; lvl <= maxLevel; lvl++) {
                            entries.add(EnchantmentHelper.getEnchantedBookWith(new EnchantmentLevelEntry(entry, lvl)));
                        }
                    });
                }
            });
        });
    }
}
