package com.vanillareforged.balance;

import com.vanillareforged.mixin.ItemAccessor;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.EnchantableComponent;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.util.Identifier;

import java.util.*;

public final class DurabilityManager {

    // === 1. Tool Durabilities ===
    public static final int WOOD_TOOL_DURABILITY = 59;
    public static final int STONE_TOOL_DURABILITY = 131;
    public static final int COPPER_TOOL_DURABILITY = 190;
    public static final int IRON_TOOL_DURABILITY = 250;
    public static final int GOLD_TOOL_DURABILITY = 1170;
    public static final int DIAMOND_TOOL_DURABILITY = 1560;
    public static final int NETHERITE_TOOL_DURABILITY = 2030;

    // === 2. Tool Mining Speeds ===
    public static final float WOOD_MINING_SPEED = 2.0f;
    public static final float STONE_MINING_SPEED = 4.0f;
    public static final float COPPER_MINING_SPEED = 5.0f;
    public static final float IRON_MINING_SPEED = 6.0f;
    public static final float GOLD_MINING_SPEED = 10.0f;
    public static final float DIAMOND_MINING_SPEED = 12.0f;
    public static final float NETHERITE_MINING_SPEED = 9.0f;

    // === 3. Tool Enchantabilities ===
    public static final int WOOD_TOOL_ENCHANTABILITY = 15;
    public static final int STONE_TOOL_ENCHANTABILITY = 5;
    public static final int COPPER_TOOL_ENCHANTABILITY = 13;
    public static final int IRON_TOOL_ENCHANTABILITY = 14;
    public static final int GOLD_TOOL_ENCHANTABILITY = 22;
    public static final int DIAMOND_TOOL_ENCHANTABILITY = 24;
    public static final int NETHERITE_TOOL_ENCHANTABILITY = 16;

    // === 4. Armor Durabilities ===
    public static final int LEATHER_HELMET_DURABILITY = 55;
    public static final int LEATHER_CHESTPLATE_DURABILITY = 80;
    public static final int LEATHER_LEGGINGS_DURABILITY = 75;
    public static final int LEATHER_BOOTS_DURABILITY = 65;

    public static final int COPPER_HELMET_DURABILITY = 121;
    public static final int COPPER_CHESTPLATE_DURABILITY = 176;
    public static final int COPPER_LEGGINGS_DURABILITY = 165;
    public static final int COPPER_BOOTS_DURABILITY = 143;

    public static final int CHAINMAIL_HELMET_DURABILITY = 165;
    public static final int CHAINMAIL_CHESTPLATE_DURABILITY = 240;
    public static final int CHAINMAIL_LEGGINGS_DURABILITY = 225;
    public static final int CHAINMAIL_BOOTS_DURABILITY = 195;

    public static final int IRON_HELMET_DURABILITY = 165;
    public static final int IRON_CHESTPLATE_DURABILITY = 240;
    public static final int IRON_LEGGINGS_DURABILITY = 225;
    public static final int IRON_BOOTS_DURABILITY = 195;

    public static final int GOLD_HELMET_DURABILITY = 275;
    public static final int GOLD_CHESTPLATE_DURABILITY = 400;
    public static final int GOLD_LEGGINGS_DURABILITY = 370;
    public static final int GOLD_BOOTS_DURABILITY = 325;

    public static final int DIAMOND_HELMET_DURABILITY = 365;
    public static final int DIAMOND_CHESTPLATE_DURABILITY = 530;
    public static final int DIAMOND_LEGGINGS_DURABILITY = 495;
    public static final int DIAMOND_BOOTS_DURABILITY = 430;

    public static final int NETHERITE_HELMET_DURABILITY = 475;
    public static final int NETHERITE_CHESTPLATE_DURABILITY = 690;
    public static final int NETHERITE_LEGGINGS_DURABILITY = 645;
    public static final int NETHERITE_BOOTS_DURABILITY = 560;

    // === 5. Armor Points (Protection) ===
    public static final double GOLD_HELMET_ARMOR = 2.0;
    public static final double GOLD_CHESTPLATE_ARMOR = 6.0;
    public static final double GOLD_LEGGINGS_ARMOR = 5.0;
    public static final double GOLD_BOOTS_ARMOR = 2.0;

