package com.vanillareforged.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;

import java.util.Optional;

public final class EnchantmentHelper {

    private EnchantmentHelper() {}

    /**
     * Look up the level of a specific enchantment key on the given item stack.
     */
    public static int getLevel(RegistryKey<Enchantment> key, ItemStack stack, RegistryWrapper.WrapperLookup registries) {
        if (stack.isEmpty() || registries == null) {
            return 0;
        }

        Optional<RegistryEntry.Reference<Enchantment>> entryOpt = registries.getOrThrow(RegistryKeys.ENCHANTMENT).getOptional(key);
        if (entryOpt.isEmpty()) {
            return 0;
        }

        return net.minecraft.enchantment.EnchantmentHelper.getLevel(entryOpt.get(), stack);
    }

    /**
     * Look up the level using the entity's world registry lookup.
     */
    public static int getLevel(RegistryKey<Enchantment> key, ItemStack stack, LivingEntity entity) {
        if (stack.isEmpty() || entity == null || entity.getEntityWorld() == null) {
            return 0;
        }
        return getLevel(key, stack, entity.getEntityWorld().getRegistryManager());
    }

    /**
     * Checks all armor slots of a LivingEntity and returns the highest level found for the given enchantment.
     */
    public static int getArmorLevel(RegistryKey<Enchantment> key, LivingEntity entity) {
        if (entity == null || entity.getEntityWorld() == null) {
            return 0;
        }

        RegistryWrapper.WrapperLookup registries = entity.getEntityWorld().getRegistryManager();
        int maxLevel = 0;
        EquipmentSlot[] armorSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (EquipmentSlot slot : armorSlots) {
            ItemStack stack = entity.getEquippedStack(slot);
            int level = getLevel(key, stack, registries);
            if (level > maxLevel) {
                maxLevel = level;
            }
        }
        return maxLevel;
    }

    /**
     * Checks all armor slots of a LivingEntity and returns the sum of levels found for the given enchantment.
     */
    public static int getArmorTotalLevel(RegistryKey<Enchantment> key, LivingEntity entity) {
        if (entity == null || entity.getEntityWorld() == null) {
            return 0;
        }

        RegistryWrapper.WrapperLookup registries = entity.getEntityWorld().getRegistryManager();
        int totalLevel = 0;
        EquipmentSlot[] armorSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (EquipmentSlot slot : armorSlots) {
            ItemStack stack = entity.getEquippedStack(slot);
            totalLevel += getLevel(key, stack, registries);
        }
        return totalLevel;
    }

    /**
     * Checks equipped stack in a specific slot.
     */
    public static int getEquipmentLevel(RegistryKey<Enchantment> key, LivingEntity entity, EquipmentSlot slot) {
        if (entity == null || entity.getEntityWorld() == null) {
            return 0;
        }
        return getLevel(key, entity.getEquippedStack(slot), entity.getEntityWorld().getRegistryManager());
    }

    /**
     * Safely updates an attribute modifier only if its existence or value has changed,
     * preventing redundant packets and server/client sync storms.
     *
     * @param attributeInstance The attribute instance to modify
     * @param modifierId The unique Identifier for the modifier
     * @param shouldApply Whether the modifier should be present
     * @param targetValue The desired modifier value if shouldApply is true
     * @param operation The operation of the modifier
     * @return true if the attribute was mutated (modifier added, removed, or changed), false if unchanged
     */
    public static boolean updateAttributeModifier(
            EntityAttributeInstance attributeInstance,
            Identifier modifierId,
            boolean shouldApply,
            double targetValue,
            EntityAttributeModifier.Operation operation
    ) {
        if (attributeInstance == null) {
            return false;
        }

        EntityAttributeModifier existing = attributeInstance.getModifier(modifierId);
        if (!shouldApply) {
            if (existing != null) {
                attributeInstance.removeModifier(modifierId);
                return true;
            }
            return false;
        } else {
            if (existing == null || Math.abs(existing.value() - targetValue) > 1e-6 || existing.operation() != operation) {
                if (existing != null) {
                    attributeInstance.removeModifier(modifierId);
                }
                attributeInstance.addTemporaryModifier(new EntityAttributeModifier(modifierId, targetValue, operation));
                return true;
            }
            return false;
        }
    }
}
