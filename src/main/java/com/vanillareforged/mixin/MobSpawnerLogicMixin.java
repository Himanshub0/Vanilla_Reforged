package com.vanillareforged.mixin;

import com.vanillareforged.config.ModConfig;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobSpawnerLogic.class)
public abstract class MobSpawnerLogicMixin {

    @Shadow private int spawnDelay;
    @Shadow private int minSpawnDelay;
    @Shadow private int maxSpawnDelay;
    @Shadow private int spawnCount;
    @Shadow private int maxNearbyEntities;

    /**
     * Buffs spawner blocks to constantly spawn mobs for mob farming:
     * 1. Drastically reduces spawn delay from vanilla 200-800 ticks (10-40s) down to 20-60 ticks (1-3s).
     * 2. Increases nearby entity capacity from 6 to 16 so spawners don't stall in mob farms.
     */
    @Inject(method = "updateSpawns", at = @At("TAIL"))
    private void buffUpdateSpawns(World world, BlockPos pos, CallbackInfo ci) {
        int min = ModConfig.SPAWNER_MIN_DELAY;
        int max = ModConfig.SPAWNER_MAX_DELAY;
        this.spawnDelay = min + (max > min ? world.random.nextInt(max - min + 1) : 0);
        this.minSpawnDelay = min;
        this.maxSpawnDelay = max;
        this.maxNearbyEntities = Math.max(this.maxNearbyEntities, ModConfig.SPAWNER_MAX_NEARBY_ENTITIES);
        this.spawnCount = Math.max(this.spawnCount, ModConfig.SPAWNER_SPAWN_COUNT);
    }

    /**
     * Ensures newly placed, converted, or loaded spawners immediately clamp their delay
     * and do not wait for a full 20-40 second vanilla cycle on first load.
     */
    @Inject(method = "serverTick", at = @At("HEAD"))
    private void clampServerTickDelay(ServerWorld world, BlockPos pos, CallbackInfo ci) {
        if (this.spawnDelay > ModConfig.SPAWNER_MAX_DELAY) {
            this.spawnDelay = ModConfig.SPAWNER_MAX_DELAY;
        }
        if (this.minSpawnDelay > ModConfig.SPAWNER_MIN_DELAY) {
            this.minSpawnDelay = ModConfig.SPAWNER_MIN_DELAY;
        }
        if (this.maxSpawnDelay > ModConfig.SPAWNER_MAX_DELAY) {
            this.maxSpawnDelay = ModConfig.SPAWNER_MAX_DELAY;
        }
        if (this.maxNearbyEntities < ModConfig.SPAWNER_MAX_NEARBY_ENTITIES) {
            this.maxNearbyEntities = ModConfig.SPAWNER_MAX_NEARBY_ENTITIES;
        }
    }
}
