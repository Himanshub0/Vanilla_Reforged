package com.vanillareforged.mixin;

import com.vanillareforged.util.ModEnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(BowItem.class)
public abstract class BowItemMixin {

    @Inject(method = "getProjectiles", at = @At("RETURN"), cancellable = true)
    private void onGetProjectiles(CallbackInfoReturnable<Predicate<ItemStack>> cir) {
        Predicate<ItemStack> original = cir.getReturnValue();
        cir.setReturnValue(original.or(stack -> stack.isOf(Items.AMETHYST_SHARD) || stack.isOf(Items.ECHO_SHARD)));
    }

    @ModifyVariable(method = "onStoppedUsing", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private int modifyRemainingUseTicks(int remainingUseTicks, ItemStack stack, World world, LivingEntity user) {
        int quickCharge = ModEnchantmentHelper.getQuickChargeLevel(stack);
        if (quickCharge > 0) {
            int maxUseTime = stack.getMaxUseTime(user);
            int actualTicks = maxUseTime - remainingUseTicks;
            int chargeTime = Math.max(1, 20 - quickCharge * 5);
            int effectiveTicks = Math.round((float) actualTicks * (20.0f / (float) chargeTime));
            return maxUseTime - effectiveTicks;
        }
        return remainingUseTicks;
    }
}
