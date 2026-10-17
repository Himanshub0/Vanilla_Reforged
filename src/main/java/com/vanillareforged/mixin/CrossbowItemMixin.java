package com.vanillareforged.mixin;

import com.vanillareforged.entity.AmethystShardEntity;
import com.vanillareforged.entity.SonicBoomEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {

    @Inject(method = "getHeldProjectiles", at = @At("RETURN"), cancellable = true)
    private void onGetHeldProjectiles(CallbackInfoReturnable<Predicate<ItemStack>> cir) {
        Predicate<ItemStack> original = cir.getReturnValue();
        cir.setReturnValue(original.or(stack -> stack.isOf(Items.AMETHYST_SHARD) || stack.isOf(Items.ECHO_SHARD)));
    }

    @Inject(method = "getProjectiles", at = @At("RETURN"), cancellable = true)
    private void onGetProjectiles(CallbackInfoReturnable<Predicate<ItemStack>> cir) {
        Predicate<ItemStack> original = cir.getReturnValue();
        cir.setReturnValue(original.or(stack -> stack.isOf(Items.AMETHYST_SHARD) || stack.isOf(Items.ECHO_SHARD)));
    }

    @Inject(method = "createArrowEntity", at = @At("HEAD"), cancellable = true)
    private void onCreateArrowEntity(World world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical, CallbackInfoReturnable<ProjectileEntity> cir) {
        if (projectileStack.isOf(Items.AMETHYST_SHARD)) {
            PersistentProjectileEntity entity = new AmethystShardEntity(world, shooter, projectileStack.copyWithCount(1), weaponStack);
            entity.setSound(SoundEvents.ITEM_CROSSBOW_HIT);
            cir.setReturnValue(entity);
        } else if (projectileStack.isOf(Items.ECHO_SHARD)) {
            PersistentProjectileEntity entity = new SonicBoomEntity(world, shooter, projectileStack.copyWithCount(1), weaponStack);
            entity.setSound(SoundEvents.ITEM_CROSSBOW_HIT);
            cir.setReturnValue(entity);
        }
    }
}
