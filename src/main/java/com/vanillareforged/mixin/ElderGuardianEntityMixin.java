package com.vanillareforged.mixin;

import com.vanillareforged.config.ModConfig;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ElderGuardianEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Phase 3 Section 8.3: Elder Guardian Balancing.
 *
 * Rules:
 * - Max health: 92 HP (vanilla 80 HP).
 * - Mining fatigue, ocean monument spawning, drops and XP: strictly unchanged.
 */
@Mixin(ElderGuardianEntity.class)
public abstract class ElderGuardianEntityMixin {

    @Inject(method = "createElderGuardianAttributes", at = @At("RETURN"), cancellable = true)
    private static void modifyElderGuardianAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.setReturnValue(cir.getReturnValue().add(EntityAttributes.MAX_HEALTH, ModConfig.ELDER_GUARDIAN_MAX_HEALTH));
    }
}
