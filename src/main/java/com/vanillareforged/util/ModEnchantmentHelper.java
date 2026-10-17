package com.vanillareforged.util;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;

public class ModEnchantmentHelper {

    /**
     * Retrieves the Quick Charge enchantment level on the provided item stack without requiring world/registry lookup.
     */
    public static int getQuickChargeLevel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        ItemEnchantmentsComponent enchants = EnchantmentHelper.getEnchantments(stack);
        for (var entry : enchants.getEnchantmentEntries()) {
            if (entry.getKey().matchesKey(Enchantments.QUICK_CHARGE)) {
                return entry.getIntValue();
            }
        }
        return 0;
    }
}
