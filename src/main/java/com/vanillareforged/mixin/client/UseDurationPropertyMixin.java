package com.vanillareforged.mixin.client;

import com.vanillareforged.util.ModEnchantmentHelper;
import net.minecraft.client.render.item.property.numeric.UseDurationProperty;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(UseDurationProperty.class)
public abstract class UseDurationPropertyMixin {

    @Inject(method = "getTicksUsedSoFar", at = @At("RETURN"), cancellable = true)
    private static void onGetTicksUsedSoFar(ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (stack != null && stack.isOf(Items.BOW)) {
            int quickCharge = ModEnchantmentHelper.getQuickChargeLevel(stack);
            if (quickCharge > 0) {
                int actualTicks = cir.getReturnValue();
                int chargeTime = Math.max(1, 20 - quickCharge * 5);
                int effectiveTicks = Math.round((float) actualTicks * (20.0f / (float) chargeTime));
                cir.setReturnValue(effectiveTicks);
            }
        }
    }
}
