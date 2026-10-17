package com.vanillareforged.sandbox.pocketrepose.item;

import com.vanillareforged.sandbox.pocketrepose.PocketReposeMod;
import com.vanillareforged.sandbox.pocketrepose.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModItems {

    public static final Identifier KEYSTONE_ID = Identifier.of(PocketReposeMod.MOD_ID, "keystone");
    public static final RegistryKey<Item> KEYSTONE_KEY = RegistryKey.of(RegistryKeys.ITEM, KEYSTONE_ID);

    public static final KeystoneItem KEYSTONE = Registry.register(
        Registries.ITEM,
        KEYSTONE_KEY,
        new KeystoneItem(new Item.Settings().registryKey(KEYSTONE_KEY).maxCount(1).rarity(Rarity.RARE))
    );

    public static final BlockItem SUITCASE = registerSuitcaseItem("suitcase", ModBlocks.SUITCASE);
    public static final BlockItem WHITE_SUITCASE = registerSuitcaseItem("white_suitcase", ModBlocks.WHITE_SUITCASE);
    public static final BlockItem ORANGE_SUITCASE = registerSuitcaseItem("orange_suitcase", ModBlocks.ORANGE_SUITCASE);
    public static final BlockItem MAGENTA_SUITCASE = registerSuitcaseItem("magenta_suitcase", ModBlocks.MAGENTA_SUITCASE);
    public static final BlockItem LIGHT_BLUE_SUITCASE = registerSuitcaseItem("light_blue_suitcase", ModBlocks.LIGHT_BLUE_SUITCASE);
    public static final BlockItem YELLOW_SUITCASE = registerSuitcaseItem("yellow_suitcase", ModBlocks.YELLOW_SUITCASE);
    public static final BlockItem LIME_SUITCASE = registerSuitcaseItem("lime_suitcase", ModBlocks.LIME_SUITCASE);
    public static final BlockItem PINK_SUITCASE = registerSuitcaseItem("pink_suitcase", ModBlocks.PINK_SUITCASE);
    public static final BlockItem GRAY_SUITCASE = registerSuitcaseItem("gray_suitcase", ModBlocks.GRAY_SUITCASE);
    public static final BlockItem LIGHT_GRAY_SUITCASE = registerSuitcaseItem("light_gray_suitcase", ModBlocks.LIGHT_GRAY_SUITCASE);
    public static final BlockItem CYAN_SUITCASE = registerSuitcaseItem("cyan_suitcase", ModBlocks.CYAN_SUITCASE);
    public static final BlockItem PURPLE_SUITCASE = registerSuitcaseItem("purple_suitcase", ModBlocks.PURPLE_SUITCASE);
    public static final BlockItem BLUE_SUITCASE = registerSuitcaseItem("blue_suitcase", ModBlocks.BLUE_SUITCASE);
    public static final BlockItem BROWN_SUITCASE = registerSuitcaseItem("brown_suitcase", ModBlocks.BROWN_SUITCASE);
    public static final BlockItem GREEN_SUITCASE = registerSuitcaseItem("green_suitcase", ModBlocks.GREEN_SUITCASE);
    public static final BlockItem RED_SUITCASE = registerSuitcaseItem("red_suitcase", ModBlocks.RED_SUITCASE);
    public static final BlockItem BLACK_SUITCASE = registerSuitcaseItem("black_suitcase", ModBlocks.BLACK_SUITCASE);
    public static final BlockItem SECRET_BARREL = registerSuitcaseItem("secret_barrel", ModBlocks.SECRET_BARREL);

    public static final Identifier PORTAL_ITEM_ID = Identifier.of(PocketReposeMod.MOD_ID, "portal");
    public static final RegistryKey<Item> PORTAL_ITEM_KEY = RegistryKey.of(RegistryKeys.ITEM, PORTAL_ITEM_ID);

    public static final BlockItem PORTAL = Registry.register(
        Registries.ITEM,
        PORTAL_ITEM_KEY,
        new BlockItem(ModBlocks.PORTAL, new Item.Settings().registryKey(PORTAL_ITEM_KEY).maxCount(64).rarity(Rarity.EPIC))
    );

    public static final Identifier ITEM_GROUP_ID = Identifier.of(PocketReposeMod.MOD_ID, "general");
    public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, ITEM_GROUP_ID);

    public static final ItemGroup ITEM_GROUP = Registry.register(
        Registries.ITEM_GROUP,
        ITEM_GROUP_KEY,
        FabricItemGroup.builder()
            .icon(() -> new ItemStack(SUITCASE))
            .displayName(Text.literal("Pocket Repose"))
            .entries((context, entries) -> {
                entries.add(SUITCASE);
                entries.add(WHITE_SUITCASE);
                entries.add(ORANGE_SUITCASE);
                entries.add(MAGENTA_SUITCASE);
                entries.add(LIGHT_BLUE_SUITCASE);
                entries.add(YELLOW_SUITCASE);
                entries.add(LIME_SUITCASE);
                entries.add(PINK_SUITCASE);
                entries.add(GRAY_SUITCASE);
                entries.add(LIGHT_GRAY_SUITCASE);
                entries.add(CYAN_SUITCASE);
                entries.add(PURPLE_SUITCASE);
                entries.add(BLUE_SUITCASE);
                entries.add(BROWN_SUITCASE);
                entries.add(GREEN_SUITCASE);
                entries.add(RED_SUITCASE);
                entries.add(BLACK_SUITCASE);
                entries.add(SECRET_BARREL);
                entries.add(KEYSTONE);
                entries.add(PORTAL);
            })
            .build()
    );

    private static BlockItem registerSuitcaseItem(String path, Block block) {
        Identifier id = Identifier.of(PocketReposeMod.MOD_ID, path);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        return Registry.register(
            Registries.ITEM,
            key,
            new BlockItem(block, new Item.Settings().registryKey(key).maxCount(1).rarity(Rarity.UNCOMMON))
        );
    }

    public static void registerItems() {
        // Loads class and triggers registrations
    }
}
