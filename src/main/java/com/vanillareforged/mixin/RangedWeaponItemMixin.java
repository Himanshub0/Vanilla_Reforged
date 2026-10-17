package com.vanillareforged.mixin;

import com.vanillareforged.entity.AmethystShardEntity;
import com.vanillareforged.entity.SonicBoomEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RangedWeaponItem.class)
public abstract class RangedWeaponItemMixin {

    @Inject(method = "createArrowEntity", at = @At("HEAD"), cancellable = true)
    private void onCreateArrowEntity(World world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical, CallbackInfoReturnable<ProjectileEntity> cir) {
        if (projectileStack.isOf(Items.AMETHYST_SHARD)) {
            PersistentProjectileEntity entity = new AmethystShardEntity(world, shooter, projectileStack.copyWithCount(1), weaponStack);
            if (critical) {
                entity.setCritical(true);
            }
            cir.setReturnValue(entity);
        } else if (projectileStack.isOf(Items.ECHO_SHARD)) {
            PersistentProjectileEntity entity = new SonicBoomEntity(world, shooter, projectileStack.copyWithCount(1), weaponStack);
            if (critical) {
                entity.setCritical(true);
            }
            cir.setReturnValue(entity);
        }
    }
}
