package com.vanillareforged.entity;

import com.vanillareforged.VanillaReforgedMod;
import com.vanillareforged.config.ModConfig;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.passive.WolfVariant;
import net.minecraft.entity.passive.WolfVariants;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.TameableEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Section 10 — Dire Wolf Custom Entity
 * - Max health: 100.0 HP (50 hearts), retained upon taming.
 * - Attack damage: 20.0 HP (5x normal wolf).
 * - Scale: 2.0x (EntityAttributes.SCALE = 2.0).
 * - Movement speed: 1.5x normal wolf (0.45 vs 0.30).
 * - Step height: 1.0625 blocks for smooth jumping/riding over terrain.
 * - Variants: 0 (Regular pale coat), 1 (Ashen Black coat), 2 (Woods coat).
 * - Eye textures:
 *     Wild: Sinister glowing red eyes across all coats.
 *     Tamed: Authentic natural wolf eyes (NOT glowing red, NOT cartoon puppy eyes).
 * - Never attacks its owner after taming (clears targets and retaliatory anger).
 * - Armor Mounting: When equipped with wolf armor, owner can mount and steer it as a fast, agile mount!
 * - 4-step taming ritual:
 *     Step 1: Active Weakness status effect.
 *     Step 2: Feed Bone -> progresses to stage 1.
 *     Step 3: Feed Steak -> progresses to stage 2.
 *     Step 4: Feed Golden Carrot -> 25% chance of success.
 *             If fail, smoke + whine, STAYS at stage 2 for repeat attempts.
 *             If success, grants "Apex Companion" advancement!
 */
public class DireWolfEntity extends WolfEntity implements JumpingMount {
    // 3 Coats: Regular (0), Ashen Black (1), Woods (2) with Wild (red eyes) and Tamed (natural eyes)
    public static final Identifier REGULAR_WILD_TEXTURE = Identifier.of(VanillaReforgedMod.MOD_ID, "textures/entity/dire_wolf/dire_wolf_regular_wild.png");
    public static final Identifier REGULAR_TAMED_TEXTURE = Identifier.of(VanillaReforgedMod.MOD_ID, "textures/entity/dire_wolf/dire_wolf_regular_tamed.png");
    public static final Identifier ASHEN_WILD_TEXTURE = Identifier.of(VanillaReforgedMod.MOD_ID, "textures/entity/dire_wolf/dire_wolf_ashen_wild.png");
    public static final Identifier ASHEN_TAMED_TEXTURE = Identifier.of(VanillaReforgedMod.MOD_ID, "textures/entity/dire_wolf/dire_wolf_ashen_tamed.png");
    public static final Identifier WOODS_WILD_TEXTURE = Identifier.of(VanillaReforgedMod.MOD_ID, "textures/entity/dire_wolf/dire_wolf_woods_wild.png");
    public static final Identifier WOODS_TAMED_TEXTURE = Identifier.of(VanillaReforgedMod.MOD_ID, "textures/entity/dire_wolf/dire_wolf_woods_tamed.png");

