package com.vanillareforged.mixin;

import com.vanillareforged.balance.SkeletonGroupManager;
import com.vanillareforged.enchantment.EnchantmentHelper;
import com.vanillareforged.enchantment.ModEnchantments;
import com.vanillareforged.entity.KingZombieEntity;
import com.vanillareforged.entity.ZombieArmorManager;
import com.vanillareforged.entity.ZombieReinforcementManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.SkeletonEntity;
import com.vanillareforged.config.ModConfig;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /**
     * Handles damage reduction for:
     * 1. Kinetic Protection (Helmet vs Elytra kinetic damage / FLY_INTO_WALL)
     * 2. Skyguard (Elytra protection vs all damage)
     */
    @ModifyVariable(
        method = "modifyAppliedDamage",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private float modifyArmorEnchantmentDamage(float amount, DamageSource source) {
        LivingEntity entity = (LivingEntity) (Object) this;
        World world = entity.getEntityWorld();
        if (world == null) {
            return amount;
        }

        // 1. Kinetic Protection (Helmet) vs FLY_INTO_WALL
        if (source.isOf(DamageTypes.FLY_INTO_WALL)) {
            int kineticLevel = EnchantmentHelper.getEquipmentLevel(
                ModEnchantments.KINETIC_PROTECTION,
                entity,
                EquipmentSlot.HEAD
            );
            if (kineticLevel > 0) {
                // 20% reduction per level, up to 80% at level IV
                float reduction = Math.min(0.80f, kineticLevel * 0.20f);
                amount *= (1.0f - reduction);
            }
        }

        // 2. Skyguard: Protection for Elytra (all damage types)
        ItemStack chest = entity.getEquippedStack(EquipmentSlot.CHEST);
        if (chest.isOf(Items.ELYTRA)) {
            int skyguardLevel = EnchantmentHelper.getLevel(
                ModEnchantments.SKYGUARD,
                chest,
                entity
            );
            if (skyguardLevel > 0) {
                // 4% damage reduction per level (similar to Protection enchantment on armor, up to 16% at level IV)
                float reduction = Math.min(0.20f, skyguardLevel * 0.04f);
                amount *= (1.0f - reduction);
            }
        }

        return amount;
    }

    /**
     * Prevents villagers from violently shoving each other into walls, corners,
     * and doorway bottlenecks, eliminating entity-cramming vibration and deadlocks.
     */
    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    private void reduceVillagerCrowdPushing(Entity other, CallbackInfo ci) {
        if ((Object) this instanceof VillagerEntity && other instanceof VillagerEntity) {
            ci.cancel();
        }
    }

    /**
     * Handles Phase 3 Section 7.1 Golden Sword death drop for equipped skeletons
     * and Section 3.3 / 13.1 Armor death drop for armored ordinary zombies:
     * 8.5% chance, randomized durability, no looting bonus, no double drops.
     */
    @Inject(method = "dropEquipment", at = @At("HEAD"))
    private void dropCustomPhase3Equipment(ServerWorld world, DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        if ((Object) this instanceof SkeletonEntity skeleton) {
            SkeletonGroupManager.handleGoldenSwordDeathDrop(skeleton, world);
        } else if ((Object) this instanceof ZombieEntity zombie && !(zombie instanceof KingZombieEntity)) {
            ZombieArmorManager.handleZombieArmorDeathDrop(zombie, world);
            ZombieReinforcementManager.cleanupOnEntityDeath(zombie.getUuid());
        }
    }

    /**
     * Boosts experience drops:
     * 1. All hostile mobs (entities implementing Monster) drop 20% more XP.
     * 2. Zombie Piglins drop additional base XP (buffed from vanilla 5 up to 12).
     * Passive mobs are 100% untouched.
     */
    @Inject(method = "getExperienceToDrop(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;)I", at = @At("RETURN"), cancellable = true)
    private void boostHostileAndPiglinXp(ServerWorld world, Entity attacker, CallbackInfoReturnable<Integer> cir) {
        int originalXp = cir.getReturnValue();
        if (originalXp <= 0) {
            return;
        }
        LivingEntity entity = (LivingEntity) (Object) this;

        // Only apply to hostile / monster mobs
        if (entity instanceof Monster) {
            int xp = originalXp;

            // Zombie Piglins drop additional base XP
            if (entity instanceof ZombifiedPiglinEntity) {
                xp += Math.max(0, ModConfig.ZOMBIFIED_PIGLIN_BASE_XP - 5);
            }

            // All hostile mobs drop 20% more XP
            int boosted = (int) Math.ceil(xp * ModConfig.HOSTILE_MOB_XP_MULTIPLIER);
            cir.setReturnValue(boosted);
        }
    }
}
