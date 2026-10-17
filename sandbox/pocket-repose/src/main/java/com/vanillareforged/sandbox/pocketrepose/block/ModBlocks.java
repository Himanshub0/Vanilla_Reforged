package com.vanillareforged.sandbox.pocketrepose.block;

import com.vanillareforged.sandbox.pocketrepose.PocketReposeMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.List;

public class ModBlocks {

    public static final Identifier SUITCASE_ID = Identifier.of(PocketReposeMod.MOD_ID, "suitcase");
    public static final RegistryKey<Block> SUITCASE_KEY = RegistryKey.of(RegistryKeys.BLOCK, SUITCASE_ID);

    public static final SuitcaseBlock SUITCASE = registerSuitcase("suitcase");
    public static final SuitcaseBlock WHITE_SUITCASE = registerSuitcase("white_suitcase");
    public static final SuitcaseBlock ORANGE_SUITCASE = registerSuitcase("orange_suitcase");
    public static final SuitcaseBlock MAGENTA_SUITCASE = registerSuitcase("magenta_suitcase");
    public static final SuitcaseBlock LIGHT_BLUE_SUITCASE = registerSuitcase("light_blue_suitcase");
    public static final SuitcaseBlock YELLOW_SUITCASE = registerSuitcase("yellow_suitcase");
    public static final SuitcaseBlock LIME_SUITCASE = registerSuitcase("lime_suitcase");
    public static final SuitcaseBlock PINK_SUITCASE = registerSuitcase("pink_suitcase");
    public static final SuitcaseBlock GRAY_SUITCASE = registerSuitcase("gray_suitcase");
    public static final SuitcaseBlock LIGHT_GRAY_SUITCASE = registerSuitcase("light_gray_suitcase");
    public static final SuitcaseBlock CYAN_SUITCASE = registerSuitcase("cyan_suitcase");
    public static final SuitcaseBlock PURPLE_SUITCASE = registerSuitcase("purple_suitcase");
    public static final SuitcaseBlock BLUE_SUITCASE = registerSuitcase("blue_suitcase");
    public static final SuitcaseBlock BROWN_SUITCASE = registerSuitcase("brown_suitcase");
    public static final SuitcaseBlock GREEN_SUITCASE = registerSuitcase("green_suitcase");
    public static final SuitcaseBlock RED_SUITCASE = registerSuitcase("red_suitcase");
    public static final SuitcaseBlock BLACK_SUITCASE = registerSuitcase("black_suitcase");
    public static final CustomSuitcaseBlock SECRET_BARREL = registerSecretBarrel("secret_barrel");

    public static final List<Block> ALL_SUITCASE_BLOCKS = List.of(
        SUITCASE,
        WHITE_SUITCASE,
        ORANGE_SUITCASE,
        MAGENTA_SUITCASE,
        LIGHT_BLUE_SUITCASE,
        YELLOW_SUITCASE,
        LIME_SUITCASE,
        PINK_SUITCASE,
        GRAY_SUITCASE,
        LIGHT_GRAY_SUITCASE,
        CYAN_SUITCASE,
        PURPLE_SUITCASE,
        BLUE_SUITCASE,
        BROWN_SUITCASE,
        GREEN_SUITCASE,
        RED_SUITCASE,
        BLACK_SUITCASE,
        SECRET_BARREL
    );

    public static final Identifier PORTAL_ID = Identifier.of(PocketReposeMod.MOD_ID, "portal");
    public static final RegistryKey<Block> PORTAL_KEY = RegistryKey.of(RegistryKeys.BLOCK, PORTAL_ID);

    public static final PocketPortalBlock PORTAL = Registry.register(
        Registries.BLOCK,
        PORTAL_KEY,
        new PocketPortalBlock(AbstractBlock.Settings.create()
            .registryKey(PORTAL_KEY)
            .noCollision()
            .breakInstantly()
            .luminance(state -> 11)
            .sounds(BlockSoundGroup.GLASS)
            .nonOpaque())
    );

    private static SuitcaseBlock registerSuitcase(String path) {
        Identifier id = Identifier.of(PocketReposeMod.MOD_ID, path);
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id);
        return Registry.register(
            Registries.BLOCK,
            key,
            new SuitcaseBlock(AbstractBlock.Settings.copy(Blocks.CHEST)
                .registryKey(key)
                .strength(2.0f, 6.0f)
                .sounds(BlockSoundGroup.WOOD)
                .nonOpaque()
                .pistonBehavior(PistonBehavior.BLOCK))
        );
    }

    private static CustomSuitcaseBlock registerSecretBarrel(String path) {
        Identifier id = Identifier.of(PocketReposeMod.MOD_ID, path);
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id);
        return Registry.register(
            Registries.BLOCK,
            key,
            new CustomSuitcaseBlock(AbstractBlock.Settings.copy(Blocks.BARREL)
                .registryKey(key)
                .strength(2.0f, 6.0f)
                .sounds(BlockSoundGroup.WOOD)
                .nonOpaque()
                .pistonBehavior(PistonBehavior.BLOCK))
        );
    }

    public static void registerBlocks() {
        // Loads class and triggers static registrations
    }
}
