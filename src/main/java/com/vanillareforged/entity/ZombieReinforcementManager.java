package com.vanillareforged.entity;

import com.vanillareforged.config.ModConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Phase 3 Section 3: Controlled Reinforcement System
 * - 40% chance (ModConfig.ZOMBIE_REINFORCEMENT_CHANCE) per eligible combat check when damaged by player.
 * - 10-second cooldown (ModConfig.ZOMBIE_REINFORCEMENT_COOLDOWN_TICKS) per source zombie.
 * - Max 4 active reinforcements (ModConfig.ZOMBIE_MAX_REINFORCEMENTS) attributed to a single source zombie. Track attribution.
 * - 1% chance (ModConfig.KING_ZOMBIE_REINFORCEMENT_CHANCE) per successfully generated reinforcement to spawn a King Zombie instead of standard zombie!
 *   (If naturally generated, 5% chance to be Diamond King).
 * - Reinforced zombies and King's Call zombies MUST NOT trigger recursive reinforcement chains.
 */
public final class ZombieReinforcementManager {

    private static final Map<UUID, Long> LAST_REINFORCEMENT_TICK = new HashMap<>();
    private static final Map<UUID, Set<UUID>> ATTRIBUTED_REINFORCEMENTS = new HashMap<>();

    private ZombieReinforcementManager() {}

    public static void checkAndTriggerReinforcement(ZombieEntity source, ServerWorld world, DamageSource damageSource) {
        // Exclude King Zombie (King Zombie has King's Call boss mechanic)
        if (source instanceof KingZombieEntity) {
            return;
        }

        // Exclude Baby Zombies from summoning reinforcements
        if (source.isBaby()) {
            return;
        }

        // Must be damaged by a player
        if (!(damageSource.getAttacker() instanceof PlayerEntity player)) {
            return;
        }

        // Reinforced zombies and King's Call zombies MUST NOT trigger recursive reinforcement chains
        if (source.getCommandTags().contains("vanilla_reforged:wave_zombie") ||
            source.getCommandTags().contains("vanilla_reforged:reinforcement") ||
            source.getCommandTags().contains("vanilla_reforged:no_reinforcement")) {
            return;
        }

        UUID sourceId = source.getUuid();
        long currentTick = world.getTime();

        // Prune stale entries to prevent memory leaks
        if (LAST_REINFORCEMENT_TICK.size() > 200) {
            LAST_REINFORCEMENT_TICK.entrySet().removeIf(e -> (currentTick - e.getValue()) > ModConfig.ZOMBIE_REINFORCEMENT_COOLDOWN_TICKS * 5);
        }
        if (ATTRIBUTED_REINFORCEMENTS.size() > 200) {
            ATTRIBUTED_REINFORCEMENTS.entrySet().removeIf(e -> e.getValue().isEmpty());
        }

        // 1. Cooldown check: 10-second cooldown per source zombie
        Long lastTick = LAST_REINFORCEMENT_TICK.get(sourceId);
        if (lastTick != null && (currentTick - lastTick) < ModConfig.ZOMBIE_REINFORCEMENT_COOLDOWN_TICKS) {
            return;
        }

        // 2. Active reinforcements check: Max 4 active reinforcements attributed to a single source zombie
        Set<UUID> active = ATTRIBUTED_REINFORCEMENTS.computeIfAbsent(sourceId, k -> new HashSet<>());
        active.removeIf(uuid -> {
            Entity entity = world.getEntity(uuid);
            return entity == null || !entity.isAlive();
        });

        if (active.size() >= ModConfig.ZOMBIE_MAX_REINFORCEMENTS) {
            return;
        }

        // 3. 40% chance per eligible combat check when damaged by player
        if (world.getRandom().nextDouble() >= ModConfig.ZOMBIE_REINFORCEMENT_CHANCE) {
            return;
        }

        // Valid reinforcement event triggered
        LAST_REINFORCEMENT_TICK.put(sourceId, currentTick);

        BlockPos targetPos = findReinforcementSpawnPos(world, source.getBlockPos());
        if (targetPos == null) {
            return;
        }

        // 4. 1% chance to spawn a King Zombie instead of standard zombie
        boolean spawnKing = world.getRandom().nextDouble() < ModConfig.KING_ZOMBIE_REINFORCEMENT_CHANCE;

        if (spawnKing && ModEntities.KING_ZOMBIE != null) {
            KingZombieEntity king = ModEntities.KING_ZOMBIE.create(world, SpawnReason.REINFORCEMENT);
            if (king != null) {
                king.refreshPositionAndAngles(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, world.getRandom().nextFloat() * 360f, 0f);
                king.initialize(world, world.getLocalDifficulty(targetPos), SpawnReason.REINFORCEMENT, null);

                king.addCommandTag("vanilla_reforged:reinforcement");
                king.addCommandTag("vanilla_reforged:no_reinforcement");
                king.setTarget(player);

                world.spawnEntityAndPassengers(king);
                active.add(king.getUuid());

                world.playSound(null, targetPos, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, SoundCategory.HOSTILE, 1.5f, 0.7f);
                world.spawnParticles(ParticleTypes.SOUL, king.getX(), king.getY() + 1.0, king.getZ(), 40, 0.8, 1.0, 0.8, 0.05);
            }
        } else {
            ZombieEntity minion = EntityType.ZOMBIE.create(world, SpawnReason.REINFORCEMENT);
            if (minion != null) {
                minion.refreshPositionAndAngles(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, world.getRandom().nextFloat() * 360f, 0f);
                minion.initialize(world, world.getLocalDifficulty(targetPos), SpawnReason.REINFORCEMENT, null);

                minion.addCommandTag("vanilla_reforged:reinforcement");
                minion.addCommandTag("vanilla_reforged:no_reinforcement");
                minion.setTarget(player);

                world.spawnEntityAndPassengers(minion);
                active.add(minion.getUuid());

                world.playSound(null, targetPos, SoundEvents.ENTITY_ZOMBIE_AMBIENT, SoundCategory.HOSTILE, 1.0f, 0.9f);
                world.spawnParticles(ParticleTypes.ANGRY_VILLAGER, minion.getX(), minion.getY() + 1.0, minion.getZ(), 15, 0.5, 0.5, 0.5, 0.02);
            }
        }
    }

    private static BlockPos findReinforcementSpawnPos(ServerWorld world, BlockPos origin) {
        for (int attempts = 0; attempts < 15; attempts++) {
            int dx = world.getRandom().nextBetween(-12, 12);
            int dz = world.getRandom().nextBetween(-12, 12);
            if (Math.abs(dx) < 3 && Math.abs(dz) < 3) continue;

            for (int dy = -3; dy <= 3; dy++) {
                BlockPos pos = origin.add(dx, dy, dz);
                if (world.getBlockState(pos.down()).blocksMovement() &&
                    world.getBlockState(pos).isAir() &&
                    world.getBlockState(pos.up()).isAir()) {
                    return pos;
                }
            }
        }
        return null;
    }

    public static void cleanupOnEntityDeath(UUID uuid) {
        LAST_REINFORCEMENT_TICK.remove(uuid);
        ATTRIBUTED_REINFORCEMENTS.remove(uuid);
    }
}
