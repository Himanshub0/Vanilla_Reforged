package com.vanillareforged.mixin;

import com.vanillareforged.config.ModConfig;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.WitchEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Phase 3 Section 2 & Section 9: Witch Health Tuning.
 *
 * Rules:
 * - Max health: 29 HP (10% increase from 26 HP rounded to 29 HP).
 * - Attack damage & potion effects: strictly unchanged.
 */
@Mixin(WitchEntity.class)
public abstract class WitchEntityMixin {

    @Inject(method = "createWitchAttributes", at = @At("RETURN"), cancellable = true)
    private static void modifyWitchAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.setReturnValue(cir.getReturnValue().add(EntityAttributes.MAX_HEALTH, ModConfig.WITCH_MAX_HEALTH));
    }
}
