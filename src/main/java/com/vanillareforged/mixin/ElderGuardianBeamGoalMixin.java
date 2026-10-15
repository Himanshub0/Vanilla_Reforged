package com.vanillareforged.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Phase 3 Section 8.3: Elder Guardian Beam Tracking & Telegraph Warning.
 *
 * Rules:
 * - Improved beam tracking during laser warmup.
 * - Recognizable audio and visual telegraph warning prior to beam blast connecting.
 * - Loot and XP unchanged.
 */
@Mixin(targets = "net.minecraft.entity.mob.GuardianEntity$FireBeamGoal")
public abstract class ElderGuardianBeamGoalMixin {

    @Shadow
    private GuardianEntity guardian;

    @Shadow
    private int beamTicks;

    @Shadow
    private boolean elder;

    @Inject(method = "tick", at = @At("HEAD"))
    private void improveTrackingAndTelegraph(CallbackInfo ci) {
        if (!this.elder || this.guardian == null) {
            return;
        }

        LivingEntity target = this.guardian.getTarget();
        if (target == null) {
            return;
        }

        // Improved beam tracking: lock look-control firmly onto target
        this.guardian.getLookControl().lookAt(target, 180.0f, 180.0f);

        // Recognizable telegraph warning: at 45 ticks (15 ticks / 0.75s before 60-tick beam strike)
        if (this.beamTicks == 45 && !this.guardian.getEntityWorld().isClient()) {
            this.guardian.getEntityWorld().playSound(
                null,
                target.getBlockPos(),
                SoundEvents.BLOCK_CONDUIT_ATTACK_TARGET,
                SoundCategory.HOSTILE,
                1.0f,
                1.2f
            );

            if (this.guardian.getEntityWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                    ParticleTypes.NAUTILUS,
                    target.getX(),
                    target.getBodyY(0.5),
                    target.getZ(),
                    20,
                    0.4,
                    0.5,
                    0.4,
                    0.15
                );
                serverWorld.spawnParticles(
                    ParticleTypes.BUBBLE_POP,
                    target.getX(),
                    target.getBodyY(0.5),
                    target.getZ(),
                    15,
                    0.3,
                    0.4,
                    0.3,
                    0.05
                );
            }
        }
    }
}
