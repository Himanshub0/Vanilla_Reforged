package com.vanillareforged.mixin;

import com.vanillareforged.config.ModConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Phase 3 Section 2 & Section 9: Creeper Health Tuning and Explosion Impact.
 *
 * Rules:
 * - Max health: 22 HP (10% increase from 20 HP).
 * - Creeper explosion does not destroy blocks (World.ExplosionSourceType.NONE),
 *   but blasts with greater impact (power multiplier).
 */
@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin {

    @Inject(method = "createCreeperAttributes", at = @At("RETURN"), cancellable = true)
    private static void modifyCreeperAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.setReturnValue(cir.getReturnValue().add(EntityAttributes.MAX_HEALTH, ModConfig.CREEPER_MAX_HEALTH));
    }

    @Redirect(
        method = "explode",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerWorld;createExplosion(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/world/World$ExplosionSourceType;)V"
        )
    )
    private void redirectCreeperExplosion(ServerWorld world, Entity entity, double x, double y, double z, float power, World.ExplosionSourceType explosionSourceType) {
        float effectivePower = power * ModConfig.CREEPER_EXPLOSION_POWER_MULTIPLIER;
        World.ExplosionSourceType effectiveType = ModConfig.CREEPER_DESTROYS_BLOCKS ? explosionSourceType : World.ExplosionSourceType.NONE;
        world.createExplosion(entity, x, y, z, effectivePower, effectiveType);
    }
}
