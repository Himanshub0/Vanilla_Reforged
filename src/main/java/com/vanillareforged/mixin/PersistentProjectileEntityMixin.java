package com.vanillareforged.mixin;

import com.vanillareforged.enchantment.EnchantmentHelper;
import com.vanillareforged.enchantment.ModEnchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin {

    /**
     * Precision: Arrows fired from Bows or Crossbows fly in a zero-gravity flat trajectory.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onPrecisionTick(CallbackInfo ci) {
        PersistentProjectileEntity projectile = (PersistentProjectileEntity) (Object) this;
        World world = projectile.getEntityWorld();
        if (world == null || world.isClient()) {
            return;
        }

        if (!projectile.hasNoGravity()) {
            ItemStack weapon = projectile.getWeaponStack();
            if (weapon != null && !weapon.isEmpty()) {
                int precisionLevel = EnchantmentHelper.getLevel(ModEnchantments.PRECISION, weapon, world.getRegistryManager());
                if (precisionLevel > 0) {
                    projectile.setNoGravity(true);
                }
            }
        } else if (projectile.age > 300) {
            // Failsafe: normalize gravity after 15 seconds to prevent permanent floating in mid-air
            projectile.setNoGravity(false);
        }
    }

    /**
     * Breeze Burst: Arrows create a wind charge explosion when they hit a block.
     */
    @Inject(method = "onBlockHit", at = @At("TAIL"))
    private void onBreezeBurstBlockHit(BlockHitResult blockHitResult, CallbackInfo ci) {
        PersistentProjectileEntity projectile = (PersistentProjectileEntity) (Object) this;
        World world = projectile.getEntityWorld();
        if (world == null || world.isClient()) {
            return;
        }

        if (projectile.hasNoGravity()) {
            projectile.setNoGravity(false);
        }

        ItemStack weapon = projectile.getWeaponStack();
        if (weapon != null && !weapon.isEmpty()) {
            int breezeLevel = EnchantmentHelper.getLevel(ModEnchantments.BREEZE_BURST, weapon, world.getRegistryManager());
            if (breezeLevel > 0) {
                Vec3d hitPos = blockHitResult.getPos();
                world.createExplosion(
                    projectile,
                    hitPos.x,
                    hitPos.y,
                    hitPos.z,
                    1.2f,
                    World.ExplosionSourceType.TRIGGER
                );
            }
        }
    }

    /**
     * Handles onEntityHit effects:
     * - Toxic: Arrows inflict Poison for 11 seconds (220 ticks). Undead mobs are immune.
     * - Breeze Burst: Direct arrow hits create a wind charge explosion.
     */
    @Inject(method = "onEntityHit", at = @At("TAIL"))
    private void onProjectileEntityHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        PersistentProjectileEntity projectile = (PersistentProjectileEntity) (Object) this;
        World world = projectile.getEntityWorld();
        if (world == null || world.isClient()) {
            return;
        }

        if (projectile.hasNoGravity()) {
            projectile.setNoGravity(false);
        }

        ItemStack weapon = projectile.getWeaponStack();
        if (weapon != null && !weapon.isEmpty()) {
            // Toxic: Arrows inflict Poison for 11 seconds (220 ticks). Undead mobs are immune.
            if (entityHitResult.getEntity() instanceof LivingEntity living && !living.getType().isIn(EntityTypeTags.UNDEAD)) {
                int toxicLevel = EnchantmentHelper.getLevel(ModEnchantments.TOXIC, weapon, world.getRegistryManager());
                if (toxicLevel > 0) {
                    living.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 220, 0));
                }
            }

            // Breeze Burst: Arrows create wind burst on direct entity hit as well
            int breezeLevel = EnchantmentHelper.getLevel(ModEnchantments.BREEZE_BURST, weapon, world.getRegistryManager());
            if (breezeLevel > 0) {
                Vec3d hitPos = entityHitResult.getPos();
                world.createExplosion(
                    projectile,
                    hitPos.x,
                    hitPos.y,
                    hitPos.z,
                    1.2f,
                    World.ExplosionSourceType.TRIGGER
                );
            }
        }
    }
}
