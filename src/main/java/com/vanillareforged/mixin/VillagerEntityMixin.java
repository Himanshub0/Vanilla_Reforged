package com.vanillareforged.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity {

    @Unique private double lastTrackedX;
    @Unique private double lastTrackedY;
    @Unique private double lastTrackedZ;
    @Unique private int stuckTickCount = 0;
    @Unique private boolean mobilityInitialized = false;

    protected VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract VillagerData getVillagerData();

    /**
     * 1. Smooth 1-block auto-stepping:
     * Increases Villager step height from 0.6 to 1.0625 so villagers smoothly walk up
     * 1-block steps, stairs, slabs, and path elevations without awkward jumping or getting stuck.
     */
    @Inject(method = "createVillagerAttributes", at = @At("RETURN"), cancellable = true)
    private static void addVillagerMobilityAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        cir.setReturnValue(cir.getReturnValue().add(EntityAttributes.STEP_HEIGHT, 1.0625));
    }

    /**
     * 2. Ensure existing villagers in loaded worlds have smooth step height and door traversal enabled.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void ensureVillagerMobility(CallbackInfo ci) {
        if (!this.mobilityInitialized) {
            this.mobilityInitialized = true;
            EntityAttributeInstance stepAttr = this.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
            if (stepAttr != null && stepAttr.getBaseValue() < 1.0) {
                stepAttr.setBaseValue(1.0625);
            }

            EntityNavigation nav = this.getNavigation();
            if (nav != null) {
                nav.setCanOpenDoors(true);
                if (nav instanceof MobNavigation mobNav) {
                    mobNav.setCanWalkOverFences(true);
                }
            }
        }
    }

    /**
     * 3. Anti-stuck detection and smart panic flee pathing on mobTick.
     */
    @Inject(method = "mobTick", at = @At("TAIL"))
    private void onVillagerMobTick(ServerWorld world, CallbackInfo ci) {
        handleAntiStuckAndPanic(world);
    }

    @Unique
    private void handleAntiStuckAndPanic(ServerWorld world) {
        if (!this.isAlive() || this.isSleeping() || this.hasCustomer() || this.hasVehicle()) {
            this.stuckTickCount = 0;
            this.lastTrackedX = this.getX();
            this.lastTrackedY = this.getY();
            this.lastTrackedZ = this.getZ();
            return;
        }

        double dx = this.getX() - this.lastTrackedX;
        double dy = this.getY() - this.lastTrackedY;
        double dz = this.getZ() - this.lastTrackedZ;
        double distSq = dx * dx + dy * dy + dz * dz;

        this.lastTrackedX = this.getX();
        this.lastTrackedY = this.getY();
        this.lastTrackedZ = this.getZ();

        EntityNavigation nav = this.getNavigation();
        boolean tryingToMove = nav != null && !nav.isIdle();

        // Check for nearby hostiles (Zombies, Creepers, Skeletons)
        Optional<LivingEntity> nearestHostile = this.getBrain().getOptionalMemory(MemoryModuleType.NEAREST_HOSTILE);
        if (nearestHostile.isEmpty()) {
            nearestHostile = this.getBrain().getOptionalMemory(MemoryModuleType.HURT_BY_ENTITY);
        }

        boolean inDanger = nearestHostile.isPresent() && nearestHostile.get().isAlive()
                && nearestHostile.get().squaredDistanceTo(this) < 144.0; // within 12 blocks

        // Smart flee: If hostile is near and villager is idle or walking slowly, trigger flee immediately
        if (inDanger && nearestHostile.isPresent()) {
            Optional<WalkTarget> currentWalk = this.getBrain().getOptionalMemory(MemoryModuleType.WALK_TARGET);
            if (currentWalk.isEmpty() || currentWalk.get().getSpeed() < 0.7f) {
                LivingEntity threat = nearestHostile.get();
                Vec3d fleePos = NoPenaltyTargeting.findFrom((VillagerEntity) (Object) this, 12, 4, threat.getEntityPos());
                if (fleePos != null) {
                    this.getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(fleePos, 0.75f, 0));
                    if (nav != null) {
                        nav.startMovingTo(fleePos.x, fleePos.y, fleePos.z, 0.75);
                    }
                }
            }
        }

        if (tryingToMove && distSq < 0.0004) {
            this.stuckTickCount++;
        } else {
            this.stuckTickCount = 0;
        }

        // Emergency threshold: 10 ticks (0.5s) if in danger, 20 ticks (1.0s) normally
        int threshold = inDanger ? 10 : 20;

        if (this.stuckTickCount >= threshold) {
            if (nav != null) {
                nav.stop();
            }
            this.getBrain().forget(MemoryModuleType.WALK_TARGET);
            this.getBrain().forget(MemoryModuleType.PATH);
            this.getBrain().remember(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, world.getTime());

            if (inDanger && nearestHostile.isPresent()) {
                LivingEntity threat = nearestHostile.get();
                Vec3d fleePos = NoPenaltyTargeting.findFrom((VillagerEntity) (Object) this, 12, 4, threat.getEntityPos());
                if (fleePos != null) {
                    this.getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(fleePos, 0.75f, 0));
                    if (nav != null) {
                        nav.startMovingTo(fleePos.x, fleePos.y, fleePos.z, 0.75);
                    }
                } else {
                    // Cornered: small physical impulse away from threat to unwedge
                    Vec3d away = this.getEntityPos().subtract(threat.getEntityPos()).normalize().multiply(0.25);
                    this.setVelocity(away.x, 0.35, away.z);
                    this.velocityDirty = true;
                }
            } else {
                // Normal wander / POI unstuck: pick an accessible reachable position
                Vec3d openPos = NoPenaltyTargeting.find((VillagerEntity) (Object) this, 6, 3);
                if (openPos != null) {
                    this.getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(openPos, 0.5f, 1));
                    if (nav != null) {
                        nav.startMovingTo(openPos.x, openPos.y, openPos.z, 0.5);
                    }
                } else {
                    this.jump();
                }
            }

            this.stuckTickCount = 0;
        }
    }

    @Inject(method = "fillRecipes", at = @At("TAIL"))
    private void onFillRecipes(ServerWorld world, CallbackInfo ci) {
        filterDiamondTrades();
        convertLibrarianEmeraldsToGold();
    }

    @Inject(method = "prepareOffersFor", at = @At("TAIL"))
    private void onPrepareOffersFor(PlayerEntity player, CallbackInfo ci) {
        filterDiamondTrades();
        convertLibrarianEmeraldsToGold();
    }

    @Unique
    private void filterDiamondTrades() {
        RegistryEntry<VillagerProfession> profEntry = this.getVillagerData().profession();
        if (profEntry.matchesKey(VillagerProfession.ARMORER) ||
            profEntry.matchesKey(VillagerProfession.TOOLSMITH) ||
            profEntry.matchesKey(VillagerProfession.WEAPONSMITH)) {

            TradeOfferList offers = this.getOffers();
            if (offers != null) {
                offers.removeIf(offer -> com.vanillareforged.economy.VillagerTradeManager.isDiamondEquipment(offer.getSellItem()));
            }
        }
    }

    @Unique
    private void convertLibrarianEmeraldsToGold() {
        RegistryEntry<VillagerProfession> profEntry = this.getVillagerData().profession();
        if (profEntry.matchesKey(VillagerProfession.LIBRARIAN)) {
            TradeOfferList offers = this.getOffers();
            if (offers != null) {
                for (int i = 0; i < offers.size(); i++) {
                    TradeOffer offer = offers.get(i);
                    TradeOffer converted = convertOfferEmeraldsToGold(offer);
                    if (converted != null) {
                        offers.set(i, converted);
                    }
                }
            }
        }
    }

    @Unique
    private static TradeOffer convertOfferEmeraldsToGold(TradeOffer offer) {
        boolean changed = false;

        TradedItem first = offer.getFirstBuyItem();
        TradedItem newFirst = first;
        if (first.item().value() == Items.EMERALD) {
            newFirst = new TradedItem(Items.GOLD_INGOT, first.count());
            changed = true;
        }

        Optional<TradedItem> second = offer.getSecondBuyItem();
        Optional<TradedItem> newSecond = second;
        if (second.isPresent() && second.get().item().value() == Items.EMERALD) {
            newSecond = Optional.of(new TradedItem(Items.GOLD_INGOT, second.get().count()));
            changed = true;
        }

        ItemStack sell = offer.getSellItem();
        ItemStack newSell = sell;
        if (sell.isOf(Items.EMERALD)) {
            newSell = new ItemStack(Items.GOLD_INGOT, sell.getCount());
            changed = true;
        }

        if (changed) {
            TradeOffer converted = new TradeOffer(
                newFirst,
                newSecond,
                newSell,
                offer.getUses(),
                offer.getMaxUses(),
                offer.getMerchantExperience(),
                offer.getPriceMultiplier(),
                offer.getDemandBonus()
            );
            converted.setSpecialPrice(offer.getSpecialPrice());
            return converted;
        }
        return null;
    }
}