    public static final TrackedData<Integer> VARIANT = DataTracker.registerData(DireWolfEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Integer> TAMING_STAGE = DataTracker.registerData(DireWolfEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public DireWolfEntity(EntityType<? extends WolfEntity> entityType, World world) {
        super(entityType, world);
        this.setHealth((float) ModConfig.DIRE_WOLF_MAX_HEALTH);
        this.calculateDimensions();
    }

    public static DefaultAttributeContainer.Builder createDireWolfAttributes() {
        return WolfEntity.createWolfAttributes()
            .add(EntityAttributes.MAX_HEALTH, ModConfig.DIRE_WOLF_MAX_HEALTH)
            .add(EntityAttributes.ATTACK_DAMAGE, ModConfig.DIRE_WOLF_ATTACK_DAMAGE)
            .add(EntityAttributes.MOVEMENT_SPEED, 0.30 * ModConfig.DIRE_WOLF_SPEED_MULTIPLIER)
            .add(EntityAttributes.SCALE, ModConfig.DIRE_WOLF_SCALE)
            .add(EntityAttributes.STEP_HEIGHT, 1.0625)
            .add(EntityAttributes.JUMP_STRENGTH, 0.8)
            .add(EntityAttributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VARIANT, 0);
        builder.add(TAMING_STAGE, 0);
    }

    public int getVariant() {
        return this.dataTracker.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.dataTracker.set(VARIANT, variant);
    }

    public int getTamingStage() {
        return this.dataTracker.get(TAMING_STAGE);
    }

    public void setTamingStage(int stage) {
        this.dataTracker.set(TAMING_STAGE, stage);
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData) {
        EntityData data = super.initialize(world, difficulty, spawnReason, entityData);
        // Dire wolves are apex predators: strictly full-grown adults
        this.setBaby(false);
        this.setBreedingAge(0);
        this.setHealth((float) ModConfig.DIRE_WOLF_MAX_HEALTH);

        // Biome-based variant is automatically selected by super.initialize() via Variants.select!
        // Sync legacy int variant for backwards compatibility with tests and saved worlds:
        RegistryEntry<WolfVariant> var = this.get(DataComponentTypes.WOLF_VARIANT);
        if (var != null && var.getKey().isPresent()) {
            Identifier id = var.getKey().get().getValue();
            if (id.equals(WolfVariants.ASHEN.getValue())) {
                this.setVariant(1);
            } else if (id.equals(WolfVariants.WOODS.getValue())) {
                this.setVariant(2);
            } else {
                this.setVariant(0);
            }
        }
        this.calculateDimensions();
        return data;
    }

    public String getWolfVariantName() {
        RegistryEntry<WolfVariant> entry = this.get(DataComponentTypes.WOLF_VARIANT);
        if (entry != null && entry.getKey().isPresent()) {
            return entry.getKey().get().getValue().getPath();
        }
        return switch (this.getVariant()) {
            case 1 -> "ashen";
            case 2 -> "woods";
            default -> "pale";
        };
    }

    @Override
    public Identifier getTextureId() {
        String name = this.getWolfVariantName();
        boolean tamed = this.isTamed();
        String suffix = tamed ? "tamed" : "wild";
        return Identifier.of(VanillaReforgedMod.MOD_ID, "textures/entity/dire_wolf/dire_wolf_" + name + "_" + suffix + ".png");
    }

    public Identifier getTextureForState() {
        return this.getTextureId();
    }

    @Override
    protected void updateAttributesForTamed() {
        // Dire Wolf retains 100 HP when tamed!
        if (this.getAttributeInstance(EntityAttributes.MAX_HEALTH) != null) {
            this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(ModConfig.DIRE_WOLF_MAX_HEALTH);
        }
        this.setHealth((float) ModConfig.DIRE_WOLF_MAX_HEALTH);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putInt("Variant", this.getVariant());
        view.putInt("TamingStage", this.getTamingStage());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setVariant(view.getInt("Variant", 0));
        this.setTamingStage(view.getInt("TamingStage", 0));
        if (this.getAttributeInstance(EntityAttributes.MAX_HEALTH) != null) {
            this.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(ModConfig.DIRE_WOLF_MAX_HEALTH);
        }
        this.calculateDimensions();
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        // Section 10 & User Polish: Apex Beast Hostile Mob Targeting
        // Dire Wolves actively hunt and attack almost all hostile mobs (Zombies, Spiders, Skeletons, Pillagers, Witches, Slimes, Endermites, Silverfish, etc.)
        // Creepers and Ghasts are strictly excluded.
        this.targetSelector.add(5, new ActiveTargetGoal<>(this, MobEntity.class, 10, true, false, (entity, world) -> this.canHuntAsHostile(entity)));
    }

    public boolean canHuntAsHostile(LivingEntity target) {
        if (target == null || target == this) {
            return false;
        }
        if (this.isSitting() || this.hasPassengers()) {
            return false;
        }
        // Never target Creepers (prevents suicide explosions)
        if (target instanceof CreeperEntity) {
            return false;
        }
        // Never target Ghasts
        if (target instanceof GhastEntity) {
            return false;
        }
        // Never target players or companions
        if (target instanceof PlayerEntity) {
            return false;
        }
        if (target instanceof WolfEntity) {
            return false;
        }
        if (target instanceof TameableEntity tameable && tameable.isTamed()) {
            return false;
        }
        if (target instanceof MerchantEntity || target instanceof GolemEntity) {
            return false;
        }
        // For neutral mobs: do not pick unprovoked fights with neutral piglins or endermen
        if (target instanceof ZombifiedPiglinEntity zombifiedPiglin && !zombifiedPiglin.hasAngerTime()) {
            return false;
        }
        if (target instanceof EndermanEntity enderman && !enderman.isAngry()) {
            return false;
        }
        // Target all hostile monsters!
        return target instanceof HostileEntity || target instanceof SlimeEntity || target instanceof PhantomEntity || target instanceof Monster;
    }

    // ==========================================
    // OWNER PROTECTION & TARGETING
    // ==========================================

    @Override
    public boolean canTarget(LivingEntity target) {
        if (target == null) {
            return false;
        }
        if (this.isTamed() && this.isOwner(target)) {
            return false;
        }
        if (target instanceof CreeperEntity) {
            return false;
        }
        if (this.isSitting()) {
            return false;
        }
        return super.canTarget(target);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target instanceof CreeperEntity) {
            return;
        }
        if (this.isTamed() && target != null && this.isOwner(target)) {
            return;
        }
        if (this.isSitting() && target != null) {
            return;
        }
        super.setTarget(target);
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isTamed() && source.getAttacker() instanceof PlayerEntity player && this.isOwner(player)) {
            // Never retaliate or become angry against the owner
            this.stopAnger();
            return super.damage(world, source, amount);
        }
        return super.damage(world, source, amount);
    }

