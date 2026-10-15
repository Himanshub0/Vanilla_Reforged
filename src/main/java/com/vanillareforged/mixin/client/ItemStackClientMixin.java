package com.vanillareforged.mixin.client;

import com.vanillareforged.client.EnchantmentTooltipHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(ItemStack.class)
public abstract class ItemStackClientMixin {

    @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
    private void appendDescriptionsOnGetTooltip(
            Item.TooltipContext context,
            PlayerEntity player,
            TooltipType type,
            CallbackInfoReturnable<List<Text>> cir) {
        List<Text> lines = cir.getReturnValue();
        if (lines != null) {
            List<Text> targetList = lines;
            try {
                EnchantmentTooltipHandler.appendEnchantmentDescriptions((ItemStack) (Object) this, targetList);
            } catch (UnsupportedOperationException e) {
                targetList = new ArrayList<>(lines);
                EnchantmentTooltipHandler.appendEnchantmentDescriptions((ItemStack) (Object) this, targetList);
                cir.setReturnValue(targetList);
            }
        }
    }
}