    public static final double NETHERITE_HELMET_ARMOR = 4.0;
    public static final double NETHERITE_CHESTPLATE_ARMOR = 10.0;
    public static final double NETHERITE_LEGGINGS_ARMOR = 8.0;
    public static final double NETHERITE_BOOTS_ARMOR = 4.0;

    // === 6. Armor Toughness ===
    public static final double GOLD_ARMOR_TOUGHNESS = 1.5;
    public static final double DIAMOND_ARMOR_TOUGHNESS = 2.0;
    public static final double NETHERITE_ARMOR_TOUGHNESS = 2.6;

    // === 7. Armor Enchantability ===
    public static final int LEATHER_ARMOR_ENCHANTABILITY = 15;
    public static final int COPPER_ARMOR_ENCHANTABILITY = 8;
    public static final int CHAINMAIL_ARMOR_ENCHANTABILITY = 12;
    public static final int IRON_ARMOR_ENCHANTABILITY = 9;
    public static final int GOLD_ARMOR_ENCHANTABILITY = 22;
    public static final int DIAMOND_ARMOR_ENCHANTABILITY = 24;
    public static final int NETHERITE_ARMOR_ENCHANTABILITY = 16;

    // === 8. Armor Knockback Resistance ===
    public static final double NETHERITE_KNOCKBACK_RESISTANCE = 0.1;

    // === 9. Special Equipment ===
    public static final int TURTLE_HELMET_DURABILITY = 275;
    public static final int ELYTRA_DURABILITY = 432;
    public static final int SHIELD_DURABILITY = 336;

    public record ToolStats(int durability, double damage, double attackSpeed, Float miningSpeed, int enchantability) {}
    public record ArmorStats(int durability, double armor, double toughness, double knockback, int enchantability, EquipmentType type) {}

    private static Map<Item, Integer> OVERRIDE_DURABILITY;
    private static Map<Item, ToolStats> TOOL_STATS;
    private static Map<Item, ArmorStats> ARMOR_STATS;

    public static synchronized Map<Item, Integer> getOverrides() {
        if (OVERRIDE_DURABILITY == null) {
            initData();
        }
        return OVERRIDE_DURABILITY;
    }

    public static synchronized Map<Item, ToolStats> getToolStats() {
        if (TOOL_STATS == null) {
            initData();
        }
        return TOOL_STATS;
    }

    public static synchronized Map<Item, ArmorStats> getArmorStats() {
        if (ARMOR_STATS == null) {
            initData();
        }
        return ARMOR_STATS;
    }

