package com.vanillareforged.balance;

import com.vanillareforged.config.ModConfig;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;

import java.util.ArrayList;
import java.util.List;

/**
 * Phase 3 Section 6 & Section 7: Skeleton Group Overhaul & Equipment Balancing.
 *
 * Rules:
 * - Natural group size: randomly 4 or 5 Skeletons (ModConfig.SKELETON_GROUP_MIN to MAX).
 * - Weapon preference: 60% Bow, 40% Golden Sword.
 * - Group composition rule: At least one bow user and at least one golden sword user per group!
 * - Golden-sword equipped skeletons:
 *     - Active melee AI (MeleeAttackGoal), pursuing and attacking nearby targets.
 *     - 8.5% chance (ModConfig.SKELETON_GOLDEN_SWORD_DROP_CHANCE) on death to drop golden sword
 *       with randomized durability (no looting bonus).
 * - Spider Jockeys & mounted skeletons: STRICTLY UNCHANGED (do not modify or give golden swords).
 */
public class SkeletonGroupManager {

    public enum WeaponType {
        BOW,
        GOLDEN_SWORD
    }

    public static class SkeletonGroupData implements EntityData {
        private final int targetSize;
        private final List<WeaponType> weapons;
        private int currentIndex = 0;

        public SkeletonGroupData(Random random) {
            int min = ModConfig.SKELETON_GROUP_MIN;
            int max = ModConfig.SKELETON_GROUP_MAX;
            this.targetSize = min + random.nextInt(max - min + 1);
            this.weapons = new ArrayList<>(this.targetSize);

            // Roll weapon preference: 60% Bow, 40% Golden Sword
            for (int i = 0; i < this.targetSize; i++) {
                if (random.nextDouble() < ModConfig.SKELETON_BOW_RATIO) {
                    this.weapons.add(WeaponType.BOW);
                } else {
                    this.weapons.add(WeaponType.GOLDEN_SWORD);
                }
            }

            // Group composition rule: At least one bow and at least one golden sword per group!
            // This rule strictly overrides the random weapon preference.
            boolean hasBow = this.weapons.contains(WeaponType.BOW);
            boolean hasSword = this.weapons.contains(WeaponType.GOLDEN_SWORD);

            if (!hasBow) {
                // If all swords, replace a random one with Bow
                int idx = random.nextInt(this.targetSize);
                this.weapons.set(idx, WeaponType.BOW);
            } else if (!hasSword) {
                // If all bows, replace a random one with Golden Sword
                int idx = random.nextInt(this.targetSize);
                this.weapons.set(idx, WeaponType.GOLDEN_SWORD);
            }
        }

        public int getTargetSize() {
            return this.targetSize;
        }

        public List<WeaponType> getWeapons() {
            return new ArrayList<>(this.weapons);
        }

        public synchronized WeaponType getNextWeapon() {
            if (this.currentIndex < this.weapons.size()) {
                return this.weapons.get(this.currentIndex++);
            }
            return WeaponType.BOW;
        }

        public synchronized boolean isComplete() {
            return this.currentIndex >= this.targetSize;
        }
    }

    /**
     * Equips a skeleton with the designated weapon type and updates its AI attack type.
     */
    public static void equipSkeleton(SkeletonEntity skeleton, WeaponType weapon) {
        if (weapon == WeaponType.GOLDEN_SWORD) {
            ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
            skeleton.equipStack(EquipmentSlot.MAINHAND, sword);
            // Disable vanilla drop equipment so custom 8.5% drop logic controls it without Looting bonus
            skeleton.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0f);
            skeleton.updateAttackType();
        } else {
            skeleton.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            skeleton.updateAttackType();
        }
    }

    /**
     * Spawns companions around the group leader to fulfill natural group size (4 or 5).
     */
    public static void spawnCompanions(SkeletonEntity leader, ServerWorldAccess world, LocalDifficulty difficulty,
                                       SpawnReason spawnReason, SkeletonGroupData groupData, int count) {
        Random random = world.getRandom();

        for (int i = 0; i < count; i++) {
            // Radial dispersion around leader to guarantee safe clearance
            double angle = (2.0 * Math.PI * i) / Math.max(1, count) + (random.nextDouble() * 0.4 - 0.2);
            double dist = 1.0 + random.nextDouble() * 1.5;
            double targetX = leader.getX() + Math.cos(angle) * dist;
            double targetZ = leader.getZ() + Math.sin(angle) * dist;
            double targetY = leader.getY();

            SkeletonEntity companion = EntityType.SKELETON.create(leader.getEntityWorld(), spawnReason);
            if (companion != null) {
                companion.refreshPositionAndAngles(
                    targetX,
                    targetY,
                    targetZ,
                    random.nextFloat() * 360.0f,
                    0.0f
                );
                // Initialize companion with ongoing group data
                companion.initialize(world, difficulty, spawnReason, groupData);
                world.spawnEntityAndPassengers(companion);
            }
        }
    }

    /**
     * Handles death drop for golden-sword equipped skeletons:
     * - 8.5% chance (ModConfig.SKELETON_GOLDEN_SWORD_DROP_CHANCE)
     * - Randomized remaining durability
     * - No bonus from Looting
     */
    public static void handleGoldenSwordDeathDrop(SkeletonEntity skeleton, ServerWorld world) {
        ItemStack mainHand = skeleton.getEquippedStack(EquipmentSlot.MAINHAND);
        if (mainHand.isOf(Items.GOLDEN_SWORD)) {
            // Zero out drop chance so vanilla dropEquipment doesn't double-drop or apply Looting
            skeleton.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0f);

            if (world.getRandom().nextDouble() < ModConfig.SKELETON_GOLDEN_SWORD_DROP_CHANCE) {
                ItemStack dropStack = new ItemStack(Items.GOLDEN_SWORD);
                int maxDmg = dropStack.getMaxDamage();
                if (maxDmg > 1) {
                    // Random remaining durability (damage from 1 to maxDmg - 1)
                    dropStack.setDamage(1 + world.getRandom().nextInt(maxDmg - 1));
                }
                skeleton.dropStack(world, dropStack);
            }
        }
    }
}
