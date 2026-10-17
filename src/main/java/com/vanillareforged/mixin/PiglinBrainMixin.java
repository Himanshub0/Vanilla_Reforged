package com.vanillareforged.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimMaterials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Piglin Pacification with Gold Armor Trim:
 * Piglins do not attack players wearing any piece of armor trimmed with gold material in the Nether.
 */
@Mixin(PiglinBrain.class)
public abstract class PiglinBrainMixin {

    @Inject(method = "isWearingPiglinSafeArmor", at = @At("RETURN"), cancellable = true)
    private static void onIsWearingPiglinSafeArmor(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            return;
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                ItemStack stack = entity.getEquippedStack(slot);
                if (!stack.isEmpty()) {
                    ArmorTrim trim = stack.get(DataComponentTypes.TRIM);
                    if (trim != null && trim.material().matchesKey(ArmorTrimMaterials.GOLD)) {
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }
        }
    }
}
