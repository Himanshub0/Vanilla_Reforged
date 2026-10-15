package com.vanillareforged.mixin;

import com.vanillareforged.config.ModConfig;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Phase 3 Section 8.2: Wither Skeleton Balancing.
 *
 * Rules:
 * - Max health: 25 HP (vanilla 20 HP).
 * - Attack damage and Wither effect: strictly unchanged.
 * - Loot table: strictly unchanged (bones, coal, skulls, stone swords).
 */
@Mixin(WitherSkeletonEntity.class)
public abstract class WitherSkeletonEntityMixin {

    @Inject(method = "initialize", at = @At("RETURN"))
    private void setWitherSkeletonHealth(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
                                        EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
        WitherSkeletonEntity entity = (WitherSkeletonEntity) (Object) this;
        EntityAttributeInstance maxHealthAttr = entity.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(ModConfig.WITHER_SKELETON_MAX_HEALTH);
            entity.setHealth((float) ModConfig.WITHER_SKELETON_MAX_HEALTH);
        }
    }
}
