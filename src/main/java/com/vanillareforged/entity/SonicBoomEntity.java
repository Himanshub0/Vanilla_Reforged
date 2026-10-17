package com.vanillareforged.entity;

import com.vanillareforged.config.ModConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SonicBoomEntity extends PersistentProjectileEntity {

    public SonicBoomEntity(EntityType<? extends SonicBoomEntity> entityType, World world) {
        super(entityType, world);
        this.pickupType = PickupPermission.DISALLOWED;
        this.setNoGravity(true);
        this.setDamage(0.0);
    }

    public SonicBoomEntity(World world, LivingEntity owner, ItemStack projectileStack, ItemStack weaponStack) {
        super(ModEntities.SONIC_BOOM, owner, world, projectileStack, weaponStack);
        this.pickupType = PickupPermission.DISALLOWED;
        this.setNoGravity(true);
        this.setDamage(0.0);

        // Play Warden sonic boom sound upon firing
        if (!world.isClient()) {
            world.playSound(null, owner.getX(), owner.getEyeY(), owner.getZ(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 3.0f, 1.0f);
        }
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return new ItemStack(Items.ECHO_SHARD);
    }

    @Override
    public void tick() {
        super.tick();
        World world = this.getEntityWorld();
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            // Spawn Warden sonic boom and sculk soul particles along flight trajectory
            serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM, this.getX(), this.getY(), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY(), this.getZ(), 2, 0.1, 0.1, 0.1, 0.02);
        }

        if (this.age > 40 || this.isInGround()) {
            this.discard();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity target = entityHitResult.getEntity();
        if (target == this.getOwner()) {
            return;
        }

        World world = this.getEntityWorld();
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            Entity owner = this.getOwner();
            DamageSource source = serverWorld.getDamageSources().sonicBoom(owner != null ? owner : this);
            
            // Deal 40% Warden sonic boom damage (12.0f)
            boolean damaged = target.damage(serverWorld, source, ModConfig.ECHO_SHARD_SONIC_BOOM_DAMAGE);
            if (damaged && target instanceof LivingEntity living) {
                // Warden-style knockback factoring knockback resistance
                double kbRes = 1.0 - living.getAttributeValue(EntityAttributes.KNOCKBACK_RESISTANCE);
                double verticalKb = 0.5 * kbRes;
                double horizontalKb = 2.5 * kbRes;
                Vec3d dir = this.getVelocity().normalize();
                living.addVelocity(dir.getX() * horizontalKb, verticalKb, dir.getZ() * horizontalKb);
            }

            // Sonic boom impact visuals & sound
            serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getBodyY(0.5), target.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 3.0f, 1.2f);
        }
        this.discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        World world = this.getEntityWorld();
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            Vec3d pos = blockHitResult.getPos();
            serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM, pos.getX(), pos.getY(), pos.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            serverWorld.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 2.0f, 1.2f);
        }
        this.discard();
    }
}
