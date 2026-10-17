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

import org.jetbrains.annotations.Nullable;

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

    private final java.util.Set<Integer> piercedEntityIds = new java.util.HashSet<>();

    private void triggerSplashDamage(ServerWorld serverWorld, Vec3d center, @Nullable Entity directTarget) {
        Entity owner = this.getOwner();
        DamageSource source = serverWorld.getDamageSources().sonicBoom(owner != null ? owner : this);
        double radius = ModConfig.ECHO_SHARD_SPLASH_RADIUS;
        net.minecraft.util.math.Box splashBox = new net.minecraft.util.math.Box(
            center.x - radius, center.y - radius, center.z - radius,
            center.x + radius, center.y + radius, center.z + radius
        );

        // If direct target is non-living (e.g. End Crystal) and not yet pierced/damaged
        if (directTarget != null && !(directTarget instanceof LivingEntity) && directTarget != owner) {
            if (!this.piercedEntityIds.contains(directTarget.getId())) {
                this.piercedEntityIds.add(directTarget.getId());
                directTarget.damage(serverWorld, source, ModConfig.ECHO_SHARD_SONIC_BOOM_DAMAGE);
            }
        }

        java.util.List<LivingEntity> nearby = serverWorld.getEntitiesByClass(
            LivingEntity.class,
            splashBox,
            entity -> entity.isAlive() && entity != owner && !entity.isSpectator() && entity.squaredDistanceTo(center.x, center.y, center.z) <= radius * radius
        );

        for (LivingEntity living : nearby) {
            if (this.piercedEntityIds.contains(living.getId())) {
                continue;
            }
            this.piercedEntityIds.add(living.getId());

            boolean damaged = living.damage(serverWorld, source, ModConfig.ECHO_SHARD_SONIC_BOOM_DAMAGE);
            if (damaged) {
                // Warden-style knockback factoring knockback resistance
                double kbRes = 1.0 - living.getAttributeValue(EntityAttributes.KNOCKBACK_RESISTANCE);
                double verticalKb = 0.5 * kbRes;
                double horizontalKb = 2.5 * kbRes;
                Vec3d diff = new Vec3d(living.getX() - center.x, living.getY() - center.y, living.getZ() - center.z);
                Vec3d dir = diff.lengthSquared() > 1.0E-4 ? diff.normalize() : this.getVelocity().normalize();
                living.addVelocity(dir.getX() * horizontalKb, verticalKb, dir.getZ() * horizontalKb);
            }
        }

        // Sonic boom impact visuals & sound at impact point
        serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM, center.getX(), center.getY(), center.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        serverWorld.playSound(null, center.getX(), center.getY(), center.getZ(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 3.0f, 1.2f);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity target = entityHitResult.getEntity();
        if (target == this.getOwner() || piercedEntityIds.contains(target.getId())) {
            return;
        }

        World world = this.getEntityWorld();
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            Vec3d hitPos = entityHitResult.getPos();
            triggerSplashDamage(serverWorld, hitPos, target);
        }

        if (this.piercedEntityIds.size() > this.getPierceLevel()) {
            this.discard();
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        World world = this.getEntityWorld();
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            Vec3d pos = blockHitResult.getPos();
            triggerSplashDamage(serverWorld, pos, null);
        }
        this.discard();
    }
}
