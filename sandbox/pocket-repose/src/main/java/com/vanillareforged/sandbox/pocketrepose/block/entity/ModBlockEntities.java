package com.vanillareforged.sandbox.pocketrepose.block.entity;

import com.vanillareforged.sandbox.pocketrepose.PocketReposeMod;
import com.vanillareforged.sandbox.pocketrepose.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

    public static final BlockEntityType<SuitcaseBlockEntity> SUITCASE_BLOCK_ENTITY = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        Identifier.of(PocketReposeMod.MOD_ID, "suitcase"),
        FabricBlockEntityTypeBuilder.create(SuitcaseBlockEntity::new, ModBlocks.ALL_SUITCASE_BLOCKS.toArray(new Block[0])).build()
    );

    public static void registerBlockEntities() {
        // Triggers static initialization
    }
}
