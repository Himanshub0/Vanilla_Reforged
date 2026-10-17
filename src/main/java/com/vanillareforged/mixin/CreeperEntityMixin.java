package com.vanillareforged.mixin;

import com.vanillareforged.config.ModConfig;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
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

    @ModifyArg(
        method = "explode",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerWorld;createExplosion(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/world/World$ExplosionSourceType;)V"
        ),
        index = 4
    )
    private float modifyExplosionPower(float power) {
        return power * ModConfig.CREEPER_EXPLOSION_POWER_MULTIPLIER;
    }

    @ModifyArg(
        method = "explode",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerWorld;createExplosion(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/world/World$ExplosionSourceType;)V"
        ),
        index = 5
    )
    private World.ExplosionSourceType modifyExplosionType(World.ExplosionSourceType type) {
        return ModConfig.CREEPER_DESTROYS_BLOCKS ? type : World.ExplosionSourceType.NONE;
    }
}
