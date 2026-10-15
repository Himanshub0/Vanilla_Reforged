package com.vanillareforged.entity;

import com.vanillareforged.config.ModConfig;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Phase 3 Section 3: Zombie Family Armor Distribution
 * - 25% chance (ModConfig.ZOMBIE_ARMOR_CHANCE) of spawning with armor.
 * - Materials: 45% Leather, 35% Chainmail, 20% Iron.
 * - Gold, Diamond, Netherite armor are strictly 0% (FORBIDDEN on ordinary zombies).
 * - Uniform tier per set!
 * - Number of pieces: 50% 1 piece, 30% 2 pieces, 15% 3 pieces, 5% 4 pieces.
 * - On death: 8.5% independent chance (ModConfig.ZOMBIE_ARMOR_DROP_CHANCE) per worn armor piece.
 */
public final class ZombieArmorManager {

    private ZombieArmorManager() {}

    public static void applySpawnArmor(ZombieEntity zombie, Random random) {
        if (zombie instanceof KingZombieEntity) {
            return; // King Zombie manages its own armor
        }

        // Strip any existing armor to ensure strictly NO gold, diamond, or netherite on standard zombies
        zombie.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
        zombie.equipStack(EquipmentSlot.CHEST, ItemStack.EMPTY);
        zombie.equipStack(EquipmentSlot.LEGS, ItemStack.EMPTY);
        zombie.equipStack(EquipmentSlot.FEET, ItemStack.EMPTY);

        // 25% chance of spawning with armor
        if (random.nextFloat() >= (float) ModConfig.ZOMBIE_ARMOR_CHANCE) {
            return;
        }

        // Material tier selection
        // 45% Leather, 35% Chainmail, 20% Iron
        float tierRoll = random.nextFloat();
        ArmorMaterialTier tier;
        if (tierRoll < (float) ModConfig.ZOMBIE_ARMOR_LEATHER_WEIGHT) {
            tier = ArmorMaterialTier.LEATHER;
        } else if (tierRoll < (float) (ModConfig.ZOMBIE_ARMOR_LEATHER_WEIGHT + ModConfig.ZOMBIE_ARMOR_CHAINMAIL_WEIGHT)) {
            tier = ArmorMaterialTier.CHAINMAIL;
        } else {
            tier = ArmorMaterialTier.IRON;
        }

        // Number of pieces: 50% 1 piece, 30% 2 pieces, 15% 3 pieces, 5% 4 pieces
        float pieceRoll = random.nextFloat();
        int pieceCount;
        if (pieceRoll < 0.50f) {
            pieceCount = 1;
        } else if (pieceRoll < 0.80f) {
            pieceCount = 2;
        } else if (pieceRoll < 0.95f) {
            pieceCount = 3;
        } else {
            pieceCount = 4;
        }

        // Select distinct armor slots randomly
        List<EquipmentSlot> slots = new ArrayList<>(List.of(
            EquipmentSlot.FEET,
            EquipmentSlot.LEGS,
            EquipmentSlot.CHEST,
            EquipmentSlot.HEAD
        ));
        Collections.shuffle(slots, new java.util.Random(random.nextLong()));

        for (int i = 0; i < pieceCount; i++) {
            EquipmentSlot slot = slots.get(i);
            ItemStack item = getArmorItem(tier, slot);
            if (item != null) {
                zombie.equipStack(slot, item);
                zombie.setEquipmentDropChance(slot, (float) ModConfig.ZOMBIE_ARMOR_DROP_CHANCE);
            }
        }
    }

    private static ItemStack getArmorItem(ArmorMaterialTier tier, EquipmentSlot slot) {
        return switch (tier) {
            case LEATHER -> switch (slot) {
                case HEAD -> new ItemStack(Items.LEATHER_HELMET);
                case CHEST -> new ItemStack(Items.LEATHER_CHESTPLATE);
                case LEGS -> new ItemStack(Items.LEATHER_LEGGINGS);
                case FEET -> new ItemStack(Items.LEATHER_BOOTS);
                default -> null;
            };
            case CHAINMAIL -> switch (slot) {
                case HEAD -> new ItemStack(Items.CHAINMAIL_HELMET);
                case CHEST -> new ItemStack(Items.CHAINMAIL_CHESTPLATE);
                case LEGS -> new ItemStack(Items.CHAINMAIL_LEGGINGS);
                case FEET -> new ItemStack(Items.CHAINMAIL_BOOTS);
                default -> null;
            };
            case IRON -> switch (slot) {
                case HEAD -> new ItemStack(Items.IRON_HELMET);
                case CHEST -> new ItemStack(Items.IRON_CHESTPLATE);
                case LEGS -> new ItemStack(Items.IRON_LEGGINGS);
                case FEET -> new ItemStack(Items.IRON_BOOTS);
                default -> null;
            };
        };
    }

    public static void handleZombieArmorDeathDrop(ZombieEntity zombie, net.minecraft.server.world.ServerWorld world) {
        if (zombie instanceof KingZombieEntity) {
            return;
        }

        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack stack = zombie.getEquippedStack(slot);
            if (!stack.isEmpty()) {
                if (world.getRandom().nextFloat() < (float) ModConfig.ZOMBIE_ARMOR_DROP_CHANCE) {
                    ItemStack dropStack = stack.copy();
                    if (dropStack.isDamageable()) {
                        int maxDamage = dropStack.getMaxDamage();
                        int damage = world.getRandom().nextInt(maxDamage);
                        dropStack.setDamage(Math.max(1, damage));
                    }
                    zombie.dropStack(world, dropStack);
                }
                // Clear slot so vanilla doesn't duplicate the drop
                zombie.equipStack(slot, ItemStack.EMPTY);
            }
        }
    }

    public enum ArmorMaterialTier {
        LEATHER,
        CHAINMAIL,
        IRON
    }
}
