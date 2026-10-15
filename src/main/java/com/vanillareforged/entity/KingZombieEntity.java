package com.vanillareforged.entity;

import com.vanillareforged.VanillaReforgedMod;
import com.vanillareforged.config.ModConfig;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

import java.util.Optional;

public class KingZombieEntity extends ZombieEntity {

    public static final RegistryKey<LootTable> KING_ZOMBIE_LOOT_TABLE =
        RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(VanillaReforgedMod.MOD_ID, "entities/king_zombie"));
    public static final RegistryKey<LootTable> DIAMOND_KING_ZOMBIE_LOOT_TABLE =
        RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(VanillaReforgedMod.MOD_ID, "entities/diamond_king_zombie"));

    private static final TrackedData<Boolean> IS_DIAMOND =
        DataTracker.registerData(KingZombieEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> KINGS_CALL_USED =
        DataTracker.registerData(KingZombieEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public KingZombieEntity(EntityType<? extends KingZombieEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 25;
        this.calculateDimensions();
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        return EntityDimensions.changing(0.6f, 1.95f).withEyeHeight(1.74f);
    }

    public static DefaultAttributeContainer.Builder createKingZombieAttributes() {
        return ZombieEntity.createZombieAttributes()
            .add(EntityAttributes.MAX_HEALTH, ModConfig.KING_ZOMBIE_MAX_HEALTH)
            .add(EntityAttributes.SCALE, ModConfig.KING_ZOMBIE_SCALE)
            .add(EntityAttributes.STEP_HEIGHT, 1.0)
            .add(EntityAttributes.ATTACK_DAMAGE, ModConfig.KING_ZOMBIE_ATTACK_DAMAGE)
            .add(EntityAttributes.KNOCKBACK_RESISTANCE, ModConfig.KING_ZOMBIE_KNOCKBACK_RESISTANCE)
            .add(EntityAttributes.FOLLOW_RANGE, ModConfig.KING_ZOMBIE_FOLLOW_RANGE)
            .add(EntityAttributes.MOVEMENT_SPEED, 0.23)
            .add(EntityAttributes.SPAWN_REINFORCEMENTS, 0.0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(IS_DIAMOND, false);
        builder.add(KINGS_CALL_USED, false);
    }

    public boolean isDiamond() {
        return this.dataTracker.get(IS_DIAMOND);
    }

    public void setDiamond(boolean diamond) {
        this.dataTracker.set(IS_DIAMOND, diamond);
        this.experiencePoints = diamond ? 40 : 25;
        if (!this.getEntityWorld().isClient()) {
            this.equipKingArmor();
        }
    }

    public boolean isKingsCallUsed() {
        return this.dataTracker.get(KINGS_CALL_USED);
    }

    public void setKingsCallUsed(boolean used) {
        this.dataTracker.set(KINGS_CALL_USED, used);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putBoolean("IsDiamond", this.isDiamond());
        view.putBoolean("KingsCallUsed", this.isKingsCallUsed());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setDiamond(view.getBoolean("IsDiamond", false));
        this.setKingsCallUsed(view.getBoolean("KingsCallUsed", false));
        this.setBaby(false);
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    public void setBaby(boolean baby) {
        super.setBaby(false);
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData) {
        EntityData data = super.initialize(world, difficulty, spawnReason, entityData);

        // King Zombies are boss mobs and can never be babies
        this.setBaby(false);

        if (spawnReason == SpawnReason.SPAWN_ITEM_USE) {
            // King Zombie Spawn Egg ALWAYS spawns Gold King
            this.setDiamond(false);
        } else {
            // Naturally spawned King Zombie has a 5% chance (ModConfig.DIAMOND_KING_CHANCE) to be Diamond King
            boolean diamond = world.getRandom().nextFloat() < (float) ModConfig.DIAMOND_KING_CHANCE;
            this.setDiamond(diamond);
        }

        this.equipKingArmor();
        return data;
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty difficulty) {
        this.equipKingArmor();
    }

    public void equipKingArmor() {
        if (this.isDiamond()) {
            this.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
            this.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
            this.equipStack(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
            this.equipStack(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
        } else {
            this.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
            this.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
            this.equipStack(EquipmentSlot.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS));
            this.equipStack(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS));
        }

        // Drop chances according to specification:
        // Gold King: 15% (0.15) per equipped gold armor piece
        // Diamond King: 5% (0.05) per equipped diamond armor piece
        float dropChance = this.isDiamond() ? 0.05f : 0.15f;
        this.setEquipmentDropChance(EquipmentSlot.HEAD, dropChance);
        this.setEquipmentDropChance(EquipmentSlot.CHEST, dropChance);
        this.setEquipmentDropChance(EquipmentSlot.LEGS, dropChance);
        this.setEquipmentDropChance(EquipmentSlot.FEET, dropChance);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getEntityWorld().isClient()) {
            // Speed I effect permanently active
            if (!this.hasStatusEffect(StatusEffects.SPEED)) {
                this.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SPEED,
                    StatusEffectInstance.INFINITE,
                    0,
                    false, // ambient
                    false, // showParticles
                    true   // showIcon
                ));
            }

            // King's Call check (50 HP threshold, 100% trigger reliability)
            if (!this.isKingsCallUsed() && this.getHealth() <= ModConfig.KINGS_CALL_THRESHOLD_HEALTH) {
                this.triggerKingsCall();
            }
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        boolean result = super.damage(world, source, amount);
        if (!this.getEntityWorld().isClient() && !this.isKingsCallUsed() && this.getHealth() <= ModConfig.KINGS_CALL_THRESHOLD_HEALTH) {
            this.triggerKingsCall();
        }
        return result;
    }

    /**
     * King's Call Ability:
     * When King Zombie falls to or below 50.0 HP for the first time:
     * - 100% trigger reliability.
     * - Spawns random 5 to 10 standard Zombies safely around King Zombie.
     * - Wave zombies are marked with "vanilla_reforged:wave_zombie" and "vanilla_reforged:no_reinforcement"
     *   so they NEVER trigger reinforcement chains.
     * - Visual and sound effects.
     */
    public void triggerKingsCall() {
        if (this.isKingsCallUsed()) {
            return;
        }
        this.setKingsCallUsed(true);

        World world = this.getEntityWorld();
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        // Visual and sound feedback
        serverWorld.playSound(
            null,
            this.getX(),
            this.getY(),
            this.getZ(),
            SoundEvents.EVENT_RAID_HORN.value(),
            SoundCategory.HOSTILE,
            2.5f,
            0.85f
        );
        serverWorld.playSound(
            null,
            this.getX(),
            this.getY(),
            this.getZ(),
            SoundEvents.ENTITY_WITHER_SPAWN,
            SoundCategory.HOSTILE,
            0.8f,
            1.2f
        );

        serverWorld.spawnParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + 1.8, this.getZ(), 35, 1.0, 0.8, 1.0, 0.1);
        serverWorld.spawnParticles(ParticleTypes.SOUL, this.getX(), this.getY() + 1.2, this.getZ(), 50, 1.5, 1.0, 1.5, 0.05);

        // Expanding 360-degree soul fire shockwave ring
        for (int d = 0; d < 36; d++) {
            double rad = Math.toRadians(d * 10);
            serverWorld.spawnParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                this.getX() + Math.cos(rad) * 3.0,
                this.getY() + 0.2,
                this.getZ() + Math.sin(rad) * 3.0,
                2, 0.1, 0.1, 0.1, 0.02
            );
        }

        int minSummons = ModConfig.KINGS_CALL_MIN_SUMMONS;
        int maxSummons = ModConfig.KINGS_CALL_MAX_SUMMONS;
        int count = minSummons + this.getRandom().nextInt(maxSummons - minSummons + 1);

        for (int i = 0; i < count; i++) {
            double angle = this.getRandom().nextDouble() * 2 * Math.PI;
            double distance = 2.0 + this.getRandom().nextDouble() * 3.5;
            double spawnX = this.getX() + Math.cos(angle) * distance;
            double spawnZ = this.getZ() + Math.sin(angle) * distance;

            BlockPos originPos = BlockPos.ofFloored(spawnX, this.getY(), spawnZ);
            BlockPos safePos = findSafeSpawnPos(serverWorld, originPos);
            if (safePos == null) {
                safePos = this.getBlockPos();
            }

            ZombieEntity minion = EntityType.ZOMBIE.create(serverWorld, SpawnReason.MOB_SUMMONED);
            if (minion != null) {
                minion.refreshPositionAndAngles(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5, this.getRandom().nextFloat() * 360f, 0.0f);
                minion.initialize(serverWorld, serverWorld.getLocalDifficulty(safePos), SpawnReason.MOB_SUMMONED, null);

                // Mark wave zombie with persistent tags to prevent recursive reinforcement
                minion.addCommandTag("vanilla_reforged:wave_zombie");
                minion.addCommandTag("vanilla_reforged:no_reinforcement");

                LivingEntity target = this.getTarget();
                if (target != null) {
                    minion.setTarget(target);
                }

                serverWorld.spawnEntityAndPassengers(minion);
                serverWorld.spawnParticles(ParticleTypes.POOF, minion.getX(), minion.getY() + 0.5, minion.getZ(), 10, 0.3, 0.5, 0.3, 0.02);
            }
        }
    }

    private BlockPos findSafeSpawnPos(ServerWorld world, BlockPos pos) {
        for (int dy = -2; dy <= 2; dy++) {
            BlockPos test = pos.up(dy);
            if (world.getBlockState(test.down()).blocksMovement() &&
                world.getBlockState(test).isAir() &&
                world.getBlockState(test.up()).isAir()) {
                return test;
            }
        }
        return null;
    }

    @Override
    protected void dropLoot(ServerWorld world, DamageSource source, boolean causedByPlayer) {
        RegistryKey<LootTable> key = this.isDiamond() ? DIAMOND_KING_ZOMBIE_LOOT_TABLE : KING_ZOMBIE_LOOT_TABLE;
        this.dropLoot(world, source, causedByPlayer, key);
    }

    @Override
    protected int getExperienceToDrop(ServerWorld world) {
        return this.isDiamond() ? 40 : 25;
    }

    @Override
    protected void dropEquipment(ServerWorld world, DamageSource source, boolean causedByPlayer) {
        // Equipment drop chance: 15% Gold King, 5% Diamond King per worn piece with randomized durability
        float dropChance = this.isDiamond() ? 0.05f : 0.15f;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                ItemStack stack = this.getEquippedStack(slot);
                if (!stack.isEmpty()) {
                    if (world.getRandom().nextFloat() < dropChance) {
                        ItemStack dropStack = stack.copy();
                        if (dropStack.isDamageable()) {
                            int maxDamage = dropStack.getMaxDamage();
                            int damage = world.getRandom().nextInt(maxDamage);
                            dropStack.setDamage(Math.max(1, damage));
                        }
                        this.dropStack(world, dropStack);
                    }
                    this.equipStack(slot, ItemStack.EMPTY);
                }
            }
        }
        super.dropEquipment(world, source, causedByPlayer);
    }

    @Override
    public boolean canPickupItem(ItemStack stack) {
        return false;
    }

    @Override
    protected boolean burnsInDaylight() {
        return false;
    }
}
