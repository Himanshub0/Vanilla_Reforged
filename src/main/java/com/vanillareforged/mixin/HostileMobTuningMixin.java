package com.vanillareforged.mixin;

import com.vanillareforged.config.ModConfig;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Phase 3 Section 2 & Section 9: General Hostile Mob Tuning.
 *
 * Rules:
 * - 10% maximum health increase on standard hostiles (Creeper 22, Witch 29, Stray 22, Drowned 22, Husk 22, etc.).
 * - Do not increase attack damage.
 * - STRICTLY PROTECTED MOBS (ZERO CHANGES):
 *     - Ghasts: 100% UNTOUCHED.
 *     - Piglin Brutes: 100% UNTOUCHED.
 *     - Spider Jockeys & mounted hostiles: 100% UNTOUCHED.
 *     - Ordinary Wolves & passive animals: 100% UNTOUCHED.
 */
@Mixin(MobEntity.class)
public abstract class HostileMobTuningMixin {

    @Inject(method = "initialize", at = @At("RETURN"))
    private void tuneHostileHealth(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
                                   EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
        MobEntity entity = (MobEntity) (Object) this;
        EntityType<?> type = entity.getType();

        // STRICTLY PROTECTED MOBS: Ghasts, Piglin Brutes, and all Jockey / Mounted hostiles
        if (type == EntityType.GHAST || type == EntityType.PIGLIN_BRUTE) {
            return;
        }
        if (entity.hasVehicle() || spawnReason == SpawnReason.JOCKEY) {
            return;
        }

        // Apply 10% health increase to standard hostiles
        if (type == EntityType.DROWNED) {
            applyMaxHealth(entity, ModConfig.DROWNED_MAX_HEALTH);
        } else if (type == EntityType.HUSK) {
            applyMaxHealth(entity, ModConfig.HUSK_MAX_HEALTH);
        } else if (type == EntityType.STRAY) {
            applyMaxHealth(entity, ModConfig.STRAY_MAX_HEALTH);
        } else if (type == EntityType.SILVERFISH) {
            applyMaxHealth(entity, ModConfig.SILVERFISH_MAX_HEALTH);
        } else if (type == EntityType.ENDERMITE) {
            applyMaxHealth(entity, ModConfig.ENDERMITE_MAX_HEALTH);
        } else if (type == EntityType.PHANTOM) {
            applyMaxHealth(entity, ModConfig.PHANTOM_MAX_HEALTH);
        } else if (type == EntityType.PILLAGER) {
            applyMaxHealth(entity, ModConfig.PILLAGER_MAX_HEALTH);
        } else if (type == EntityType.VINDICATOR) {
            applyMaxHealth(entity, ModConfig.VINDICATOR_MAX_HEALTH);
        } else if (type == EntityType.EVOKER) {
            applyMaxHealth(entity, ModConfig.EVOKER_MAX_HEALTH);
        } else if (type == EntityType.VEX) {
            applyMaxHealth(entity, ModConfig.VEX_MAX_HEALTH);
        } else if (type == EntityType.RAVAGER) {
            applyMaxHealth(entity, ModConfig.RAVAGER_MAX_HEALTH);
        } else if (type == EntityType.GUARDIAN && !(entity instanceof ElderGuardianEntity)) {
            applyMaxHealth(entity, ModConfig.GUARDIAN_MAX_HEALTH);
        } else if (type == EntityType.HOGLIN) {
            applyMaxHealth(entity, ModConfig.HOGLIN_MAX_HEALTH);
        } else if (type == EntityType.ZOGLIN) {
            applyMaxHealth(entity, ModConfig.ZOGLIN_MAX_HEALTH);
        } else if (type == EntityType.SHULKER) {
            applyMaxHealth(entity, ModConfig.SHULKER_MAX_HEALTH);
        } else if (type == EntityType.BREEZE) {
            applyMaxHealth(entity, ModConfig.BREEZE_MAX_HEALTH);
        }
    }

    @Unique
    private static void applyMaxHealth(MobEntity entity, double targetMaxHealth) {
        EntityAttributeInstance maxHealthAttr = entity.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(targetMaxHealth);
            entity.setHealth((float) targetMaxHealth);
        }
    }
}
