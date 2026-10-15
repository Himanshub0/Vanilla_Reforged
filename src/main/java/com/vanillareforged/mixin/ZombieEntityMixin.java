package com.vanillareforged.mixin;

import com.vanillareforged.config.ModConfig;
import com.vanillareforged.entity.KingZombieEntity;
import com.vanillareforged.entity.ZombieArmorManager;
import com.vanillareforged.entity.ZombieReinforcementManager;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin {

    /**
     * Section 3: Increase base max health from 20.0 to 22.0 HP (+10%).
     * Disable vanilla unconstrained hard-mode reinforcements (set to 0.0)
     * so our Controlled Reinforcement System has full authority.
     */
    @Inject(method = "createZombieAttributes", at = @At("RETURN"), cancellable = true)
    private static void modifyZombieAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.setReturnValue(cir.getReturnValue()
            .add(EntityAttributes.MAX_HEALTH, ModConfig.ZOMBIE_MAX_HEALTH)
            .add(EntityAttributes.SPAWN_REINFORCEMENTS, 0.0));
    }

    /**
     * Prevents vanilla equipment logic from equipping random gold/diamond armor.
     */
    @Inject(method = "initEquipment", at = @At("TAIL"))
    private void clearVanillaArmor(Random random, LocalDifficulty difficulty, CallbackInfo ci) {
        if ((Object) this instanceof KingZombieEntity) {
            return;
        }

        ZombieEntity zombie = (ZombieEntity) (Object) this;
        zombie.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
        zombie.equipStack(EquipmentSlot.CHEST, ItemStack.EMPTY);
        zombie.equipStack(EquipmentSlot.LEGS, ItemStack.EMPTY);
        zombie.equipStack(EquipmentSlot.FEET, ItemStack.EMPTY);
    }

    /**
     * Applies custom armor distribution & health initialization on spawn:
     * - 25% chance of spawning with armor (Leather 45%, Chainmail 35%, Iron 20%).
     * - Strict uniform tier, 1-4 pieces.
     * - Gold, diamond, netherite strictly 0%.
     * - Max health 22.0 HP.
     */
    @Inject(method = "initialize", at = @At("RETURN"))
    private void onZombieInitialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
        if ((Object) this instanceof KingZombieEntity) {
            return;
        }

        ZombieEntity zombie = (ZombieEntity) (Object) this;

        // Ensure 22 HP max health
        EntityAttributeInstance maxHealthAttr = zombie.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(ModConfig.ZOMBIE_MAX_HEALTH);
            zombie.setHealth((float) ModConfig.ZOMBIE_MAX_HEALTH);
        }

        // Apply custom armor distribution
        ZombieArmorManager.applySpawnArmor(zombie, world.getRandom());
    }

    /**
     * Section 3: Controlled Reinforcement System
     * Triggers when damaged by player, respecting 40% chance, 10s cooldown,
     * and max 4 active reinforcements per source zombie.
     */
    @Inject(method = "damage", at = @At("RETURN"))
    private void onZombieDamaged(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            return;
        }

        if ((Object) this instanceof KingZombieEntity) {
            return;
        }

        ZombieEntity zombie = (ZombieEntity) (Object) this;
        ZombieReinforcementManager.checkAndTriggerReinforcement(zombie, world, source);
    }
}
