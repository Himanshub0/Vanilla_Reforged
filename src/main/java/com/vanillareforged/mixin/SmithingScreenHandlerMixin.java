package com.vanillareforged.mixin;

import com.vanillareforged.item.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingScreenHandler.class)
public abstract class SmithingScreenHandlerMixin extends ScreenHandler {

    protected SmithingScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "decrementStack", at = @At("HEAD"), cancellable = true)
    private void preserveDiamondUpgradeTemplate(int slot, CallbackInfo ci) {
        // Slot 0 is the template slot in SmithingScreenHandler
        if (slot == 0) {
            Slot templateSlot = this.getSlot(0);
            if (templateSlot != null && templateSlot.hasStack()) {
                ItemStack template = templateSlot.getStack();
                if (template.isOf(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE)) {
                    // The Diamond Upgrade Smithing Template is reusable and preserved across upgrades
                    ci.cancel();
                }
            }
        }
    }
}
