package com.vanillareforged.mixin;

import com.vanillareforged.item.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.SmithingTransformRecipe;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SmithingTransformRecipe.class)
public abstract class SmithingTransformRecipeMixin {

    @Inject(
        method = "craft(Lnet/minecraft/recipe/input/SmithingRecipeInput;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/ItemStack;",
        at = @At("RETURN"),
        cancellable = true
    )
    private void normalizeDurabilityOnDiamondUpgrade(
        SmithingRecipeInput input,
        RegistryWrapper.WrapperLookup registries,
        CallbackInfoReturnable<ItemStack> cir
    ) {
        ItemStack template = input.template();
        if (template.isOf(ModItems.DIAMOND_UPGRADE_SMITHING_TEMPLATE)) {
            ItemStack base = input.base();
            ItemStack result = cir.getReturnValue();

            if (!result.isEmpty() && !base.isEmpty()) {
                int baseMax = base.getMaxDamage();
                int baseDamage = base.getDamage();

                if (baseMax > 0 && baseDamage > 0) {
                    int resultMax = result.getMaxDamage();
                    if (resultMax > 0) {
                        double damageRatio = (double) baseDamage / (double) baseMax;
                        int newDamage = (int) Math.round(damageRatio * resultMax);
                        // Ensure the upgraded tool is not instantly broken (at least 1 durability remaining)
                        newDamage = Math.min(newDamage, resultMax - 1);
                        result.setDamage(Math.max(1, newDamage));
                    }
                } else if (baseDamage == 0) {
                    result.setDamage(0);
                }
                cir.setReturnValue(result);
            }
        }
    }
}
