package com.vanillareforged.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Phase 3 Section 8.1: Blaze Combat Behavior Overhaul.
 *
 * Rules:
 * - Improved aim against moving players (predictive target leading).
 * - Deliberate fireball volley timing (10-tick interval between shots, 100-tick clear counterplay cooldown).
 * - Preserve visible counterplay and Nether Fortress behavior.
 * - Loot and XP unchanged.
 */
@Mixin(targets = "net.minecraft.entity.mob.BlazeEntity$ShootFireballGoal")
public abstract class BlazeShootFireballGoalMixin {

    @Shadow
    private BlazeEntity blaze;

    @Shadow
    private int fireballsFired;

    @Shadow
    private int fireballCooldown;

    /**
     * Predictive aiming against moving players: leads the target and reduces erratic spread.
     */
    @ModifyArg(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"),
        index = 0
    )
    private Entity improveFireballAim(Entity entity) {
        if (entity instanceof SmallFireballEntity fireball && this.blaze != null) {
            LivingEntity target = this.blaze.getTarget();
            if (target != null) {
                Vec3d targetPos = new Vec3d(target.getX(), target.getEyeY() - 0.2, target.getZ());
                Vec3d blazePos = new Vec3d(this.blaze.getX(), this.blaze.getEyeY(), this.blaze.getZ());
                double dist = blazePos.distanceTo(targetPos);
                double travelTicks = Math.max(1.0, dist / 1.0); // fireball speed approx 1 block/tick
                Vec3d targetVel = target.getVelocity();

                // Lead moving target position moderately
                Vec3d predictedPos = targetPos.add(targetVel.multiply(Math.min(travelTicks, 20.0) * 0.75));
                Vec3d aimDir = predictedPos.subtract(blazePos).normalize();

                // Tighter divergence (0.5f) compared to vanilla's erratic spread
                fireball.setVelocity(aimDir.x, aimDir.y, aimDir.z, 1.0f, 0.5f);
            }
        }
        return entity;
    }

    /**
     * Deliberate volley timing:
     * - Fires a deliberate 3-shot volley with 10 ticks between fireballs.
     * - Enforces a 100-tick (5.0s) gap between volleys with extinguished flame for clear player counterplay.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void enforceDeliberateVolleyTiming(CallbackInfo ci) {
        if (this.blaze == null) {
            return;
        }

        // Vanilla sets fireballCooldown to 6 right after firing; adjust to 10 ticks for deliberate pacing
        if (this.fireballsFired > 0 && this.fireballsFired <= 3 && this.fireballCooldown == 6) {
            this.fireballCooldown = 10;
        } else if (this.fireballsFired == 4) {
            // After 3rd shot, advance counter so vanilla's > 4 reset branch naturally triggers cooldown & extinguishes fire
            this.fireballsFired = 5;
        }
    }
}
