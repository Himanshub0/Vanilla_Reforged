package com.vanillareforged.entity;

import com.vanillareforged.config.ModConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class AmethystShardEntity extends PersistentProjectileEntity implements FlyingItemEntity {

    public AmethystShardEntity(EntityType<? extends AmethystShardEntity> entityType, World world) {
        super(entityType, world);
        this.pickupType = PickupPermission.DISALLOWED;
    }

    public AmethystShardEntity(World world, LivingEntity owner, ItemStack projectileStack, ItemStack weaponStack) {
        super(ModEntities.AMETHYST_SHARD, owner, world, projectileStack, weaponStack);
        this.pickupType = PickupPermission.DISALLOWED;
        this.setDamage(2.0);
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return new ItemStack(Items.AMETHYST_SHARD);
    }

    @Override
    public ItemStack getStack() {
        return new ItemStack(Items.AMETHYST_SHARD);
    }

    @Override
    protected void onHit(LivingEntity target) {
        super.onHit(target);
        // Apply 2 minutes of Weakness and Instant Damage
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, ModConfig.AMETHYST_SHARD_WEAKNESS_TICKS, 0), this.getEffectCause());
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, 0), this.getEffectCause());
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        World world = this.getEntityWorld();
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(
                new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.AMETHYST_SHARD)),
                this.getX(), this.getY(), this.getZ(),
                12, 0.15, 0.15, 0.15, 0.08
            );
            serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK, SoundCategory.NEUTRAL, 1.0f, 1.2f);
        }
        if (this.getPierceLevel() <= 0) {
            this.discard();
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        World world = this.getEntityWorld();
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(
                new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.AMETHYST_SHARD)),
                this.getX(), this.getY(), this.getZ(),
                12, 0.15, 0.15, 0.15, 0.08
            );
            serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK, SoundCategory.NEUTRAL, 1.0f, 1.2f);
        }
        this.discard();
    }
}