    private static synchronized void initData() {
        if (OVERRIDE_DURABILITY != null) return;

        Map<Item, Integer> durMap = new LinkedHashMap<>();
        Map<Item, ToolStats> toolMap = new LinkedHashMap<>();
        Map<Item, ArmorStats> armorMap = new LinkedHashMap<>();

        // 1. Wood Tools
        registerTool(toolMap, durMap, Items.WOODEN_SWORD, new ToolStats(WOOD_TOOL_DURABILITY, 4.0, 1.6, null, WOOD_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.WOODEN_AXE, new ToolStats(WOOD_TOOL_DURABILITY, 7.0, 0.8, WOOD_MINING_SPEED, WOOD_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.WOODEN_PICKAXE, new ToolStats(WOOD_TOOL_DURABILITY, 2.0, 1.2, WOOD_MINING_SPEED, WOOD_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.WOODEN_SHOVEL, new ToolStats(WOOD_TOOL_DURABILITY, 2.5, 1.0, WOOD_MINING_SPEED, WOOD_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.WOODEN_HOE, new ToolStats(WOOD_TOOL_DURABILITY, 1.0, 1.0, WOOD_MINING_SPEED, WOOD_TOOL_ENCHANTABILITY));

        // 2. Stone Tools
        registerTool(toolMap, durMap, Items.STONE_SWORD, new ToolStats(STONE_TOOL_DURABILITY, 5.0, 1.6, null, STONE_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.STONE_AXE, new ToolStats(STONE_TOOL_DURABILITY, 9.0, 0.8, STONE_MINING_SPEED, STONE_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.STONE_PICKAXE, new ToolStats(STONE_TOOL_DURABILITY, 3.0, 1.2, STONE_MINING_SPEED, STONE_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.STONE_SHOVEL, new ToolStats(STONE_TOOL_DURABILITY, 3.5, 1.0, STONE_MINING_SPEED, STONE_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.STONE_HOE, new ToolStats(STONE_TOOL_DURABILITY, 1.0, 2.0, STONE_MINING_SPEED, STONE_TOOL_ENCHANTABILITY));

        // 3. Copper Tools
        registerTool(toolMap, durMap, Items.COPPER_SWORD, new ToolStats(COPPER_TOOL_DURABILITY, 5.0, 1.6, null, COPPER_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.COPPER_AXE, new ToolStats(COPPER_TOOL_DURABILITY, 9.0, 0.8, COPPER_MINING_SPEED, COPPER_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.COPPER_PICKAXE, new ToolStats(COPPER_TOOL_DURABILITY, 3.0, 1.2, COPPER_MINING_SPEED, COPPER_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.COPPER_SHOVEL, new ToolStats(COPPER_TOOL_DURABILITY, 3.5, 1.0, COPPER_MINING_SPEED, COPPER_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.COPPER_HOE, new ToolStats(COPPER_TOOL_DURABILITY, 1.0, 2.5, COPPER_MINING_SPEED, COPPER_TOOL_ENCHANTABILITY));

        // 4. Iron Tools
        registerTool(toolMap, durMap, Items.IRON_SWORD, new ToolStats(IRON_TOOL_DURABILITY, 5.0, 1.6, null, IRON_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.IRON_AXE, new ToolStats(IRON_TOOL_DURABILITY, 8.0, 0.9, IRON_MINING_SPEED, IRON_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.IRON_PICKAXE, new ToolStats(IRON_TOOL_DURABILITY, 4.0, 1.2, IRON_MINING_SPEED, IRON_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.IRON_SHOVEL, new ToolStats(IRON_TOOL_DURABILITY, 4.5, 1.0, IRON_MINING_SPEED, IRON_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.IRON_HOE, new ToolStats(IRON_TOOL_DURABILITY, 1.0, 3.0, IRON_MINING_SPEED, IRON_TOOL_ENCHANTABILITY));

        // 5. Gold Tools
        registerTool(toolMap, durMap, Items.GOLDEN_SWORD, new ToolStats(GOLD_TOOL_DURABILITY, 6.0, 1.7, null, GOLD_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.GOLDEN_SPEAR, new ToolStats(GOLD_TOOL_DURABILITY, 3.6, 1.9, null, GOLD_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.GOLDEN_AXE, new ToolStats(GOLD_TOOL_DURABILITY, 9.0, 1.0, GOLD_MINING_SPEED, GOLD_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.GOLDEN_PICKAXE, new ToolStats(GOLD_TOOL_DURABILITY, 5.0, 1.3, GOLD_MINING_SPEED, GOLD_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.GOLDEN_SHOVEL, new ToolStats(GOLD_TOOL_DURABILITY, 5.0, 1.1, GOLD_MINING_SPEED, GOLD_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.GOLDEN_HOE, new ToolStats(GOLD_TOOL_DURABILITY, 2.0, 3.5, GOLD_MINING_SPEED, GOLD_TOOL_ENCHANTABILITY));

        // 6. Diamond Tools
        registerTool(toolMap, durMap, Items.DIAMOND_SWORD, new ToolStats(DIAMOND_TOOL_DURABILITY, 7.0, 1.8, null, DIAMOND_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.DIAMOND_AXE, new ToolStats(DIAMOND_TOOL_DURABILITY, 10.0, 1.1, DIAMOND_MINING_SPEED, DIAMOND_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.DIAMOND_PICKAXE, new ToolStats(DIAMOND_TOOL_DURABILITY, 6.0, 1.4, DIAMOND_MINING_SPEED, DIAMOND_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.DIAMOND_SHOVEL, new ToolStats(DIAMOND_TOOL_DURABILITY, 6.0, 1.2, DIAMOND_MINING_SPEED, DIAMOND_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.DIAMOND_HOE, new ToolStats(DIAMOND_TOOL_DURABILITY, 3.0, 4.0, DIAMOND_MINING_SPEED, DIAMOND_TOOL_ENCHANTABILITY));

        // 7. Netherite Tools
        registerTool(toolMap, durMap, Items.NETHERITE_SWORD, new ToolStats(NETHERITE_TOOL_DURABILITY, 9.0, 1.8, null, NETHERITE_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.NETHERITE_AXE, new ToolStats(NETHERITE_TOOL_DURABILITY, 12.0, 1.1, NETHERITE_MINING_SPEED, NETHERITE_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.NETHERITE_PICKAXE, new ToolStats(NETHERITE_TOOL_DURABILITY, 7.0, 1.4, NETHERITE_MINING_SPEED, NETHERITE_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.NETHERITE_SHOVEL, new ToolStats(NETHERITE_TOOL_DURABILITY, 8.0, 1.2, NETHERITE_MINING_SPEED, NETHERITE_TOOL_ENCHANTABILITY));
        registerTool(toolMap, durMap, Items.NETHERITE_HOE, new ToolStats(NETHERITE_TOOL_DURABILITY, 1.0, 4.0, NETHERITE_MINING_SPEED, NETHERITE_TOOL_ENCHANTABILITY));

        // 8. Leather Armor
        registerArmor(armorMap, durMap, Items.LEATHER_HELMET, new ArmorStats(LEATHER_HELMET_DURABILITY, 1.0, 0.0, 0.0, LEATHER_ARMOR_ENCHANTABILITY, EquipmentType.HELMET));
        registerArmor(armorMap, durMap, Items.LEATHER_CHESTPLATE, new ArmorStats(LEATHER_CHESTPLATE_DURABILITY, 3.0, 0.0, 0.0, LEATHER_ARMOR_ENCHANTABILITY, EquipmentType.CHESTPLATE));
        registerArmor(armorMap, durMap, Items.LEATHER_LEGGINGS, new ArmorStats(LEATHER_LEGGINGS_DURABILITY, 2.0, 0.0, 0.0, LEATHER_ARMOR_ENCHANTABILITY, EquipmentType.LEGGINGS));
        registerArmor(armorMap, durMap, Items.LEATHER_BOOTS, new ArmorStats(LEATHER_BOOTS_DURABILITY, 1.0, 0.0, 0.0, LEATHER_ARMOR_ENCHANTABILITY, EquipmentType.BOOTS));

        // 9. Copper Armor
        registerArmor(armorMap, durMap, Items.COPPER_HELMET, new ArmorStats(COPPER_HELMET_DURABILITY, 2.0, 0.0, 0.0, COPPER_ARMOR_ENCHANTABILITY, EquipmentType.HELMET));
        registerArmor(armorMap, durMap, Items.COPPER_CHESTPLATE, new ArmorStats(COPPER_CHESTPLATE_DURABILITY, 4.0, 0.0, 0.0, COPPER_ARMOR_ENCHANTABILITY, EquipmentType.CHESTPLATE));
        registerArmor(armorMap, durMap, Items.COPPER_LEGGINGS, new ArmorStats(COPPER_LEGGINGS_DURABILITY, 3.0, 0.0, 0.0, COPPER_ARMOR_ENCHANTABILITY, EquipmentType.LEGGINGS));
        registerArmor(armorMap, durMap, Items.COPPER_BOOTS, new ArmorStats(COPPER_BOOTS_DURABILITY, 1.0, 0.0, 0.0, COPPER_ARMOR_ENCHANTABILITY, EquipmentType.BOOTS));

        // 10. Chainmail Armor
        registerArmor(armorMap, durMap, Items.CHAINMAIL_HELMET, new ArmorStats(CHAINMAIL_HELMET_DURABILITY, 2.0, 0.0, 0.0, CHAINMAIL_ARMOR_ENCHANTABILITY, EquipmentType.HELMET));
        registerArmor(armorMap, durMap, Items.CHAINMAIL_CHESTPLATE, new ArmorStats(CHAINMAIL_CHESTPLATE_DURABILITY, 5.0, 0.0, 0.0, CHAINMAIL_ARMOR_ENCHANTABILITY, EquipmentType.CHESTPLATE));
        registerArmor(armorMap, durMap, Items.CHAINMAIL_LEGGINGS, new ArmorStats(CHAINMAIL_LEGGINGS_DURABILITY, 4.0, 0.0, 0.0, CHAINMAIL_ARMOR_ENCHANTABILITY, EquipmentType.LEGGINGS));
        registerArmor(armorMap, durMap, Items.CHAINMAIL_BOOTS, new ArmorStats(CHAINMAIL_BOOTS_DURABILITY, 1.0, 0.0, 0.0, CHAINMAIL_ARMOR_ENCHANTABILITY, EquipmentType.BOOTS));

        // 11. Iron Armor
        registerArmor(armorMap, durMap, Items.IRON_HELMET, new ArmorStats(IRON_HELMET_DURABILITY, 2.0, 0.0, 0.0, IRON_ARMOR_ENCHANTABILITY, EquipmentType.HELMET));
        registerArmor(armorMap, durMap, Items.IRON_CHESTPLATE, new ArmorStats(IRON_CHESTPLATE_DURABILITY, 6.0, 0.0, 0.0, IRON_ARMOR_ENCHANTABILITY, EquipmentType.CHESTPLATE));
        registerArmor(armorMap, durMap, Items.IRON_LEGGINGS, new ArmorStats(IRON_LEGGINGS_DURABILITY, 5.0, 0.0, 0.0, IRON_ARMOR_ENCHANTABILITY, EquipmentType.LEGGINGS));
        registerArmor(armorMap, durMap, Items.IRON_BOOTS, new ArmorStats(IRON_BOOTS_DURABILITY, 2.0, 0.0, 0.0, IRON_ARMOR_ENCHANTABILITY, EquipmentType.BOOTS));

        // 12. Gold Armor
        registerArmor(armorMap, durMap, Items.GOLDEN_HELMET, new ArmorStats(GOLD_HELMET_DURABILITY, GOLD_HELMET_ARMOR, GOLD_ARMOR_TOUGHNESS, 0.0, GOLD_ARMOR_ENCHANTABILITY, EquipmentType.HELMET));
        registerArmor(armorMap, durMap, Items.GOLDEN_CHESTPLATE, new ArmorStats(GOLD_CHESTPLATE_DURABILITY, GOLD_CHESTPLATE_ARMOR, GOLD_ARMOR_TOUGHNESS, 0.0, GOLD_ARMOR_ENCHANTABILITY, EquipmentType.CHESTPLATE));
        registerArmor(armorMap, durMap, Items.GOLDEN_LEGGINGS, new ArmorStats(GOLD_LEGGINGS_DURABILITY, GOLD_LEGGINGS_ARMOR, GOLD_ARMOR_TOUGHNESS, 0.0, GOLD_ARMOR_ENCHANTABILITY, EquipmentType.LEGGINGS));
        registerArmor(armorMap, durMap, Items.GOLDEN_BOOTS, new ArmorStats(GOLD_BOOTS_DURABILITY, GOLD_BOOTS_ARMOR, GOLD_ARMOR_TOUGHNESS, 0.0, GOLD_ARMOR_ENCHANTABILITY, EquipmentType.BOOTS));

        // 13. Diamond Armor
        registerArmor(armorMap, durMap, Items.DIAMOND_HELMET, new ArmorStats(DIAMOND_HELMET_DURABILITY, 3.0, DIAMOND_ARMOR_TOUGHNESS, 0.0, DIAMOND_ARMOR_ENCHANTABILITY, EquipmentType.HELMET));
        registerArmor(armorMap, durMap, Items.DIAMOND_CHESTPLATE, new ArmorStats(DIAMOND_CHESTPLATE_DURABILITY, 8.0, DIAMOND_ARMOR_TOUGHNESS, 0.0, DIAMOND_ARMOR_ENCHANTABILITY, EquipmentType.CHESTPLATE));
        registerArmor(armorMap, durMap, Items.DIAMOND_LEGGINGS, new ArmorStats(DIAMOND_LEGGINGS_DURABILITY, 6.0, DIAMOND_ARMOR_TOUGHNESS, 0.0, DIAMOND_ARMOR_ENCHANTABILITY, EquipmentType.LEGGINGS));
        registerArmor(armorMap, durMap, Items.DIAMOND_BOOTS, new ArmorStats(DIAMOND_BOOTS_DURABILITY, 3.0, DIAMOND_ARMOR_TOUGHNESS, 0.0, DIAMOND_ARMOR_ENCHANTABILITY, EquipmentType.BOOTS));

        // 14. Netherite Armor
        registerArmor(armorMap, durMap, Items.NETHERITE_HELMET, new ArmorStats(NETHERITE_HELMET_DURABILITY, NETHERITE_HELMET_ARMOR, NETHERITE_ARMOR_TOUGHNESS, NETHERITE_KNOCKBACK_RESISTANCE, NETHERITE_ARMOR_ENCHANTABILITY, EquipmentType.HELMET));
        registerArmor(armorMap, durMap, Items.NETHERITE_CHESTPLATE, new ArmorStats(NETHERITE_CHESTPLATE_DURABILITY, NETHERITE_CHESTPLATE_ARMOR, NETHERITE_ARMOR_TOUGHNESS, NETHERITE_KNOCKBACK_RESISTANCE, NETHERITE_ARMOR_ENCHANTABILITY, EquipmentType.CHESTPLATE));
        registerArmor(armorMap, durMap, Items.NETHERITE_LEGGINGS, new ArmorStats(NETHERITE_LEGGINGS_DURABILITY, NETHERITE_LEGGINGS_ARMOR, NETHERITE_ARMOR_TOUGHNESS, NETHERITE_KNOCKBACK_RESISTANCE, NETHERITE_ARMOR_ENCHANTABILITY, EquipmentType.LEGGINGS));
        registerArmor(armorMap, durMap, Items.NETHERITE_BOOTS, new ArmorStats(NETHERITE_BOOTS_DURABILITY, NETHERITE_BOOTS_ARMOR, NETHERITE_ARMOR_TOUGHNESS, NETHERITE_KNOCKBACK_RESISTANCE, NETHERITE_ARMOR_ENCHANTABILITY, EquipmentType.BOOTS));

        // 15. Special Equipment
        registerArmor(armorMap, durMap, Items.TURTLE_HELMET, new ArmorStats(TURTLE_HELMET_DURABILITY, 2.0, 0.0, 0.0, 9, EquipmentType.HELMET));
        durMap.put(Items.ELYTRA, ELYTRA_DURABILITY);
        durMap.put(Items.SHIELD, SHIELD_DURABILITY);

        OVERRIDE_DURABILITY = Collections.unmodifiableMap(durMap);
        TOOL_STATS = Collections.unmodifiableMap(toolMap);
        ARMOR_STATS = Collections.unmodifiableMap(armorMap);
    }

    private static void registerTool(Map<Item, ToolStats> toolMap, Map<Item, Integer> durMap, Item item, ToolStats stats) {
        toolMap.put(item, stats);
        durMap.put(item, stats.durability());
    }

    private static void registerArmor(Map<Item, ArmorStats> armorMap, Map<Item, Integer> durMap, Item item, ArmorStats stats) {
        armorMap.put(item, stats);
        durMap.put(item, stats.durability());
    }

    public static void applyDurabilityChanges() {
        initData();

        // 1. Apply Tool Stats
        for (Map.Entry<Item, ToolStats> entry : TOOL_STATS.entrySet()) {
            Item item = entry.getKey();
            ToolStats stats = entry.getValue();

            ComponentMap current = item.getComponents();
            ComponentMap.Builder builder = ComponentMap.builder().addAll(current);

            // Durability
            builder.add(DataComponentTypes.MAX_DAMAGE, stats.durability());

            // Enchantability
            builder.add(DataComponentTypes.ENCHANTABLE, new EnchantableComponent(stats.enchantability()));

            // Attack Damage & Attack Speed
            builder.add(DataComponentTypes.ATTRIBUTE_MODIFIERS, createWeaponAttributes(stats.damage(), stats.attackSpeed()));

            // Mining Speed on ToolComponent (if applicable)
            if (stats.miningSpeed() != null) {
                ToolComponent currentTool = current.get(DataComponentTypes.TOOL);
                if (currentTool != null) {
                    List<ToolComponent.Rule> newRules = new ArrayList<>();
                    for (ToolComponent.Rule rule : currentTool.rules()) {
                        Optional<Float> speed = rule.speed().isPresent() ? Optional.of(stats.miningSpeed()) : Optional.empty();
                        newRules.add(new ToolComponent.Rule(rule.blocks(), speed, rule.correctForDrops()));
                    }
                    builder.add(DataComponentTypes.TOOL, new ToolComponent(newRules, currentTool.defaultMiningSpeed(), currentTool.damagePerBlock(), currentTool.canDestroyBlocksInCreative()));
                }
            }

            ((ItemAccessor) item).vanilla_reforged$setComponents(builder.build());
        }

        // 2. Apply Armor Stats
        for (Map.Entry<Item, ArmorStats> entry : ARMOR_STATS.entrySet()) {
            Item item = entry.getKey();
            ArmorStats stats = entry.getValue();

            ComponentMap current = item.getComponents();
            ComponentMap.Builder builder = ComponentMap.builder().addAll(current);

            // Durability
            builder.add(DataComponentTypes.MAX_DAMAGE, stats.durability());

            // Enchantability
            builder.add(DataComponentTypes.ENCHANTABLE, new EnchantableComponent(stats.enchantability()));

            // Armor, Toughness, Knockback Resistance
            builder.add(DataComponentTypes.ATTRIBUTE_MODIFIERS, createArmorAttributes(stats.type(), stats.armor(), stats.toughness(), stats.knockback()));

            ((ItemAccessor) item).vanilla_reforged$setComponents(builder.build());
        }

        // 3. Apply Special Equipment (Elytra, Shield)
        applySpecialDurability(Items.ELYTRA, ELYTRA_DURABILITY);
        applySpecialDurability(Items.SHIELD, SHIELD_DURABILITY);
    }

    private static void applySpecialDurability(Item item, int durability) {
        ComponentMap current = item.getComponents();
        ComponentMap updated = ComponentMap.builder()
            .addAll(current)
            .add(DataComponentTypes.MAX_DAMAGE, durability)
            .build();
        ((ItemAccessor) item).vanilla_reforged$setComponents(updated);
    }

    public static AttributeModifiersComponent createWeaponAttributes(double attackDamage, double attackSpeed) {
        return AttributeModifiersComponent.builder()
            .add(
                EntityAttributes.ATTACK_DAMAGE,
                new EntityAttributeModifier(Item.BASE_ATTACK_DAMAGE_MODIFIER_ID, attackDamage - 1.0, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND
            )
            .add(
                EntityAttributes.ATTACK_SPEED,
                new EntityAttributeModifier(Item.BASE_ATTACK_SPEED_MODIFIER_ID, attackSpeed - 4.0, EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND
            )
            .build();
    }

    public static AttributeModifiersComponent createArmorAttributes(EquipmentType type, double armor, double toughness, double knockbackResistance) {
        AttributeModifierSlot slot = AttributeModifierSlot.forEquipmentSlot(type.getEquipmentSlot());
        Identifier id = Identifier.ofVanilla(type.getName());
        AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder();
        builder.add(EntityAttributes.ARMOR, new EntityAttributeModifier(id, armor, EntityAttributeModifier.Operation.ADD_VALUE), slot);
        if (toughness > 0.0) {
            builder.add(EntityAttributes.ARMOR_TOUGHNESS, new EntityAttributeModifier(id, toughness, EntityAttributeModifier.Operation.ADD_VALUE), slot);
        }
        if (knockbackResistance > 0.0) {
            builder.add(EntityAttributes.KNOCKBACK_RESISTANCE, new EntityAttributeModifier(id, knockbackResistance, EntityAttributeModifier.Operation.ADD_VALUE), slot);
        }
        return builder.build();
    }
}
