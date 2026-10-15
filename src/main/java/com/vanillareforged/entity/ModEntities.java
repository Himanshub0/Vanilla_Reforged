package com.vanillareforged.entity;

import com.vanillareforged.VanillaReforgedMod;
import com.vanillareforged.config.ModConfig;
import com.vanillareforged.mixin.SpawnRestrictionAccessor;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.BiomeKeys;

public final class ModEntities {
    // King Zombie
    public static final Identifier KING_ZOMBIE_ID = Identifier.of(VanillaReforgedMod.MOD_ID, "king_zombie");
    public static final RegistryKey<EntityType<?>> KING_ZOMBIE_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, KING_ZOMBIE_ID);

    // King Zombie: Scaled by 1.5x via EntityAttributes.SCALE.
    // Changing base dimensions (0.6f width, 1.95f height, 1.74f eye height) scale dynamically
    // via EntityAttributes.SCALE to 0.9f width x 2.925f height (2.61f eye height),
    // precisely matching the giant 1.5x King Zombie model.
    public static final EntityType<KingZombieEntity> KING_ZOMBIE = FabricEntityTypeBuilder.create(
            SpawnGroup.MONSTER, KingZombieEntity::new
        )
        .dimensions(EntityDimensions.changing(0.6f, 1.95f).withEyeHeight(1.74f))
        .trackRangeBlocks(40)
        .build(KING_ZOMBIE_KEY);

    // Dire Wolf: Scaled by 2.0x via EntityAttributes.SCALE.
    // Base dimensions (0.9f width, 0.95f height) expand to 1.8f width x 1.9f height,
    // seamlessly encompassing the giant 2.0x wolf model from snout to tail.
    public static final Identifier DIRE_WOLF_ID = Identifier.of(VanillaReforgedMod.MOD_ID, "dire_wolf");
    public static final RegistryKey<EntityType<?>> DIRE_WOLF_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, DIRE_WOLF_ID);

    public static final EntityType<DireWolfEntity> DIRE_WOLF = FabricEntityTypeBuilder.create(
            SpawnGroup.CREATURE, DireWolfEntity::new
        )
        .dimensions(EntityDimensions.changing(0.9f, 0.95f).withEyeHeight(0.85f))
        .trackRangeBlocks(32)
        .build(DIRE_WOLF_KEY);

    public static void registerEntities() {
        // Register King Zombie
        Registry.register(Registries.ENTITY_TYPE, KING_ZOMBIE_KEY, KING_ZOMBIE);
        FabricDefaultAttributeRegistry.register(KING_ZOMBIE, KingZombieEntity.createKingZombieAttributes());

        // Register Dire Wolf
        Registry.register(Registries.ENTITY_TYPE, DIRE_WOLF_KEY, DIRE_WOLF);
        FabricDefaultAttributeRegistry.register(DIRE_WOLF, DireWolfEntity.createDireWolfAttributes());

        // Spawn restrictions: Dire Wolf spawns on ground like wolves
        SpawnRestrictionAccessor.callRegister(
            DIRE_WOLF,
            SpawnLocationTypes.ON_GROUND,
            Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
            AnimalEntity::isValidNaturalSpawn
        );

        // Biome Spawns: All vanilla wolf biomes (Forest, Taiga, Snowy Taiga, Old Growth Taiga, Jungle, Savanna, Badlands, Grove)
        // plus cold/alpine peaks where Dire Wolves naturally roam
        BiomeModifications.addSpawn(
            BiomeSelectors.spawnsOneOf(EntityType.WOLF).or(
                BiomeSelectors.includeByKey(
                    BiomeKeys.SNOWY_PLAINS,
                    BiomeKeys.SNOWY_TAIGA,
                    BiomeKeys.SNOWY_SLOPES,
                    BiomeKeys.FROZEN_PEAKS,
                    BiomeKeys.JAGGED_PEAKS,
                    BiomeKeys.GROVE
                )
            ),
            SpawnGroup.CREATURE,
            DIRE_WOLF,
            ModConfig.DIRE_WOLF_SPAWN_WEIGHT,
            1,
            2
        );
    }
}
