package com.vanillareforged.mixin;

import com.vanillareforged.config.ModConfig;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.BlazeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Phase 3 Section 8.1: Blaze Mob Balancing.
 *
 * Rules:
 * - Max health: 23 HP (vanilla 20 HP).
 * - Loot / XP: strictly unchanged.
 */
@Mixin(BlazeEntity.class)
public abstract class BlazeEntityMixin {

    @Inject(method = "createBlazeAttributes", at = @At("RETURN"), cancellable = true)
    private static void modifyBlazeAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.setReturnValue(cir.getReturnValue().add(EntityAttributes.MAX_HEALTH, ModConfig.BLAZE_MAX_HEALTH));
    }
}
