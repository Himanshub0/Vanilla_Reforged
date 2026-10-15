package com.vanillareforged.mixin;

import com.vanillareforged.config.ModConfig;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Phase 3 Section 6.1: Spider Scaling & Jockey Exclusion.
 *
 * Rules:
 * - Ordinary standalone Spiders reduced by 30% (scale 0.7x, width ~0.98, height ~0.63).
 * - Spider Jockeys MUST REMAIN STRICTLY VANILLA (1.0x scale)!
 * - Cave Spiders remain completely untouched.
 */
@Mixin(SpiderEntity.class)
public abstract class SpiderEntityMixin extends HostileEntity {

    protected SpiderEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onSpiderConstructed(EntityType<? extends SpiderEntity> entityType, World world, CallbackInfo ci) {
        updateSpiderScale();
    }

    @Inject(method = "initialize", at = @At("RETURN"))
    private void onSpiderInitialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
                                    EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
        updateSpiderScale();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onSpiderTick(CallbackInfo ci) {
        // Periodically verify scale in case passenger state changes (e.g. jockey mounts/dismounts)
        if (!this.getEntityWorld().isClient() && this.age % 10 == 0) {
            updateSpiderScale();
        }
    }

    @Unique
    private void updateSpiderScale() {
        // Strictly protect Cave Spiders (EntityType.CAVE_SPIDER)
        if (this.getType() != EntityType.SPIDER) {
            return;
        }

        // CRITICAL: Spider Jockeys remain strictly 1.0x scale (vanilla)!
        boolean isJockey = this.hasPassengers();
        float targetScale = isJockey ? 1.0f : ModConfig.SPIDER_SCALE;

        EntityAttributeInstance scaleAttr = this.getAttributeInstance(EntityAttributes.SCALE);
        if (scaleAttr != null && Math.abs(scaleAttr.getBaseValue() - (double) targetScale) > 0.001) {
            scaleAttr.setBaseValue(targetScale);
        }
    }
}
