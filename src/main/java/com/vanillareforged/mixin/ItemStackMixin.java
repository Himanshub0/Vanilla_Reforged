package com.vanillareforged.mixin;

import com.vanillareforged.balance.DurabilityManager;
import com.vanillareforged.enchantment.ModEnchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract Item getItem();

    @Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
    private void onGetMaxDamage(CallbackInfoReturnable<Integer> cir) {
        Integer override = DurabilityManager.getOverrides().get(this.getItem());
        if (override != null) {
            cir.setReturnValue(override);
        }
    }

    /**
     * Curse of Breaking: Cursed gear loses durability four times faster.
     */
    @ModifyVariable(
        method = "calculateDamage",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private int multiplyCurseOfBreakingDamage(int amount, int initialAmount, ServerWorld world, ServerPlayerEntity player) {
        if (world != null && com.vanillareforged.enchantment.EnchantmentHelper.getLevel(
                ModEnchantments.CURSE_OF_BREAKING,
                (ItemStack) (Object) this,
                world.getRegistryManager()) > 0) {
            return amount * 4;
        }
        return amount;
    }
}
