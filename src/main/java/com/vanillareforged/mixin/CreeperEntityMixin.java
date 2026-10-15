package com.vanillareforged.mixin;

import com.vanillareforged.config.ModConfig;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.CreeperEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Phase 3 Section 2 & Section 9: Creeper Health Tuning.
 *
 * Rules:
 * - Max health: 22 HP (10% increase from 20 HP).
 * - Attack damage & explosion radius: strictly unchanged.
 */
@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin {

    @Inject(method = "createCreeperAttributes", at = @At("RETURN"), cancellable = true)
    private static void modifyCreeperAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.setReturnValue(cir.getReturnValue().add(EntityAttributes.MAX_HEALTH, ModConfig.CREEPER_MAX_HEALTH));
    }
}
