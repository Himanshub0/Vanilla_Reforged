package com.vanillareforged.mixin;

import com.vanillareforged.balance.SkeletonGroupManager;
import com.vanillareforged.config.ModConfig;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Phase 3 Section 2 & Section 6: Skeleton Group Overhaul & General Hostile Mob Balancing.
 *
 * Rules:
 * - Natural group size: randomly 4 or 5 Skeletons (ModConfig.SKELETON_GROUP_MIN to MAX).
 * - Weapon preference: 60% Bow, 40% Golden Sword.
 * - Group composition rule: At least one bow user and at least one golden sword user per group!
 * - Golden-sword equipped skeletons: active melee AI pursuing and attacking nearby targets.
 * - Spider Jockeys and mounted skeletons: STRICTLY UNCHANGED (vanilla 20 HP, bow only).
 * - General hostile health: 10% maximum health increase (22 HP for standard skeletons/strays).
 */
@Mixin(AbstractSkeletonEntity.class)
public abstract class SkeletonEntityMixin {

    /**
     * General hostiles: 10% maximum health increase on standard skeletons (22 HP).
     */
    @Inject(method = "createAbstractSkeletonAttributes", at = @At("RETURN"), cancellable = true)
    private static void increaseSkeletonMaxHealth(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.setReturnValue(cir.getReturnValue().add(EntityAttributes.MAX_HEALTH, ModConfig.SKELETON_MAX_HEALTH));
    }

    /**
     * Handles group composition, weapon distribution, and jockey protection.
     */
    @Inject(method = "initialize", at = @At("RETURN"), cancellable = true)
    private void onSkeletonInitialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
                                      EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
        AbstractSkeletonEntity abstractSkeleton = (AbstractSkeletonEntity) (Object) this;
        if (!(abstractSkeleton instanceof SkeletonEntity skeleton)) {
            return;
        }

        // Spider Jockeys and other jockeys: STRICTLY UNCHANGED (vanilla 20 HP, bow, vanilla behavior)
        if (skeleton.hasVehicle() || spawnReason == SpawnReason.JOCKEY) {
            EntityAttributeInstance maxHealthAttr = skeleton.getAttributeInstance(EntityAttributes.MAX_HEALTH);
            if (maxHealthAttr != null) {
                maxHealthAttr.setBaseValue(20.0);
                skeleton.setHealth(20.0f);
            }
            return;
        }

        // Group overhaul for natural spawning, chunk generation, and creative spawn egg testing
        if (spawnReason == SpawnReason.NATURAL || spawnReason == SpawnReason.CHUNK_GENERATION || spawnReason == SpawnReason.SPAWN_ITEM_USE) {
            EntityData currentData = cir.getReturnValue();
            if (currentData instanceof SkeletonGroupManager.SkeletonGroupData groupData) {
                // Member of an existing group
                if (groupData.isComplete()) {
                    // Prevent excess duplicate spawns if vanilla spawn loop exceeds group target
                    if (spawnReason != SpawnReason.SPAWN_ITEM_USE) {
                        skeleton.discard();
                    }
                    return;
                }
                SkeletonGroupManager.WeaponType weapon = groupData.getNextWeapon();
                SkeletonGroupManager.equipSkeleton(skeleton, weapon);
            } else {
                // Group Leader: initialize new group with random size (4 or 5)
                SkeletonGroupManager.SkeletonGroupData newGroup = new SkeletonGroupManager.SkeletonGroupData(world.getRandom());
                SkeletonGroupManager.WeaponType leaderWeapon = newGroup.getNextWeapon();
                SkeletonGroupManager.equipSkeleton(skeleton, leaderWeapon);
                cir.setReturnValue(newGroup);

                // Spawn the companions (targetSize - 1) to guarantee group size
                int companionsNeeded = newGroup.getTargetSize() - 1;
                SkeletonGroupManager.spawnCompanions(skeleton, world, difficulty, spawnReason, newGroup, companionsNeeded);
            }
        } else if (spawnReason != SpawnReason.REINFORCEMENT) {
            // For other individual spawns (e.g. /summon, spawners), roll weapon preference (60% Bow, 40% Golden Sword)
            if (world.getRandom().nextDouble() >= ModConfig.SKELETON_BOW_RATIO) {
                SkeletonGroupManager.equipSkeleton(skeleton, SkeletonGroupManager.WeaponType.GOLDEN_SWORD);
            }
        }
    }
}