    // ==========================================
    // RIDING & VEHICLE CONTROL SYSTEM
    // ==========================================

    protected float mountJumpStrength;

    public boolean hasWolfArmor() {
        return this.isWearingBodyArmor() || !this.getBodyArmor().isEmpty();
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (this.isTamed() && this.hasWolfArmor()) {
            Entity firstPassenger = this.getFirstPassenger();
            if (firstPassenger instanceof PlayerEntity player && this.isOwner(player)) {
                return player;
            }
        }
        return null;
    }

    @Override
    protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput) {
        float sideways = controllingPlayer.sidewaysSpeed * 0.5f;
        float forward = controllingPlayer.forwardSpeed;
        if (forward <= 0.0f) {
            forward *= 0.25f;
        }
        return new Vec3d(sideways, 0.0, forward);
    }

    protected Vec2f getControlledRotation(LivingEntity controllingPassenger) {
        return new Vec2f(controllingPassenger.getPitch() * 0.5f, controllingPassenger.getYaw());
    }

    @Override
    protected float getSaddledSpeed(PlayerEntity controllingPlayer) {
        return (float) this.getAttributeValue(EntityAttributes.MOVEMENT_SPEED);
    }

    @Override
    protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
        super.tickControlled(controllingPlayer, movementInput);
        Vec2f rotation = this.getControlledRotation(controllingPlayer);
        this.setRotation(rotation.y, rotation.x);
        this.lastYaw = this.bodyYaw = this.headYaw = this.getYaw();

        if (this.isLogicalSideForUpdatingMovement()) {
            if (this.isOnGround() && this.mountJumpStrength > 0.0f) {
                this.executeMountJump(this.mountJumpStrength, movementInput);
                this.mountJumpStrength = 0.0f;
            } else if (this.isTouchingWater()) {
                // Natural mount buoyancy & swimming: stays afloat at surface and rises when jumping
                double upward = (this.jumping || this.mountJumpStrength > 0.0f) ? 0.08 : 0.02;
                this.setVelocity(this.getVelocity().x, Math.max(this.getVelocity().y, upward), this.getVelocity().z);
                this.mountJumpStrength = 0.0f;
            }
        }
    }

    @Override
    protected int computeFallDamage(double fallDistance, float damageMultiplier) {
        return super.computeFallDamage(Math.max(0.0, fallDistance - 6.0), damageMultiplier);
    }

    protected void executeMountJump(float strength, Vec3d movementInput) {
        double jumpY = (0.42 + strength * 0.40) * 1.5;
        Vec3d velocity = this.getVelocity();
        this.setVelocity(velocity.x, jumpY, velocity.z);
        this.velocityDirty = true;
        if (movementInput.z > 0.0) {
            float f = MathHelper.sin(this.getYaw() * ((float) Math.PI / 180.0f));
            float g = MathHelper.cos(this.getYaw() * ((float) Math.PI / 180.0f));
            this.setVelocity(this.getVelocity().add(-0.35f * f * strength, 0.0, 0.35f * g * strength));
        }
        this.playSound(SoundEvents.ENTITY_WOLF_STEP, 1.0f, 0.8f);
    }

    @Override
    public void setJumpStrength(int strength) {
        if (!this.hasWolfArmor()) return;
        if (strength < 0) {
            strength = 0;
        } else {
            this.jumping = true;
        }
        this.mountJumpStrength = this.clampJumpStrength(strength);
    }

    @Override
    public boolean canJump() {
        return this.isTamed() && this.hasWolfArmor();
    }

    @Override
    public void startJumping(int height) {
        if (!this.hasWolfArmor()) return;
        this.jumping = true;
        this.mountJumpStrength = this.clampJumpStrength(height);
        this.playSound(SoundEvents.ENTITY_HORSE_JUMP, 0.4f, 1.2f);
    }

    @Override
    public void stopJumping() {
    }

    @Override
    public int getJumpCooldown() {
        return 3;
    }

    @Override
    protected Vec3d getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scale) {
        double y = this.isSitting() ? 1.40 : 1.70;
        double z = this.isSitting() ? 0.0 : 0.15;
        return new Vec3d(0.0, y, z).rotateY(-this.bodyYaw * ((float) Math.PI / 180.0f));
    }

    // ==========================================
    // INTERACTION & TAMING RITUAL
    // ==========================================

    private void completeTaming(PlayerEntity player) {
        this.setOwner(player);
        this.setTamed(true, true);
        this.navigation.stop();
        this.setTarget(null);
        this.stopAnger();
        this.getEntityWorld().sendEntityStatus(this, (byte) 7); // Heart particles
        this.playSound(this.getAmbientSound(), 1.0f, 1.0f);
        this.updateAttributesForTamed();

        if (player instanceof ServerPlayerEntity serverPlayer) {
            Criteria.TAME_ANIMAL.trigger(serverPlayer, this);
            try {
                var advEntry = serverPlayer.getEntityWorld().getServer().getAdvancementLoader()
                    .get(Identifier.of("vanilla_reforged", "tame_dire_wolf"));
                if (advEntry != null) {
                    serverPlayer.getAdvancementTracker().grantCriterion(advEntry, "tame_dire_wolf");
                }
            } catch (Exception ignored) {}
        }
    }

    public static boolean isWolfFood(ItemStack stack) {
        return stack.isOf(Items.BEEF)
            || stack.isOf(Items.COOKED_BEEF)
            || stack.isOf(Items.PORKCHOP)
            || stack.isOf(Items.COOKED_PORKCHOP)
            || stack.isOf(Items.MUTTON)
            || stack.isOf(Items.COOKED_MUTTON)
            || stack.isOf(Items.CHICKEN)
            || stack.isOf(Items.COOKED_CHICKEN)
            || stack.isOf(Items.RABBIT)
            || stack.isOf(Items.COOKED_RABBIT)
            || stack.isOf(Items.ROTTEN_FLESH)
            || stack.isIn(net.minecraft.registry.tag.ItemTags.WOLF_FOOD);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        // Client side prediction: return SUCCESS for any interaction so client swings hand and sends packet
        if (this.getEntityWorld().isClient()) {
            boolean eligible = this.isTamed() 
                || stack.isOf(Items.BONE) 
                || stack.isOf(Items.WOLF_ARMOR)
                || stack.isOf(Items.GOLDEN_CARROT)
                || stack.isOf(Items.LEAD)
                || stack.isOf(Items.NAME_TAG)
                || isWolfFood(stack)
                || stack.isOf(Items.SHEARS)
                || stack.isOf(Items.ARMADILLO_SCUTE)
                || (this.hasWolfArmor() && !player.shouldCancelInteraction())
                || stack.isEmpty();
            return eligible ? ActionResult.SUCCESS : super.interactMob(player, hand);
        }

        // ==========================================
        // 1. UNTAMED DIRE WOLF INTERACTION
        // ==========================================
        if (!this.isTamed()) {
            // Calm anger if interacting with food, bone, or armor
            if (stack.isOf(Items.BONE) || stack.isOf(Items.WOLF_ARMOR) || stack.isOf(Items.GOLDEN_CARROT) || isWolfFood(stack)) {
                this.stopAnger();
                this.setTarget(null);
            }

            // Creative mode instant tame with any taming item or Wolf Armor
            if (player.getAbilities().creativeMode) {
                if (stack.isOf(Items.WOLF_ARMOR)) {
                    this.completeTaming(player);
                    this.equipBodyArmor(stack.copyWithCount(1));
                    this.playSound(SoundEvents.ITEM_ARMOR_EQUIP_WOLF.value(), 1.0f, 1.0f);
                    return ActionResult.SUCCESS;
                }
                if (stack.isOf(Items.BONE) || stack.isOf(Items.GOLDEN_CARROT) || isWolfFood(stack)) {
                    this.completeTaming(player);
                    return ActionResult.SUCCESS;
                }
            }

            // Giving Wolf Armor directly to untamed wolf: immediately tames & equips!
            if (stack.isOf(Items.WOLF_ARMOR)) {
                this.completeTaming(player);
                this.equipBodyArmor(stack.copyWithCount(1));
                stack.decrementUnlessCreative(1, player);
                this.playSound(SoundEvents.ITEM_ARMOR_EQUIP_WOLF.value(), 1.0f, 1.0f);
                return ActionResult.SUCCESS;
            }

            int stage = this.getTamingStage();

            // Bone interaction
            if (stack.isOf(Items.BONE)) {
                stack.decrementUnlessCreative(1, player);
                this.playSound(SoundEvents.ENTITY_GENERIC_EAT.value(), 1.0f, 1.0f);

                // Step 1: Weakness active -> advances ritual guaranteed!
                if (stage == 0 && this.hasStatusEffect(StatusEffects.WEAKNESS)) {
                    this.setTamingStage(1);
                    if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
                        serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getBodyY(0.5), this.getZ(), 12, 0.4, 0.4, 0.4, 0.02);
                    }
                    return ActionResult.SUCCESS;
                }

                // If fed bone without weakness: has 33% chance to tame directly (vanilla wolf parity)
                if (this.random.nextFloat() < 0.33f) {
                    this.completeTaming(player);
                } else {
                    this.getEntityWorld().sendEntityStatus(this, (byte) 6); // Smoke particles
                    this.playSound(this.getAmbientSound(), 1.0f, 0.8f);
                }
                return ActionResult.SUCCESS;
            }

            // Meat interaction (Beef, Cooked Beef, Mutton, Porkchop, Chicken, Rabbit, Rotten Flesh, etc.)
            if (isWolfFood(stack)) {
                stack.decrementUnlessCreative(1, player);
                this.playSound(SoundEvents.ENTITY_GENERIC_EAT.value(), 1.0f, 1.0f);
                if (stage == 1) {
                    this.setTamingStage(2);
                    if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
                        serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getBodyY(0.5), this.getZ(), 12, 0.4, 0.4, 0.4, 0.02);
                    }
                } else {
                    // Small direct tame chance (20%) or smoke
                    if (this.random.nextFloat() < 0.20f) {
                        this.completeTaming(player);
                    } else {
                        this.getEntityWorld().sendEntityStatus(this, (byte) 6);
                        this.playSound(this.getAmbientSound(), 1.0f, 0.8f);
                    }
                }
                return ActionResult.SUCCESS;
            }

            // Golden Carrot interaction
            if (stack.isOf(Items.GOLDEN_CARROT)) {
                stack.decrementUnlessCreative(1, player);
                this.playSound(SoundEvents.ENTITY_GENERIC_EAT.value(), 1.0f, 1.0f);
                if (stage == 2) {
                    if (this.random.nextDouble() < ModConfig.DIRE_WOLF_TAMING_SUCCESS_CHANCE) {
                        this.completeTaming(player);
                    } else {
                        this.getEntityWorld().sendEntityStatus(this, (byte) 6); // Smoke
                        this.playSound(this.getAmbientSound(), 1.0f, 0.8f);
                    }
                } else {
                    if (this.random.nextFloat() < 0.25f) {
                        this.completeTaming(player);
                    } else {
                        this.getEntityWorld().sendEntityStatus(this, (byte) 6);
                        this.playSound(this.getAmbientSound(), 1.0f, 0.8f);
                    }
                }
                return ActionResult.SUCCESS;
            }

            // Fallback for leads, name tags, etc.
            return super.interactMob(player, hand);
        }

        // ==========================================
        // 2. TAMED DIRE WOLF INTERACTION
        // ==========================================
        // If unowned (e.g. from /summon), assign owner to player interacting
        if (this.getOwnerReference() == null) {
            this.setOwner(player);
        }

        if (this.isOwner(player) || player.getAbilities().creativeMode) {
            // 1. Equip Wolf Armor directly
            if (stack.isOf(Items.WOLF_ARMOR) && !this.hasWolfArmor()) {
                this.equipBodyArmor(stack.copyWithCount(1));
                stack.decrementUnlessCreative(1, player);
                this.playSound(SoundEvents.ITEM_ARMOR_EQUIP_WOLF.value(), 1.0f, 1.0f);
                return ActionResult.SUCCESS;
            }

            // 2. Remove Wolf Armor with Shears
            if (stack.isOf(Items.SHEARS) && this.hasWolfArmor()) {
                ItemStack armor = this.getBodyArmor().copy();
                this.equipBodyArmor(ItemStack.EMPTY);
                this.dropStack((ServerWorld) this.getEntityWorld(), armor);
                stack.damage(1, player, hand);
                this.playSound(SoundEvents.ITEM_ARMOR_UNEQUIP_WOLF, 1.0f, 1.0f);
                return ActionResult.SUCCESS;
            }

            // 3. Repair Wolf Armor with Armadillo Scute
            if (stack.isOf(Items.ARMADILLO_SCUTE) && this.hasWolfArmor() && this.getBodyArmor().isDamaged()) {
                stack.decrementUnlessCreative(1, player);
                ItemStack armor = this.getBodyArmor();
                int maxDamage = armor.getMaxDamage();
                int repair = (int) (maxDamage * 0.125f);
                armor.setDamage(Math.max(0, armor.getDamage() - repair));
                this.playSound(SoundEvents.ITEM_WOLF_ARMOR_REPAIR, 1.0f, 1.0f);
                return ActionResult.SUCCESS;
            }

            // 4. Healing food
            boolean isFood = isWolfFood(stack);
            if (isFood && this.getHealth() < this.getMaxHealth()) {
                this.heal(6.0f);
                stack.decrementUnlessCreative(1, player);
                this.playSound(SoundEvents.ENTITY_GENERIC_EAT.value(), 1.0f, 1.0f);
                this.getEntityWorld().sendEntityStatus(this, (byte) 7); // Hearts
                return ActionResult.SUCCESS;
            }

            // 5. Riding interaction: if wearing armor and not sneaking and not holding food/armor/shears/scute/lead
            if (this.hasWolfArmor() && !player.shouldCancelInteraction() && !isFood && !stack.isOf(Items.SHEARS) && !stack.isOf(Items.WOLF_ARMOR) && !stack.isOf(Items.ARMADILLO_SCUTE) && !stack.isOf(Items.LEAD)) {
                if (this.isSitting()) {
                    this.setSitting(false);
                }
                player.startRiding(this);
                return ActionResult.SUCCESS;
            }

            // 6. Sitting/Standing toggle: when sneaking or when without armor
            if (stack.isEmpty() || player.shouldCancelInteraction()) {
                this.setSitting(!this.isSitting());
                this.jumping = false;
                this.navigation.stop();
                this.setTarget(null);
                return ActionResult.SUCCESS;
            }
        }

        return super.interactMob(player, hand);
    }

    @Override
    protected boolean shouldDropLoot(ServerWorld world) {
        if (this.isTamed()) {
            return false;
        }
        return super.shouldDropLoot(world);
    }

    @Override
    protected void dropLoot(ServerWorld world, DamageSource damageSource, boolean causedByPlayer) {
        if (this.isTamed()) {
            return;
        }
        super.dropLoot(world, damageSource, causedByPlayer);
    }

    @Override
    protected void dropEquipment(ServerWorld world, DamageSource damageSource, boolean causedByPlayer) {
        super.dropEquipment(world, damageSource, causedByPlayer);
        if (this.hasWolfArmor()) {
            this.dropStack(world, this.getBodyArmor().copy());
            this.equipBodyArmor(ItemStack.EMPTY);
        }
    }

    @Override
    protected int getExperienceToDrop(ServerWorld world) {
        if (this.isTamed()) {
            return 0;
        }
        return ModConfig.DIRE_WOLF_XP_MIN + this.random.nextInt(ModConfig.DIRE_WOLF_XP_MAX - ModConfig.DIRE_WOLF_XP_MIN + 1);
    }

    // ==========================================
    // APEX BEAST AGE RULES (NO BABY DIRE WOLVES)
    // ==========================================

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    public void setBaby(boolean baby) {
        super.setBaby(false);
    }

    @Override
    public void setBreedingAge(int age) {
        if (age < 0) {
            age = 0;
        }
        super.setBreedingAge(age);
    }

    @Override
    public WolfEntity createChild(ServerWorld world, PassiveEntity entity) {
        DireWolfEntity child = ModEntities.DIRE_WOLF.create(world, SpawnReason.BREEDING);
        if (child != null) {
            child.setBaby(false);
            child.setBreedingAge(0);
            if (this.isTamed()) {
                child.setOwner(this.getOwner());
                child.setTamed(true, true);
                child.updateAttributesForTamed();
            }
            if (entity instanceof DireWolfEntity otherWolf) {
                if (this.random.nextBoolean()) {
                    child.setVariant(this.getVariant());
                } else {
                    child.setVariant(otherWolf.getVariant());
                }
            }
        }
        return child;
    }

    @Override
    public void setSitting(boolean sitting) {
        super.setSitting(sitting);
        this.calculateDimensions();
    }

    @Override
    public void setInSittingPose(boolean inSittingPose) {
        super.setInSittingPose(inSittingPose);
        this.calculateDimensions();
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (TAMEABLE_FLAGS.equals(data)) {
            this.calculateDimensions();
        }
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        if (this.isSitting()) {
            return super.getBaseDimensions(pose).scaled(1.0f, 0.85f);
        }
        return super.getBaseDimensions(pose);
    }
}
