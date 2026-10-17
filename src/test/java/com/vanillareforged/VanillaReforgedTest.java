package com.vanillareforged;

import com.vanillareforged.balance.DurabilityManager;
import com.vanillareforged.client.EnchantmentTooltipHandler;
import com.vanillareforged.item.ModItems;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class VanillaReforgedTest {

    @Test
    @DisplayName("Verify Complete Master Stats v1.2 Constants")
    public void testMasterStatsV12() {
        // 1. Tool Durabilities
        assertEquals(59, DurabilityManager.WOOD_TOOL_DURABILITY);
        assertEquals(131, DurabilityManager.STONE_TOOL_DURABILITY);
        assertEquals(190, DurabilityManager.COPPER_TOOL_DURABILITY);
        assertEquals(250, DurabilityManager.IRON_TOOL_DURABILITY);
        assertEquals(1170, DurabilityManager.GOLD_TOOL_DURABILITY);
        assertEquals(1560, DurabilityManager.DIAMOND_TOOL_DURABILITY);
        assertEquals(2030, DurabilityManager.NETHERITE_TOOL_DURABILITY);

        // 2. Mining Speeds
        assertEquals(2.0f, DurabilityManager.WOOD_MINING_SPEED);
        assertEquals(4.0f, DurabilityManager.STONE_MINING_SPEED);
        assertEquals(5.0f, DurabilityManager.COPPER_MINING_SPEED);
        assertEquals(6.0f, DurabilityManager.IRON_MINING_SPEED);
        assertEquals(10.0f, DurabilityManager.GOLD_MINING_SPEED);
        assertEquals(12.0f, DurabilityManager.DIAMOND_MINING_SPEED);
        assertEquals(9.0f, DurabilityManager.NETHERITE_MINING_SPEED);

        // 3. Tool Enchantabilities
        assertEquals(15, DurabilityManager.WOOD_TOOL_ENCHANTABILITY);
        assertEquals(5, DurabilityManager.STONE_TOOL_ENCHANTABILITY);
        assertEquals(13, DurabilityManager.COPPER_TOOL_ENCHANTABILITY);
        assertEquals(14, DurabilityManager.IRON_TOOL_ENCHANTABILITY);
        assertEquals(22, DurabilityManager.GOLD_TOOL_ENCHANTABILITY);
        assertEquals(24, DurabilityManager.DIAMOND_TOOL_ENCHANTABILITY);
        assertEquals(16, DurabilityManager.NETHERITE_TOOL_ENCHANTABILITY);

        // 4. Armor Durabilities
        assertEquals(275, DurabilityManager.GOLD_HELMET_DURABILITY);
        assertEquals(400, DurabilityManager.GOLD_CHESTPLATE_DURABILITY);
        assertEquals(370, DurabilityManager.GOLD_LEGGINGS_DURABILITY);
        assertEquals(325, DurabilityManager.GOLD_BOOTS_DURABILITY);

        assertEquals(365, DurabilityManager.DIAMOND_HELMET_DURABILITY);
        assertEquals(530, DurabilityManager.DIAMOND_CHESTPLATE_DURABILITY);
        assertEquals(495, DurabilityManager.DIAMOND_LEGGINGS_DURABILITY);
        assertEquals(430, DurabilityManager.DIAMOND_BOOTS_DURABILITY);

        assertEquals(475, DurabilityManager.NETHERITE_HELMET_DURABILITY);
        assertEquals(690, DurabilityManager.NETHERITE_CHESTPLATE_DURABILITY);
        assertEquals(645, DurabilityManager.NETHERITE_LEGGINGS_DURABILITY);
        assertEquals(560, DurabilityManager.NETHERITE_BOOTS_DURABILITY);

        // 5. Armor Stats (Points, Toughness, KB, Enchantability)
        assertEquals(6.0, DurabilityManager.GOLD_CHESTPLATE_ARMOR);
        assertEquals(1.5, DurabilityManager.GOLD_ARMOR_TOUGHNESS);
        assertEquals(22, DurabilityManager.GOLD_ARMOR_ENCHANTABILITY);

        assertEquals(2.0, DurabilityManager.DIAMOND_ARMOR_TOUGHNESS);
        assertEquals(24, DurabilityManager.DIAMOND_ARMOR_ENCHANTABILITY);

        assertEquals(10.0, DurabilityManager.NETHERITE_CHESTPLATE_ARMOR);
        assertEquals(2.6, DurabilityManager.NETHERITE_ARMOR_TOUGHNESS);
        assertEquals(0.1, DurabilityManager.NETHERITE_KNOCKBACK_RESISTANCE, 0.001);
        assertEquals(16, DurabilityManager.NETHERITE_ARMOR_ENCHANTABILITY);

        // 6. Special Equipment
        assertEquals(275, DurabilityManager.TURTLE_HELMET_DURABILITY);
        assertEquals(432, DurabilityManager.ELYTRA_DURABILITY);
        assertEquals(336, DurabilityManager.SHIELD_DURABILITY);
    }



























    @Test
    @DisplayName("Verify proportional damage calculation formula on smithing upgrade")
    public void testProportionalDurabilityScaling() {
        int goldMaxDamage = 1170;
        int diamondMaxDamage = 1560;

        // 1. Undamaged base -> Undamaged output
        int undamagedBase = 0;
        int resultDamage = calculateProportionalDamage(undamagedBase, goldMaxDamage, diamondMaxDamage);
        assertEquals(0, resultDamage, "Undamaged gold item must yield undamaged diamond item");

        // 2. 50% damaged base -> ~50% damaged output
        int halfDamageBase = 585;
        resultDamage = calculateProportionalDamage(halfDamageBase, goldMaxDamage, diamondMaxDamage);
        double halfRatio = (double) resultDamage / (double) diamondMaxDamage;
        assertTrue(Math.abs(halfRatio - 0.5) < 0.01, "50% damaged gold tool must result in approximately 50% damaged diamond tool");

        // 3. Severely damaged base (1 durability left) -> must retain at least 1 durability on diamond tool
        int nearBrokenBase = 1169;
        resultDamage = calculateProportionalDamage(nearBrokenBase, goldMaxDamage, diamondMaxDamage);
        assertTrue(resultDamage < diamondMaxDamage, "Upgraded item must not be completely broken");
        assertTrue(resultDamage >= diamondMaxDamage - 10, "Heavily damaged item should remain heavily damaged");

        // 4. Test edge case with 32 max durability (vanilla gold tools)
        int vanillaGoldMax = 32;
        int halfVanilla = 16;
        resultDamage = calculateProportionalDamage(halfVanilla, vanillaGoldMax, diamondMaxDamage);
        assertEquals(780, resultDamage, "Half damaged 16/32 must scale to 780/1560");
    }

    private int calculateProportionalDamage(int baseDamage, int baseMax, int resultMax) {
        if (baseMax <= 0 || baseDamage <= 0) return 0;
        double ratio = (double) baseDamage / (double) baseMax;
        int newDamage = (int) Math.round(ratio * resultMax);
        newDamage = Math.min(newDamage, resultMax - 1);
        return Math.max(1, newDamage);
    }

    @Test
    @DisplayName("Verify Diamond Refining Recipe JSON Integrity")
    public void testDiamondRefiningRecipeFile() throws IOException {
        File file = new File("src/main/resources/data/vanilla_reforged/recipe/diamond_from_crystals.json");
        assertTrue(file.exists(), "Diamond refining recipe file must exist");
        String content = Files.readString(file.toPath());

        assertTrue(content.contains("\"vanilla_reforged:diamond_crystal\""), "Must require Diamond Crystal");
        assertTrue(content.contains("\"minecraft:amethyst_shard\""), "Must require Amethyst Shard");
        assertTrue(content.contains("\"minecraft:diamond\""), "Must output vanilla Diamond");
        assertTrue(content.contains("\"count\": 1"), "Must output exactly 1 Diamond");
    }

    @Test
    @DisplayName("Verify Template Duplication Recipe JSON Integrity")
    public void testTemplateDuplicationRecipeFile() throws IOException {
        File file = new File("src/main/resources/data/vanilla_reforged/recipe/diamond_upgrade_smithing_template_duplication.json");
        assertTrue(file.exists(), "Template duplication recipe must exist");
        String content = Files.readString(file.toPath());

        assertTrue(content.contains("\"vanilla_reforged:diamond_upgrade_smithing_template\""), "Must require Diamond Upgrade Template");
        assertTrue(content.contains("\"minecraft:amethyst_shard\""), "Must require Amethyst Shards");
        assertTrue(content.contains("\"minecraft:diamond\""), "Must require Diamond");
        assertTrue(content.contains("\"count\": 2"), "Must output exactly 2 templates");
    }

    @Test
    @DisplayName("Verify all 9 Smithing Upgrade Recipe JSON Files")
    public void testAllNineSmithingTransformRecipes() throws IOException {
        List<String> items = List.of(
            "sword", "pickaxe", "axe", "shovel", "hoe",
            "helmet", "chestplate", "leggings", "boots"
        );

        for (String item : items) {
            File file = new File("src/main/resources/data/vanilla_reforged/recipe/diamond_" + item + "_smithing.json");
            assertTrue(file.exists(), "Smithing recipe for diamond_" + item + " must exist");
            String content = Files.readString(file.toPath());

            assertTrue(content.contains("minecraft:smithing_transform"), "Must be smithing_transform recipe");
            assertTrue(content.contains("vanilla_reforged:diamond_upgrade_smithing_template"), "Must use Diamond Upgrade Template");
            assertTrue(content.contains("minecraft:golden_" + item), "Must require golden_" + item + " base");
            assertTrue(content.contains("minecraft:diamond"), "Must require diamond addition");
            assertTrue(content.contains("minecraft:diamond_" + item), "Must produce diamond_" + item);
        }
    }

    @Test
    @DisplayName("Verify all 9 Direct Diamond Equipment Crafting Recipes are disabled")
    public void testDisabledDirectDiamondRecipes() throws IOException {
        List<String> items = List.of(
            "sword", "pickaxe", "axe", "shovel", "hoe",
            "helmet", "chestplate", "leggings", "boots"
        );

        for (String item : items) {
            File file = new File("src/main/resources/data/minecraft/recipe/diamond_" + item + ".json");
            assertTrue(file.exists(), "Disabled recipe file for diamond_" + item + " must exist");
            String content = Files.readString(file.toPath());
            assertTrue(content.contains("fabric:load_conditions"), "Must have load_conditions disabling the recipe");
            assertTrue(content.contains("vanilla_reforged_disabled"), "Must check for disabled condition");
        }
    }

    @Test
    @DisplayName("Verify Diamond Ore and Deepslate Diamond Ore loot tables")
    public void testDiamondOreLootTables() throws IOException {
        File regularOre = new File("src/main/resources/data/minecraft/loot_table/blocks/diamond_ore.json");
        File deepslateOre = new File("src/main/resources/data/minecraft/loot_table/blocks/deepslate_diamond_ore.json");

        assertTrue(regularOre.exists(), "diamond_ore.json must exist");
        assertTrue(deepslateOre.exists(), "deepslate_diamond_ore.json must exist");

        for (File f : List.of(regularOre, deepslateOre)) {
            String content = Files.readString(f.toPath());
            assertTrue(content.contains("vanilla_reforged:diamond_crystal"), "Must drop vanilla_reforged:diamond_crystal");
            assertFalse(content.contains("\"name\": \"minecraft:diamond\""), "Must NOT drop ordinary diamond item directly");
            assertTrue(content.contains("minecraft:silk_touch"), "Must preserve Silk Touch check");
            assertTrue(content.contains("minecraft:ore_drops"), "Must preserve Fortune ore_drops formula");
        }
    }

    @Test
    @DisplayName("Verify Block Harvest Tags")
    public void testBlockHarvestTags() throws IOException {
        File goldTag = new File("src/main/resources/data/minecraft/tags/block/incorrect_for_gold_tool.json");
        File ironTag = new File("src/main/resources/data/minecraft/tags/block/incorrect_for_iron_tool.json");

        assertTrue(goldTag.exists(), "incorrect_for_gold_tool.json must exist");
        assertTrue(ironTag.exists(), "incorrect_for_iron_tool.json must exist");

        String goldContent = Files.readString(goldTag.toPath());
        assertTrue(goldContent.contains("#minecraft:needs_diamond_tool"), "Gold tool cannot mine diamond tier");
        assertFalse(goldContent.contains("#minecraft:needs_iron_tool"), "Gold tool CAN mine iron and gold tiers");

        String ironContent = Files.readString(ironTag.toPath());
        assertTrue(ironContent.contains("#minecraft:needs_diamond_tool"), "Iron tool cannot mine diamond tier");
        assertTrue(ironContent.contains("minecraft:diamond_ore"), "Iron tool cannot mine diamond ore");
        assertTrue(ironContent.contains("minecraft:deepslate_diamond_ore"), "Iron tool cannot mine deepslate diamond ore");
        assertTrue(ironContent.contains("minecraft:redstone_ore"), "Iron tool cannot mine redstone ore");
        assertTrue(ironContent.contains("minecraft:emerald_ore"), "Iron tool cannot mine emerald ore");
    }

    @Test
    @DisplayName("Verify Language file and Item assets")
    public void testAssetsAndLang() throws IOException {
        File lang = new File("src/main/resources/assets/vanilla_reforged/lang/en_us.json");
        assertTrue(lang.exists(), "en_us.json must exist");
        String langContent = Files.readString(lang.toPath());

        assertTrue(langContent.contains("Diamond Crystal"));
        assertTrue(langContent.contains("Diamond Upgrade"));

        File crystalTexture = new File("src/main/resources/assets/vanilla_reforged/textures/item/diamond_crystal.png");
        File templateTexture = new File("src/main/resources/assets/vanilla_reforged/textures/item/diamond_upgrade_smithing_template.png");
        assertTrue(crystalTexture.exists(), "diamond_crystal.png must exist");
        assertTrue(templateTexture.exists(), "diamond_upgrade_smithing_template.png must exist");
    }

    @Test
    @DisplayName("Verify Phase 2 All 20 Enchantment JSON Definitions")
    public void testPhase2EnchantmentJsonFiles() throws IOException {
        List<String> enchantments = List.of(
            // 4 Curses
            "curse_of_breaking", "curse_of_clumsiness", "curse_of_displacement", "curse_of_double_edge",
            // 6 Combat
            "breeze_burst", "ice_aspect", "outreach", "swift_strike", "toxic", "websnare",
            // 3 Mining / Utility
            "crabs_touch", "gluttony", "pyrolysis",
            // 6 Armor / Survival
            "kinetic_protection", "retrieval", "scorch_walker", "skyguard", "stride", "vitality",
            // 1 Movement
            "graviole"
        );

        for (String ench : enchantments) {
            File file = new File("src/main/resources/data/vanilla_reforged/enchantment/" + ench + ".json");
            assertTrue(file.exists(), "Enchantment JSON must exist: " + ench);
            String content = Files.readString(file.toPath());
            assertTrue(content.contains("enchantment.vanilla_reforged." + ench), "Must contain valid translation key: " + ench);
            assertTrue(content.contains("\"max_level\""), "Must define max_level: " + ench);
            assertTrue(content.contains("\"supported_items\""), "Must define supported_items: " + ench);
            assertTrue(content.contains("\"slots\""), "Must define slots: " + ench);
        }
    }

    @Test
    @DisplayName("Verify Phase 2 Enchantment Tags and Exclusive Sets")
    public void testPhase2EnchantmentTags() throws IOException {
        File curseTag = new File("src/main/resources/data/minecraft/tags/enchantment/curse.json");
        File tableTag = new File("src/main/resources/data/minecraft/tags/enchantment/in_enchanting_table.json");
        File tradeTag = new File("src/main/resources/data/minecraft/tags/enchantment/tradeable.json");
        File lootTag = new File("src/main/resources/data/minecraft/tags/enchantment/on_random_loot.json");

        assertTrue(curseTag.exists(), "curse.json tag must exist");
        assertTrue(tableTag.exists(), "in_enchanting_table.json tag must exist");
        assertTrue(tradeTag.exists(), "tradeable.json tag must exist");
        assertTrue(lootTag.exists(), "on_random_loot.json tag must exist");

        String curseContent = Files.readString(curseTag.toPath());
        assertTrue(curseContent.contains("vanilla_reforged:curse_of_breaking"));
        assertTrue(curseContent.contains("vanilla_reforged:curse_of_clumsiness"));
        assertTrue(curseContent.contains("vanilla_reforged:curse_of_displacement"));
        assertTrue(curseContent.contains("vanilla_reforged:curse_of_double_edge"));

        String tableContent = Files.readString(tableTag.toPath());
        assertTrue(tableContent.contains("vanilla_reforged:gluttony"));
        assertTrue(tableContent.contains("vanilla_reforged:vitality"));
        assertTrue(tableContent.contains("vanilla_reforged:pyrolysis"));
        assertFalse(tableContent.contains("vanilla_reforged:skyguard"), "Skyguard is treasure only");
        assertFalse(tableContent.contains("vanilla_reforged:graviole"), "Graviole is treasure only");

        // Exclusive sets
        File fireGroup = new File("src/main/resources/data/vanilla_reforged/tags/enchantment/exclusive_set/fire_aspect_group.json");
        File reachGroup = new File("src/main/resources/data/vanilla_reforged/tags/enchantment/exclusive_set/reach_group.json");
        File smeltingGroup = new File("src/main/resources/data/vanilla_reforged/tags/enchantment/exclusive_set/smelting_group.json");

        assertTrue(fireGroup.exists());
        assertTrue(reachGroup.exists());
        assertTrue(smeltingGroup.exists());
    }

    @Test
    @DisplayName("Verify Modified Vanilla Enchantments")
    public void testModifiedVanillaEnchantments() throws IOException {
        File power = new File("src/main/resources/data/minecraft/enchantment/power.json");
        File sharpness = new File("src/main/resources/data/minecraft/enchantment/sharpness.json");
        File silkTouch = new File("src/main/resources/data/minecraft/enchantment/silk_touch.json");

        assertTrue(power.exists(), "power.json override must exist");
        assertTrue(sharpness.exists(), "sharpness.json override must exist");
        assertTrue(silkTouch.exists(), "silk_touch.json override must exist");

        String powerContent = Files.readString(power.toPath());
        assertTrue(powerContent.contains("bow_and_crossbow"), "Power must support bow and crossbow");

        String sharpContent = Files.readString(sharpness.toPath());
        assertTrue(sharpContent.contains("sharp_weapon_and_trident"), "Sharpness must support sharp weapon and trident");

        String silkContent = Files.readString(silkTouch.toPath());
        assertTrue(silkContent.contains("silk_touch_items"), "Silk Touch must support tools and swords");
    }

    @Test
    @DisplayName("Verify Piglin and Zombified Piglin Gold Drops")
    public void testPiglinDrops() throws IOException {
        File piglin = new File("src/main/resources/data/minecraft/loot_table/entity/piglin.json");
        File zombified = new File("src/main/resources/data/minecraft/loot_table/entity/zombified_piglin.json");

        assertTrue(piglin.exists(), "piglin.json must exist");
        assertTrue(zombified.exists(), "zombified_piglin.json must exist");

        String piglinContent = Files.readString(piglin.toPath());
        assertTrue(piglinContent.contains("minecraft:gold_nugget"), "Piglin must drop gold nuggets");
        assertFalse(piglinContent.contains("minecraft:gold_ingot"), "Piglin must NOT drop gold ingots");

        String zombifiedContent = Files.readString(zombified.toPath());
        assertTrue(zombifiedContent.contains("minecraft:gold_nugget"), "Zombified Piglin must drop gold nuggets");
        assertFalse(zombifiedContent.contains("minecraft:gold_ingot"), "Zombified Piglin must NOT drop gold ingots");
    }

    @Test
    @DisplayName("Verify All Phase 2 Language Translations")
    public void testPhase2Translations() throws IOException {
        File lang = new File("src/main/resources/assets/vanilla_reforged/lang/en_us.json");
        assertTrue(lang.exists());
        String content = Files.readString(lang.toPath());

        List<String> names = List.of(
            "Curse of Breaking", "Curse of Clumsiness", "Curse of Displacement", "Curse of Double Edge",
            "Breeze Burst", "Ice Aspect", "Outreach", "Swift Strike", "Toxic", "Websnare",
            "Crab's Touch", "Gluttony", "Pyrolysis", "Kinetic Protection", "Retrieval",
            "Scorch Walker", "Skyguard", "Stride", "Vitality", "Graviole"
        );

        for (String name : names) {
            assertTrue(content.contains(name), "Lang file must contain translation for: " + name);
        }
    }

    @Test
    @DisplayName("Verify Enchantment Tag Integrity")
    public void testEnchantmentTagIntegrity() throws IOException {
        File dir = new File("src/main/resources/data/vanilla_reforged/enchantment");
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        assertNotNull(files);

        for (File f : files) {
            String content = Files.readString(f.toPath());
            assertFalse(content.contains("#minecraft:enchantable/sword"), 
                "Must NOT use non-existent tag #minecraft:enchantable/sword in " + f.getName());
        }

        File silkTag = new File("src/main/resources/data/vanilla_reforged/tags/item/enchantable/silk_touch_items.json");
        assertTrue(silkTag.exists());
        String silkContent = Files.readString(silkTag.toPath());
        assertFalse(silkContent.contains("#minecraft:enchantable/sword"), 
            "Must NOT use non-existent tag #minecraft:enchantable/sword in silk_touch_items.json");
    }

    @Test
    @DisplayName("Verify Villager Gold Economy & Equipment Restriction")
    public void testVillagerGoldEconomy() throws IOException {
        // 1. Verify VillagerTradeManager registers all 4 professions (Armorer, Toolsmith, Weaponsmith, Librarian)
        File tradeManagerFile = new File("src/main/java/com/vanillareforged/economy/VillagerTradeManager.java");
        assertTrue(tradeManagerFile.exists(), "VillagerTradeManager.java must exist");
        String tradeContent = Files.readString(tradeManagerFile.toPath());
        assertTrue(tradeContent.contains("registerArmorerTrades()"), "Must register Armorer trades");
        assertTrue(tradeContent.contains("registerToolsmithTrades()"), "Must register Toolsmith trades");
        assertTrue(tradeContent.contains("registerWeaponsmithTrades()"), "Must register Weaponsmith trades");
        assertTrue(tradeContent.contains("registerLibrarianTrades()"), "Must register Librarian trades");
        assertTrue(tradeContent.contains("createCustomEnchantedBookTrade"), "Must register custom enchanted book trades");

        // 2. Verify all 9 diamond equipment items are restricted
        List<String> diamondGear = List.of(
            "DIAMOND_SWORD", "DIAMOND_PICKAXE", "DIAMOND_AXE", "DIAMOND_SHOVEL", "DIAMOND_HOE",
            "DIAMOND_HELMET", "DIAMOND_CHESTPLATE", "DIAMOND_LEGGINGS", "DIAMOND_BOOTS"
        );
        for (String gear : diamondGear) {
            assertTrue(tradeContent.contains(gear), "DIAMOND_EQUIPMENT must contain: " + gear);
        }

        // 3. Verify VillagerEntityMixin filters diamond equipment at fillRecipes and prepareOffersFor
        File villagerMixinFile = new File("src/main/java/com/vanillareforged/mixin/VillagerEntityMixin.java");
        assertTrue(villagerMixinFile.exists(), "VillagerEntityMixin.java must exist");
        String mixinContent = Files.readString(villagerMixinFile.toPath());
        assertTrue(mixinContent.contains("fillRecipes"), "Must hook fillRecipes");
        assertTrue(mixinContent.contains("prepareOffersFor"), "Must hook prepareOffersFor");
        assertTrue(mixinContent.contains("isDiamondEquipment"), "Must call isDiamondEquipment");

        // 4. Verify PiglinEntityMixin converts inventory and held gold ingots to nuggets on death
        File piglinMixinFile = new File("src/main/java/com/vanillareforged/mixin/PiglinEntityMixin.java");
        assertTrue(piglinMixinFile.exists(), "PiglinEntityMixin.java must exist");
        String piglinMixinContent = Files.readString(piglinMixinFile.toPath());
        assertTrue(piglinMixinContent.contains("dropEquipment"), "Must hook dropEquipment");
        assertTrue(piglinMixinContent.contains("GOLD_NUGGET"), "Must convert to gold nuggets");
        assertTrue(piglinMixinContent.contains("MAINHAND") && piglinMixinContent.contains("OFFHAND"),
            "Must check MAINHAND and OFFHAND for mid-barter gold ingots");
        assertTrue(piglinMixinContent.contains("nextInt(3)"),
            "Must randomize 2-4 gold nuggets");
    }

    @Test
    @DisplayName("Verify Tooltips for Every Enchantment in en_us.json and Client Registration")
    public void testEnchantmentTooltips() throws IOException {
        File lang = new File("src/main/resources/assets/vanilla_reforged/lang/en_us.json");
        assertTrue(lang.exists(), "en_us.json must exist");
        String langContent = Files.readString(lang.toPath());

        // 1. All 20 Phase 2 enchantments and curses have tooltips
        List<String> phase2Keys = List.of(
            "curse_of_breaking.desc", "curse_of_clumsiness.desc", "curse_of_displacement.desc", "curse_of_double_edge.desc",
            "breeze_burst.desc", "ice_aspect.desc", "outreach.desc", "swift_strike.desc", "toxic.desc", "websnare.desc",
            "crabs_touch.desc", "gluttony.desc", "pyrolysis.desc", "kinetic_protection.desc", "retrieval.desc",
            "scorch_walker.desc", "skyguard.desc", "stride.desc", "vitality.desc", "graviole.desc"
        );
        for (String key : phase2Keys) {
            assertTrue(langContent.contains("enchantment.vanilla_reforged." + key), 
                "en_us.json must have description for Phase 2 enchantment: " + key);
        }

        // 2. Vanilla enchantments have tooltips
        List<String> vanillaKeys = List.of(
            "protection.desc", "fire_protection.desc", "feather_falling.desc", "blast_protection.desc", "projectile_protection.desc",
            "respiration.desc", "aqua_affinity.desc", "thorns.desc", "depth_strider.desc", "frost_walker.desc",
            "binding_curse.desc", "soul_speed.desc", "swift_sneak.desc", "sharpness.desc", "smite.desc",
            "bane_of_arthropods.desc", "knockback.desc", "fire_aspect.desc", "looting.desc", "sweeping_edge.desc",
            "efficiency.desc", "silk_touch.desc", "unbreaking.desc", "fortune.desc", "power.desc", "punch.desc",
            "flame.desc", "infinity.desc", "luck_of_the_sea.desc", "lure.desc", "loyalty.desc", "impaling.desc",
            "riptide.desc", "channeling.desc", "multishot.desc", "quick_charge.desc", "piercing.desc", "density.desc",
            "breach.desc", "wind_burst.desc", "mending.desc", "vanishing_curse.desc"
        );
        for (String key : vanillaKeys) {
            assertTrue(langContent.contains("enchantment.minecraft." + key), 
                "en_us.json must have description for vanilla enchantment: " + key);
        }

        // 3. Client entrypoint and tooltip handler files exist
        File clientInit = new File("src/main/java/com/vanillareforged/client/VanillaReforgedClient.java");
        assertTrue(clientInit.exists(), "VanillaReforgedClient.java must exist");
        File tooltipHandler = new File("src/main/java/com/vanillareforged/client/EnchantmentTooltipHandler.java");
        assertTrue(tooltipHandler.exists(), "EnchantmentTooltipHandler.java must exist");
        String handlerContent = Files.readString(tooltipHandler.toPath());
        assertTrue(handlerContent.contains("ItemTooltipCallback.EVENT"), "Handler must listen to ItemTooltipCallback.EVENT");

        // 4. Client entrypoint in fabric.mod.json
        File modJson = new File("src/main/resources/fabric.mod.json");
        String modJsonContent = Files.readString(modJson.toPath());
        assertTrue(modJsonContent.contains("com.vanillareforged.client.VanillaReforgedClient"), 
            "fabric.mod.json must register client entrypoint");
    }

    @Test
    @DisplayName("Verify Librarians Exclusively Trade with Gold instead of Emeralds")
    public void testLibrarianTradesUseOnlyGold() throws IOException {
        // 1. Verify VillagerEntityMixin has convertLibrarianEmeraldsToGold
        File mixinFile = new File("src/main/java/com/vanillareforged/mixin/VillagerEntityMixin.java");
        assertTrue(mixinFile.exists(), "VillagerEntityMixin.java must exist");
        String mixinContent = Files.readString(mixinFile.toPath());
        assertTrue(mixinContent.contains("convertLibrarianEmeraldsToGold"), "Must have convertLibrarianEmeraldsToGold method");
        assertTrue(mixinContent.contains("Items.GOLD_INGOT"), "Must convert to Items.GOLD_INGOT");
        assertTrue(mixinContent.contains("setSpecialPrice"), "Must preserve special price");

        // 2. Verify VillagerTradeManager registers Librarian trades with gold
        File tradeManagerFile = new File("src/main/java/com/vanillareforged/economy/VillagerTradeManager.java");
        String tradeContent = Files.readString(tradeManagerFile.toPath());

        // Extract registerLibrarianTrades method body
        int librarianStart = tradeContent.indexOf("private static void registerLibrarianTrades()");
        assertTrue(librarianStart != -1, "registerLibrarianTrades method must exist");
        int nextMethod = tradeContent.indexOf("private static TradeOffers.Factory createSellItemForEmeraldTrade", librarianStart);
        String librarianBody = tradeContent.substring(librarianStart, nextMethod);

        // Check that Librarian trades use Gold trades and do NOT use Emerald trades
        assertTrue(librarianBody.contains("createGoldEnchantedBookTrade"), "Must use createGoldEnchantedBookTrade");
        assertTrue(librarianBody.contains("createSellItemForGoldTrade"), "Must use createSellItemForGoldTrade");
        assertTrue(librarianBody.contains("createBuyItemForGoldTrade"), "Must use createBuyItemForGoldTrade");
        assertFalse(librarianBody.contains("createSellItemForEmeraldTrade"), "Librarian must NOT use Emerald sell trades");
        assertFalse(librarianBody.contains("createBuyItemForEmeraldTrade"), "Librarian must NOT use Emerald buy trades");
        assertFalse(librarianBody.contains("Items.EMERALD"), "Librarian trade registration must NOT reference Items.EMERALD");
    }

    @Test
    @DisplayName("Verify Tool Component Preserves Default Mining Speed (Prevents Pickaxes from Insta-mining Dirt)")
    public void testToolDefaultMiningSpeed() throws IOException {
        File durabilityManagerFile = new File("src/main/java/com/vanillareforged/balance/DurabilityManager.java");
        assertTrue(durabilityManagerFile.exists(), "DurabilityManager.java must exist");
        String content = Files.readString(durabilityManagerFile.toPath());

        // Must preserve currentTool.defaultMiningSpeed() instead of stats.miningSpeed() as default
        assertTrue(content.contains("new ToolComponent(newRules, currentTool.defaultMiningSpeed()"),
            "ToolComponent must use currentTool.defaultMiningSpeed() so tools do not insta-mine unsuited blocks like dirt");
        assertFalse(content.contains("new ToolComponent(newRules, stats.miningSpeed()"),
            "ToolComponent must NOT use stats.miningSpeed() as the default fallback mining speed");
    }

    @Test
    @DisplayName("Verify Dual Resource Enchanting (Gold = Mod Enchantments, Lapis = Vanilla Enchantments)")
    public void testDualResourceEnchanting() throws IOException {
        // 1. Verify in_enchanting_table.json tag includes custom table enchantments including retrieval
        File tableTag = new File("src/main/resources/data/minecraft/tags/enchantment/in_enchanting_table.json");
        assertTrue(tableTag.exists(), "in_enchanting_table.json must exist");
        String tableContent = Files.readString(tableTag.toPath());
        assertTrue(tableContent.contains("vanilla_reforged:retrieval"), "Tag must include retrieval");
        assertTrue(tableContent.contains("vanilla_reforged:gluttony"), "Tag must include gluttony");
        assertTrue(tableContent.contains("vanilla_reforged:vitality"), "Tag must include vitality");
        assertTrue(tableContent.contains("vanilla_reforged:outreach"), "Tag must include outreach");
        assertTrue(tableContent.contains("vanilla_reforged:swift_strike"), "Tag must include swift_strike");
        assertTrue(tableContent.contains("vanilla_reforged:pyrolysis"), "Tag must include pyrolysis");
        assertTrue(tableContent.contains("vanilla_reforged:stride"), "Tag must include stride");
        assertTrue(tableContent.contains("vanilla_reforged:toxic"), "Tag must include toxic");
        assertTrue(tableContent.contains("vanilla_reforged:kinetic_protection"), "Tag must include kinetic_protection");

        // 2. Verify EnchantmentScreenHandlerMixin implements reagent-based enchantment filtering
        File mixinFile = new File("src/main/java/com/vanillareforged/mixin/EnchantmentScreenHandlerMixin.java");
        assertTrue(mixinFile.exists(), "EnchantmentScreenHandlerMixin.java must exist");
        String mixinContent = Files.readString(mixinFile.toPath());

        assertTrue(mixinContent.contains("isGoldReagent()"), "Must check for gold reagent");
        assertTrue(mixinContent.contains("Items.GOLD_INGOT"), "Must reference Items.GOLD_INGOT");
        assertTrue(mixinContent.contains("@Inject"), "Must use @Inject to intercept enchantment generation");
        assertTrue(mixinContent.contains("method = \"generateEnchantments\""), "Inject must target generateEnchantments");
        assertTrue(mixinContent.contains("onGenerateEnchantments"), "Must contain onGenerateEnchantments method");
        assertTrue(mixinContent.contains("\"vanilla_reforged\""), "Must filter for vanilla_reforged when gold is used");
        assertTrue(mixinContent.contains("\"minecraft\""), "Must filter for minecraft when lapis is used");
    }

    @Test
    @DisplayName("Verify Villager Mobility Overhaul (Step Height, Anti-Stuck, Panic Flee, Crowd Push)")
    public void testVillagerMobilityOverhaul() throws IOException {
        // 1. Verify VillagerEntityMixin has smooth 1-block auto-stepping
        File villagerMixinFile = new File("src/main/java/com/vanillareforged/mixin/VillagerEntityMixin.java");
        assertTrue(villagerMixinFile.exists(), "VillagerEntityMixin.java must exist");
        String villagerContent = Files.readString(villagerMixinFile.toPath());

        assertTrue(villagerContent.contains("createVillagerAttributes"), "Must hook createVillagerAttributes");
        assertTrue(villagerContent.contains("EntityAttributes.STEP_HEIGHT"), "Must set STEP_HEIGHT");
        assertTrue(villagerContent.contains("1.0625"), "Must set step height to 1.0625 for smooth 1-block stepping");
        assertTrue(villagerContent.contains("ensureVillagerMobility"), "Must ensure mobility for existing villagers on tick");

        // 2. Verify Anti-stuck detection and Smart panic flee
        assertTrue(villagerContent.contains("handleAntiStuckAndPanic"), "Must have handleAntiStuckAndPanic method");
        assertTrue(villagerContent.contains("NoPenaltyTargeting.findFrom"), "Must use NoPenaltyTargeting.findFrom for smart flee");
        assertTrue(villagerContent.contains("NoPenaltyTargeting.find"), "Must use NoPenaltyTargeting.find for unstuck");
        assertTrue(villagerContent.contains("stuckTickCount"), "Must track stuck ticks");
        assertTrue(villagerContent.contains("CANT_REACH_WALK_TARGET_SINCE"), "Must update CANT_REACH_WALK_TARGET_SINCE on stuck");

        // 3. Verify LivingEntityMixin softens villager-on-villager crowd collision deadlock
        File livingMixinFile = new File("src/main/java/com/vanillareforged/mixin/LivingEntityMixin.java");
        assertTrue(livingMixinFile.exists(), "LivingEntityMixin.java must exist");
        String livingContent = Files.readString(livingMixinFile.toPath());
        assertTrue(livingContent.contains("pushAwayFrom"), "Must hook pushAwayFrom");
        assertTrue(livingContent.contains("reduceVillagerCrowdPushing"), "Must have reduceVillagerCrowdPushing method");
    }

    @Test
    @DisplayName("Verify King Zombie Entity Attributes, DataTrackers & Mechanics")
    public void testKingZombieEntityAndStats() throws IOException {
        // 1. ModConfig constants
        assertEquals(80.0, com.vanillareforged.config.ModConfig.KING_ZOMBIE_MAX_HEALTH);
        assertEquals(1.5f, com.vanillareforged.config.ModConfig.KING_ZOMBIE_SCALE);
        assertEquals(5.0, com.vanillareforged.config.ModConfig.KING_ZOMBIE_ATTACK_DAMAGE);
        assertEquals(0.5, com.vanillareforged.config.ModConfig.KING_ZOMBIE_KNOCKBACK_RESISTANCE);
        assertEquals(40.0, com.vanillareforged.config.ModConfig.KING_ZOMBIE_FOLLOW_RANGE);
        assertEquals(40.0f, com.vanillareforged.config.ModConfig.KINGS_CALL_THRESHOLD_HEALTH);
        assertEquals(3, com.vanillareforged.config.ModConfig.KINGS_CALL_MIN_SUMMONS);
        assertEquals(6, com.vanillareforged.config.ModConfig.KINGS_CALL_MAX_SUMMONS);
        assertEquals(1.0, com.vanillareforged.config.ModConfig.KINGS_CALL_TRIGGER_RELIABILITY);
        assertEquals(0.05, com.vanillareforged.config.ModConfig.DIAMOND_KING_CHANCE);

        // 2. KingZombieEntity source inspection
        File kingZombieFile = new File("src/main/java/com/vanillareforged/entity/KingZombieEntity.java");
        assertTrue(kingZombieFile.exists(), "KingZombieEntity.java must exist");
        String content = Files.readString(kingZombieFile.toPath());

        assertTrue(content.contains("extends ZombieEntity"), "KingZombieEntity must extend ZombieEntity");
        assertTrue(content.contains("IS_DIAMOND"), "Must have IS_DIAMOND data tracker");
        assertTrue(content.contains("KINGS_CALL_USED"), "Must have KINGS_CALL_USED data tracker");
        assertTrue(content.contains("\"IsDiamond\""), "Must persist IsDiamond to NBT");
        assertTrue(content.contains("\"KingsCallUsed\""), "Must persist KingsCallUsed to NBT");
        assertTrue(content.contains("StatusEffects.SPEED"), "Must have Speed effect");
        assertTrue(content.contains("triggerKingsCall"), "Must have King's Call trigger");
        assertTrue(content.contains("vanilla_reforged:wave_zombie"), "Must tag summoned zombies to prevent recursion");
        assertTrue(content.contains("vanilla_reforged:no_reinforcement"), "Must tag summoned zombies to prevent recursion");
        assertTrue(content.contains("equipKingArmor"), "Must equip King armor");
        assertTrue(content.contains("boolean isBaby()"), "King Zombie must override isBaby to return false");
        assertTrue(content.contains("void setBaby(boolean baby)"), "King Zombie must override setBaby");
        assertTrue(content.contains("super.setBaby(false)"), "setBaby must always call super.setBaby(false)");
    }

    @Test
    @DisplayName("Verify Zombie Family Overhaul & Controlled Reinforcement System")
    public void testZombieFamilyOverhaulAndReinforcements() throws IOException {
        // 1. ModConfig Zombie family values
        assertEquals(22.0, com.vanillareforged.config.ModConfig.ZOMBIE_MAX_HEALTH);
        assertEquals(0.40, com.vanillareforged.config.ModConfig.ZOMBIE_REINFORCEMENT_CHANCE);
        assertEquals(200, com.vanillareforged.config.ModConfig.ZOMBIE_REINFORCEMENT_COOLDOWN_TICKS);
        assertEquals(4, com.vanillareforged.config.ModConfig.ZOMBIE_MAX_REINFORCEMENTS);
        assertEquals(0.25, com.vanillareforged.config.ModConfig.ZOMBIE_ARMOR_CHANCE);
        assertEquals(0.01, com.vanillareforged.config.ModConfig.KING_ZOMBIE_REINFORCEMENT_CHANCE);
        assertEquals(0.085, com.vanillareforged.config.ModConfig.ZOMBIE_ARMOR_DROP_CHANCE);
        assertEquals(0.45, com.vanillareforged.config.ModConfig.ZOMBIE_ARMOR_LEATHER_WEIGHT);
        assertEquals(0.35, com.vanillareforged.config.ModConfig.ZOMBIE_ARMOR_CHAINMAIL_WEIGHT);
        assertEquals(0.20, com.vanillareforged.config.ModConfig.ZOMBIE_ARMOR_IRON_WEIGHT);

        // 2. ZombieEntityMixin file verification
        File mixinFile = new File("src/main/java/com/vanillareforged/mixin/ZombieEntityMixin.java");
        assertTrue(mixinFile.exists(), "ZombieEntityMixin.java must exist");
        String mixinContent = Files.readString(mixinFile.toPath());
        assertTrue(mixinContent.contains("createZombieAttributes"), "Must hook createZombieAttributes");
        assertTrue(mixinContent.contains("ZOMBIE_MAX_HEALTH"), "Must set ZOMBIE_MAX_HEALTH");
        assertTrue(mixinContent.contains("ZombieArmorManager.applySpawnArmor"), "Must call applySpawnArmor");
        File livingMixinFile = new File("src/main/java/com/vanillareforged/mixin/LivingEntityMixin.java");
        assertTrue(Files.readString(livingMixinFile.toPath()).contains("handleZombieArmorDeathDrop"), "LivingEntityMixin must handle zombie armor death drop");
        File armorManagerFile = new File("src/main/java/com/vanillareforged/entity/ZombieArmorManager.java");
        assertTrue(Files.readString(armorManagerFile.toPath()).contains("ZOMBIE_ARMOR_DROP_CHANCE"), "Must handle 8.5% armor drop");

        // 3. ZombieReinforcementManager file verification
        File managerFile = new File("src/main/java/com/vanillareforged/entity/ZombieReinforcementManager.java");
        assertTrue(managerFile.exists(), "ZombieReinforcementManager.java must exist");
        String managerContent = Files.readString(managerFile.toPath());
        assertTrue(managerContent.contains("LAST_REINFORCEMENT_TICK"), "Must track cooldown ticks");
        assertTrue(managerContent.contains("ATTRIBUTED_REINFORCEMENTS"), "Must track attributed reinforcements");
        assertTrue(managerContent.contains("ZOMBIE_MAX_REINFORCEMENTS"), "Must cap reinforcements at 4");
        assertTrue(managerContent.contains("KING_ZOMBIE_REINFORCEMENT_CHANCE"), "Must have 1% chance for King Zombie");

        // 4. vanilla_reforged.mixins.json includes ZombieEntityMixin
        File mixinsJson = new File("src/main/resources/vanilla_reforged.mixins.json");
        String mixinsJsonContent = Files.readString(mixinsJson.toPath());
        assertTrue(mixinsJsonContent.contains("\"ZombieEntityMixin\""), "Mixins JSON must register ZombieEntityMixin");
    }

    @Test
    @DisplayName("Verify King Zombie Loot Tables")
    public void testKingZombieLootTables() throws IOException {
        File goldKingTable = new File("src/main/resources/data/vanilla_reforged/loot_table/entities/king_zombie.json");
        File diamondKingTable = new File("src/main/resources/data/vanilla_reforged/loot_table/entities/diamond_king_zombie.json");

        assertTrue(goldKingTable.exists(), "king_zombie.json must exist");
        assertTrue(diamondKingTable.exists(), "diamond_king_zombie.json must exist");

        String goldContent = Files.readString(goldKingTable.toPath());
        assertTrue(goldContent.contains("minecraft:rotten_flesh"), "Gold King must drop rotten flesh");
        assertTrue(goldContent.contains("minecraft:gold_nugget"), "Gold King must drop gold nuggets");
        assertTrue(goldContent.contains("minecraft:gold_ingot"), "Gold King must drop gold ingots");

        String diamondContent = Files.readString(diamondKingTable.toPath());
        assertTrue(diamondContent.contains("minecraft:rotten_flesh"), "Diamond King must drop rotten flesh");
        assertTrue(diamondContent.contains("minecraft:gold_nugget"), "Diamond King must drop gold nuggets");
        assertTrue(diamondContent.contains("minecraft:gold_ingot"), "Diamond King must drop gold ingots");
        assertTrue(diamondContent.contains("minecraft:diamond"), "Diamond King must drop diamond");
    }

    @Test
    @DisplayName("Verify Dire Wolf Entity Attributes, Ritual, Health Preservation & Loot Rules")
    public void testDireWolfEntityAndStats() throws IOException {
        // 1. ModConfig constants
        assertEquals(100.0, com.vanillareforged.config.ModConfig.DIRE_WOLF_MAX_HEALTH);
        assertEquals(20.0, com.vanillareforged.config.ModConfig.DIRE_WOLF_ATTACK_DAMAGE);
        assertEquals(2.0f, com.vanillareforged.config.ModConfig.DIRE_WOLF_SCALE);
        assertEquals(1.5, com.vanillareforged.config.ModConfig.DIRE_WOLF_SPEED_MULTIPLIER);
        assertEquals(0.25, com.vanillareforged.config.ModConfig.DIRE_WOLF_TAMING_SUCCESS_CHANCE);
        assertEquals(2, com.vanillareforged.config.ModConfig.DIRE_WOLF_SPAWN_WEIGHT);
        assertEquals(8, com.vanillareforged.config.ModConfig.DIRE_WOLF_XP_MIN);
        assertEquals(12, com.vanillareforged.config.ModConfig.DIRE_WOLF_XP_MAX);

        // 2. DireWolfEntity source inspection
        File direWolfFile = new File("src/main/java/com/vanillareforged/entity/DireWolfEntity.java");
        assertTrue(direWolfFile.exists(), "DireWolfEntity.java must exist");
        String content = Files.readString(direWolfFile.toPath());

        assertTrue(content.contains("extends WolfEntity"), "DireWolfEntity must extend WolfEntity");
        assertTrue(content.contains("TAMING_STAGE"), "Must have TAMING_STAGE data tracker");
        assertTrue(content.contains("VARIANT"), "Must have VARIANT data tracker");
        assertTrue(content.contains("\"TamingStage\""), "Must persist TamingStage to NBT");
        assertTrue(content.contains("\"Variant\""), "Must persist Variant to NBT");

        // Ritual requirements
        assertTrue(content.contains("StatusEffects.WEAKNESS"), "Step 1 must check for Weakness effect");
        assertTrue(content.contains("Items.BONE"), "Step 2 must accept bone");
        assertTrue(content.contains("Items.COOKED_BEEF"), "Step 3 must accept cooked beef (steak)");
        assertTrue(content.contains("Items.GOLDEN_CARROT"), "Step 4 must accept golden carrot");
        assertTrue(content.contains("DIRE_WOLF_TAMING_SUCCESS_CHANCE"), "Step 4 must use 25% taming chance");

        // Health preservation & untamed protection
        assertTrue(content.contains("updateAttributesForTamed"), "Must override updateAttributesForTamed to maintain 100 HP");
        assertTrue(content.contains("isTamed()"), "Must check isTamed() for drop rules");

        // Loot & XP rules
        assertTrue(content.contains("shouldDropLoot"), "Must override shouldDropLoot to return false when tamed");
        assertTrue(content.contains("getExperienceToDrop"), "Must override getExperienceToDrop to drop 0 XP when tamed and 8-12 when wild");
    }

    @Test
    @DisplayName("Verify Custom Spawn Eggs, Models, Textures and Translations")
    public void testCustomSpawnEggsAndAssets() throws IOException {
        // 1. ModSpawnEggItem and ModItems registration
        File spawnEggItemFile = new File("src/main/java/com/vanillareforged/item/ModSpawnEggItem.java");
        assertTrue(spawnEggItemFile.exists(), "ModSpawnEggItem.java must exist");
        String spawnEggItemContent = Files.readString(spawnEggItemFile.toPath());
        assertTrue(spawnEggItemContent.contains("extends SpawnEggItem"), "ModSpawnEggItem must extend SpawnEggItem");

        File modItemsFile = new File("src/main/java/com/vanillareforged/item/ModItems.java");
        assertTrue(modItemsFile.exists(), "ModItems.java must exist");
        String modItemsContent = Files.readString(modItemsFile.toPath());
        assertTrue(modItemsContent.contains("KING_ZOMBIE_SPAWN_EGG"), "ModItems must register KING_ZOMBIE_SPAWN_EGG");
        assertTrue(modItemsContent.contains("DIRE_WOLF_SPAWN_EGG"), "ModItems must register DIRE_WOLF_SPAWN_EGG");
        assertTrue(modItemsContent.contains("ItemGroups.SPAWN_EGGS"), "Must add eggs to ItemGroups.SPAWN_EGGS");

        // 2. Item model JSON files
        File kingEggItemJson = new File("src/main/resources/assets/vanilla_reforged/items/king_zombie_spawn_egg.json");
        File direEggItemJson = new File("src/main/resources/assets/vanilla_reforged/items/dire_wolf_spawn_egg.json");
        File kingEggModelJson = new File("src/main/resources/assets/vanilla_reforged/models/item/king_zombie_spawn_egg.json");
        File direEggModelJson = new File("src/main/resources/assets/vanilla_reforged/models/item/dire_wolf_spawn_egg.json");

        assertTrue(kingEggItemJson.exists(), "king_zombie_spawn_egg item definition must exist");
        assertTrue(direEggItemJson.exists(), "dire_wolf_spawn_egg item definition must exist");
        assertTrue(kingEggModelJson.exists(), "king_zombie_spawn_egg model must exist");
        assertTrue(direEggModelJson.exists(), "dire_wolf_spawn_egg model must exist");

        // 3. Textures
        File kingEggTexture = new File("src/main/resources/assets/vanilla_reforged/textures/item/king_zombie_spawn_egg.png");
        File direEggTexture = new File("src/main/resources/assets/vanilla_reforged/textures/item/dire_wolf_spawn_egg.png");
        assertTrue(kingEggTexture.exists(), "king_zombie_spawn_egg texture must exist");
        assertTrue(direEggTexture.exists(), "dire_wolf_spawn_egg texture must exist");

        // 4. en_us.json translations
        File langFile = new File("src/main/resources/assets/vanilla_reforged/lang/en_us.json");
        assertTrue(langFile.exists(), "en_us.json must exist");
        String langContent = Files.readString(langFile.toPath());
        assertTrue(langContent.contains("\"item.vanilla_reforged.king_zombie_spawn_egg\""), "Must have translation for king zombie spawn egg");
        assertTrue(langContent.contains("\"item.vanilla_reforged.dire_wolf_spawn_egg\""), "Must have translation for dire wolf spawn egg");
        assertTrue(langContent.contains("\"entity.vanilla_reforged.king_zombie\""), "Must have translation for king zombie entity");
        assertTrue(langContent.contains("\"entity.vanilla_reforged.dire_wolf\""), "Must have translation for dire wolf entity");
    }

    @Test
    @DisplayName("Verify Entity Registration, Biome Spawns, Mixin Accessor and Client Rendering")
    public void testEntityRegistrationAndClientRendering() throws IOException {
        // 1. ModEntities registration
        File modEntitiesFile = new File("src/main/java/com/vanillareforged/entity/ModEntities.java");
        assertTrue(modEntitiesFile.exists(), "ModEntities.java must exist");
        String modEntitiesContent = Files.readString(modEntitiesFile.toPath());
        assertTrue(modEntitiesContent.contains("KING_ZOMBIE"), "ModEntities must declare KING_ZOMBIE");
        assertTrue(modEntitiesContent.contains("DIRE_WOLF"), "ModEntities must declare DIRE_WOLF");
        assertTrue(modEntitiesContent.contains("FabricDefaultAttributeRegistry.register"), "Must register entity attributes");
        assertTrue(modEntitiesContent.contains("BiomeModifications.addSpawn"), "Must register biome spawns for Dire Wolf");

        // 2. SpawnRestrictionAccessor mixin
        File accessorFile = new File("src/main/java/com/vanillareforged/mixin/SpawnRestrictionAccessor.java");
        assertTrue(accessorFile.exists(), "SpawnRestrictionAccessor.java must exist");
        File mixinsJson = new File("src/main/resources/vanilla_reforged.mixins.json");
        String mixinsJsonContent = Files.readString(mixinsJson.toPath());
        assertTrue(mixinsJsonContent.contains("\"SpawnRestrictionAccessor\""), "Mixins JSON must register SpawnRestrictionAccessor");

        // 3. Client renderers
        File clientFile = new File("src/main/java/com/vanillareforged/client/VanillaReforgedClient.java");
        assertTrue(clientFile.exists(), "VanillaReforgedClient.java must exist");
        String clientContent = Files.readString(clientFile.toPath());
        assertTrue(clientContent.contains("ModEntities.KING_ZOMBIE"), "Client must register KING_ZOMBIE renderer");
        assertTrue(clientContent.contains("ModEntities.DIRE_WOLF"), "Client must register DIRE_WOLF renderer");

        File direWolfRendererFile = new File("src/main/java/com/vanillareforged/client/DireWolfEntityRenderer.java");
        assertTrue(direWolfRendererFile.exists(), "DireWolfEntityRenderer.java must exist");
        String rendererContent = Files.readString(direWolfRendererFile.toPath());
        assertTrue(rendererContent.contains("EntityModelLayers.WOLF"), "Must use Wolf model layer");
        assertTrue(rendererContent.contains("getTextureForState"), "Must use getTextureForState for coat & eyes");

        // Check textures for 3 variants (Regular, Ashen, Woods) both wild (red eyes) and tamed (natural eyes)
        File regWild = new File("src/main/resources/assets/vanilla_reforged/textures/entity/dire_wolf/dire_wolf_regular_wild.png");
        File regTamed = new File("src/main/resources/assets/vanilla_reforged/textures/entity/dire_wolf/dire_wolf_regular_tamed.png");
        File ashenWild = new File("src/main/resources/assets/vanilla_reforged/textures/entity/dire_wolf/dire_wolf_ashen_wild.png");
        File ashenTamed = new File("src/main/resources/assets/vanilla_reforged/textures/entity/dire_wolf/dire_wolf_ashen_tamed.png");
        File woodsWild = new File("src/main/resources/assets/vanilla_reforged/textures/entity/dire_wolf/dire_wolf_woods_wild.png");
        File woodsTamed = new File("src/main/resources/assets/vanilla_reforged/textures/entity/dire_wolf/dire_wolf_woods_tamed.png");
        File whiteTexture = new File("src/main/resources/assets/vanilla_reforged/textures/entity/dire_wolf/dire_wolf_white.png");

        assertTrue(regWild.exists(), "dire_wolf_regular_wild.png must exist");
        assertTrue(regTamed.exists(), "dire_wolf_regular_tamed.png must exist");
        assertTrue(ashenWild.exists(), "dire_wolf_ashen_wild.png must exist");
        assertTrue(ashenTamed.exists(), "dire_wolf_ashen_tamed.png must exist");
        assertTrue(woodsWild.exists(), "dire_wolf_woods_wild.png must exist");
        assertTrue(woodsTamed.exists(), "dire_wolf_woods_tamed.png must exist");
        assertFalse(whiteTexture.exists(), "dire_wolf_white.png must NOT exist (snow-white removed)");
    }

    @Test
    @DisplayName("Verify Dire Wolf Loot Table")
    public void testDireWolfLootTable() throws IOException {
        File direWolfTable = new File("src/main/resources/data/vanilla_reforged/loot_table/entities/dire_wolf.json");
        assertTrue(direWolfTable.exists(), "dire_wolf.json must exist");
        String content = Files.readString(direWolfTable.toPath());
        assertTrue(content.contains("minecraft:bone"), "Dire wolf must drop bones");
        assertTrue(content.contains("minecraft:random_chance"), "Dire wolf bone drop must have random chance");
        assertTrue(content.contains("minecraft:enchanted_count_increase"), "Dire wolf bone drop must support Looting");
    }

    @Test
    @DisplayName("Verify Enhanced Entity Loot Tables and Structure Injections")
    public void testEnhancedLootTablesAndStructureInjections() throws IOException {
        // 1. King Zombie Looting and Trophy Drops
        File kingTable = new File("src/main/resources/data/vanilla_reforged/loot_table/entities/king_zombie.json");
        String kingContent = Files.readString(kingTable.toPath());
        assertTrue(kingContent.contains("minecraft:enchanted_count_increase"), "King Zombie loot must support looting");
        assertTrue(kingContent.contains("minecraft:golden_apple"), "King Zombie must have rare golden apple trophy drop");

        // 2. Diamond King Zombie Looting and Boss Drops
        File diamondKingTable = new File("src/main/resources/data/vanilla_reforged/loot_table/entities/diamond_king_zombie.json");
        String diamondKingContent = Files.readString(diamondKingTable.toPath());
        assertTrue(diamondKingContent.contains("minecraft:enchanted_count_increase"), "Diamond King Zombie loot must support looting");
        assertTrue(diamondKingContent.contains("minecraft:golden_apple"), "Diamond King Zombie must drop golden apple");
        assertTrue(diamondKingContent.contains("vanilla_reforged:diamond_upgrade_smithing_template"), "Diamond King Zombie must have rare template drop");

        // 3. ModLootManager Structure Injections
        File lootManagerFile = new File("src/main/java/com/vanillareforged/loot/ModLootManager.java");
        assertTrue(lootManagerFile.exists(), "ModLootManager.java must exist");
        String lootContent = Files.readString(lootManagerFile.toPath());

        // Nether & Bastion: Scorch Walker
        assertTrue(lootContent.contains("BASTION_TREASURE_CHEST"));
        assertTrue(lootContent.contains("NETHER_BRIDGE_CHEST"));
        assertTrue(lootContent.contains("ModEnchantments.SCORCH_WALKER"));

        // Jungle Temple & Outpost: Toxic & Websnare
        assertTrue(lootContent.contains("JUNGLE_TEMPLE_CHEST"));
        assertTrue(lootContent.contains("PILLAGER_OUTPOST_CHEST"));
        assertTrue(lootContent.contains("ModEnchantments.TOXIC"));
        assertTrue(lootContent.contains("ModEnchantments.WEBSNARE"));

        // Stronghold & Ancient City: Vitality & Stride
        assertTrue(lootContent.contains("STRONGHOLD_LIBRARY_CHEST"));
        assertTrue(lootContent.contains("ANCIENT_CITY_CHEST"));
        assertTrue(lootContent.contains("ModEnchantments.VITALITY"));
        assertTrue(lootContent.contains("ModEnchantments.STRIDE"));

        // Mineshafts & Dungeons: Pyrolysis & Gluttony
        assertTrue(lootContent.contains("ABANDONED_MINESHAFT_CHEST"));
        assertTrue(lootContent.contains("SIMPLE_DUNGEON_CHEST"));
        assertTrue(lootContent.contains("ModEnchantments.PYROLYSIS"));
        assertTrue(lootContent.contains("ModEnchantments.GLUTTONY"));

        // Ominous Vaults: Curses
        assertTrue(lootContent.contains("TRIAL_CHAMBERS_REWARD_OMINOUS_CHEST"));
        assertTrue(lootContent.contains("ModEnchantments.CURSE_OF_BREAKING"));
        assertTrue(lootContent.contains("ModEnchantments.CURSE_OF_CLUMSINESS"));
        assertTrue(lootContent.contains("ModEnchantments.CURSE_OF_DISPLACEMENT"));
        assertTrue(lootContent.contains("ModEnchantments.CURSE_OF_DOUBLE_EDGE"));
    }

    @Test
    @DisplayName("Verify Enchanted Book Sweet & Square Tooltips and Target Equipment Badges")
    public void testEnchantedBookSquareTooltipsAndTargetEquipment() {
        // 1. Verify target mappings for custom & vanilla modified enchantments
        assertEquals("Swords", EnchantmentTooltipHandler.ENCHANTMENT_TARGETS.get("ice_aspect"));
        assertEquals("Swords, Axes, Tridents", EnchantmentTooltipHandler.ENCHANTMENT_TARGETS.get("sharpness"));
        assertEquals("Bows, Crossbows", EnchantmentTooltipHandler.ENCHANTMENT_TARGETS.get("power"));
        assertEquals("Tools & Swords", EnchantmentTooltipHandler.ENCHANTMENT_TARGETS.get("silk_touch"));
        assertEquals("Tools, Weapons", EnchantmentTooltipHandler.ENCHANTMENT_TARGETS.get("curse_of_clumsiness"));
        assertEquals("Elytra", EnchantmentTooltipHandler.ENCHANTMENT_TARGETS.get("skyguard"));
        assertEquals("Elytra", EnchantmentTooltipHandler.ENCHANTMENT_TARGETS.get("graviole"));
        assertEquals("Chestplates", EnchantmentTooltipHandler.ENCHANTMENT_TARGETS.get("vitality"));
        assertEquals("Leggings", EnchantmentTooltipHandler.ENCHANTMENT_TARGETS.get("stride"));
        assertEquals("Boots", EnchantmentTooltipHandler.ENCHANTMENT_TARGETS.get("scorch_walker"));
        assertEquals("All Gear", EnchantmentTooltipHandler.ENCHANTMENT_TARGETS.get("curse_of_breaking"));

        // 2. Verify word-wrap produces square lines (no line exceeding limit)
        String sampleText = "Attacks have a chance to freeze the enemy in ice for 3 seconds.";
        List<String> wrapped = EnchantmentTooltipHandler.wrapText(sampleText, 32);
        assertTrue(wrapped.size() >= 2, "Long descriptions should wrap to multiple lines for square shape");
        for (String line : wrapped) {
            assertTrue(line.length() <= 32, "Wrapped line length must not exceed 32 characters, was: " + line.length());
        }

        // 3. Verify descriptions map contains sweet, concise descriptions
        for (String key : EnchantmentTooltipHandler.ENCHANTMENT_DESCRIPTIONS.keySet()) {
            String desc = EnchantmentTooltipHandler.ENCHANTMENT_DESCRIPTIONS.get(key);
            assertNotNull(desc);
            assertFalse(desc.isEmpty());
        }
    }

    @Test
    @DisplayName("Verify Dire Wolf Hitbox Dimensions and 2.0x Model Alignment")
    public void testDireWolfHitboxDimensionsAndScaling() throws IOException {
        // 1. Verify ModEntities hitbox configuration
        File modEntitiesFile = new File("src/main/java/com/vanillareforged/entity/ModEntities.java");
        assertTrue(modEntitiesFile.exists());
        String entitiesContent = Files.readString(modEntitiesFile.toPath());
        
        // Base dimensions (0.9f width, 0.95f height, 0.85f eye height)
        assertTrue(entitiesContent.contains("EntityDimensions.changing(0.9f, 0.95f).withEyeHeight(0.85f)"),
            "ModEntities must define Dire Wolf dimensions with 0.9f width, 0.95f height, and 0.85f eye height");

        // 2. Verify DireWolfEntity sitting dimension override and calculateDimensions calls
        File direWolfFile = new File("src/main/java/com/vanillareforged/entity/DireWolfEntity.java");
        assertTrue(direWolfFile.exists());
        String direWolfContent = Files.readString(direWolfFile.toPath());
        assertTrue(direWolfContent.contains("getBaseDimensions(EntityPose pose)"),
            "DireWolfEntity must override getBaseDimensions for sitting pose");
        assertTrue(direWolfContent.contains("scaled(1.0f, 0.85f)"),
            "DireWolfEntity must scale sitting height by 0.85f");
        assertTrue(direWolfContent.contains("this.calculateDimensions()"),
            "DireWolfEntity must call calculateDimensions to scale collision box to 2.0x model size");

        // 3. Verify DireWolfEntityRenderer sets 1.0f shadow radius
        File direWolfRendererFile = new File("src/main/java/com/vanillareforged/client/DireWolfEntityRenderer.java");
        assertTrue(direWolfRendererFile.exists());
        String rendererContent = Files.readString(direWolfRendererFile.toPath());
        assertTrue(rendererContent.contains("this.shadowRadius = 1.0f;"),
            "DireWolfEntityRenderer must set 1.0f shadow radius for 2.0x scale wolf");
    }

    @Test
    @DisplayName("Verify Dedicated Vanilla Reforged Creative Tabs and Vanilla Tab Distribution")
    public void testVanillaReforgedCreativeTabAndEntries() throws IOException {
        // 1. Verify ModItems ItemGroup registration and builders
        File modItemsFile = new File("src/main/java/com/vanillareforged/item/ModItems.java");
        assertTrue(modItemsFile.exists());
        String modItemsContent = Files.readString(modItemsFile.toPath());

        // Dedicated Page 1: Items
        assertTrue(modItemsContent.contains("GENERAL_GROUP_ID = Identifier.of(VanillaReforgedMod.MOD_ID, \"general\")"),
            "ModItems must define 'general' ItemGroup Identifier");
        assertTrue(modItemsContent.contains("GENERAL_GROUP = FabricItemGroup.builder()"),
            "ModItems must define GENERAL_GROUP");
        assertTrue(modItemsContent.contains("itemGroup.vanilla_reforged.general"),
            "ModItems must set display name translation key for general items tab");

        // Dedicated Page 2: Enchantments
        assertTrue(modItemsContent.contains("ENCHANTMENTS_GROUP_ID = Identifier.of(VanillaReforgedMod.MOD_ID, \"enchantments\")"),
            "ModItems must define 'enchantments' ItemGroup Identifier");
        assertTrue(modItemsContent.contains("ENCHANTMENTS_GROUP = FabricItemGroup.builder()"),
            "ModItems must define ENCHANTMENTS_GROUP");
        assertTrue(modItemsContent.contains("itemGroup.vanilla_reforged.enchantments"),
            "ModItems must set display name translation key for enchantments tab");

        // Both groups registered with Registries.ITEM_GROUP
        assertTrue(modItemsContent.contains("Registry.register(Registries.ITEM_GROUP, GENERAL_GROUP_KEY, GENERAL_GROUP)"));
        assertTrue(modItemsContent.contains("Registry.register(Registries.ITEM_GROUP, ENCHANTMENTS_GROUP_KEY, ENCHANTMENTS_GROUP)"));

        // 2. Verify all custom items are added to the Items creative tab and vanilla tabs
        assertTrue(modItemsContent.contains("entries.add(DIAMOND_CRYSTAL)"));
        assertTrue(modItemsContent.contains("entries.add(DIAMOND_UPGRADE_SMITHING_TEMPLATE)"));
        assertTrue(modItemsContent.contains("entries.add(KING_ZOMBIE_SPAWN_EGG)"));
        assertTrue(modItemsContent.contains("entries.add(DIRE_WOLF_SPAWN_EGG)"));
        assertTrue(modItemsContent.contains("ItemGroups.INGREDIENTS"));
        assertTrue(modItemsContent.contains("ItemGroups.SPAWN_EGGS"));
        assertTrue(modItemsContent.contains("ItemGroups.COMBAT"));
        assertTrue(modItemsContent.contains("ItemGroups.TOOLS"));

        // 3. Verify all 20 custom Phase 2 enchantments are tracked and populated
        assertTrue(modItemsContent.contains("ALL_CUSTOM_ENCHANTMENTS"));
        assertTrue(modItemsContent.contains("COMBAT_ENCHANTMENTS"));
        assertTrue(modItemsContent.contains("TOOL_ENCHANTMENTS"));
        assertTrue(modItemsContent.contains("CURSE_OF_BREAKING"));
        assertTrue(modItemsContent.contains("CURSE_OF_CLUMSINESS"));
        assertTrue(modItemsContent.contains("CURSE_OF_DISPLACEMENT"));
        assertTrue(modItemsContent.contains("CURSE_OF_DOUBLE_EDGE"));
        assertTrue(modItemsContent.contains("BREEZE_BURST"));
        assertTrue(modItemsContent.contains("ICE_ASPECT"));
        assertTrue(modItemsContent.contains("OUTREACH"));
        assertTrue(modItemsContent.contains("SWIFT_STRIKE"));
        assertTrue(modItemsContent.contains("TOXIC"));
        assertTrue(modItemsContent.contains("WEBSNARE"));
        assertTrue(modItemsContent.contains("CRABS_TOUCH"));
        assertTrue(modItemsContent.contains("GLUTTONY"));
        assertTrue(modItemsContent.contains("PYROLYSIS"));
        assertTrue(modItemsContent.contains("KINETIC_PROTECTION"));
        assertTrue(modItemsContent.contains("RETRIEVAL"));
        assertTrue(modItemsContent.contains("SCORCH_WALKER"));
        assertTrue(modItemsContent.contains("SKYGUARD"));
        assertTrue(modItemsContent.contains("STRIDE"));
        assertTrue(modItemsContent.contains("VITALITY"));
        assertTrue(modItemsContent.contains("GRAVIOLE"));
        assertTrue(modItemsContent.contains("LUMINOSITY"));
        assertTrue(modItemsContent.contains("PRECISION"));
        assertTrue(modItemsContent.contains("EnchantmentHelper.getEnchantedBookWith"));

        // 4. Verify translations in en_us.json
        File langFile = new File("src/main/resources/assets/vanilla_reforged/lang/en_us.json");
        assertTrue(langFile.exists(), "en_us.json must exist");
        String langContent = Files.readString(langFile.toPath());
        assertTrue(langContent.contains("\"itemGroup.vanilla_reforged.general\": \"Vanilla Reforged: Items\""),
            "en_us.json must contain translation for Items creative tab");
        assertTrue(langContent.contains("\"itemGroup.vanilla_reforged.enchantments\": \"Vanilla Reforged: Enchantments\""),
            "en_us.json must contain translation for Enchantments creative tab");
    }

    @Test
    @DisplayName("Verify Enchants Plus Ported Enchantments (Luminosity & Precision)")
    public void testEnchantsPlusPortedEnchantments() throws IOException {
        File lumFile = new File("src/main/resources/data/vanilla_reforged/enchantment/luminosity.json");
        File precFile = new File("src/main/resources/data/vanilla_reforged/enchantment/precision.json");
        assertTrue(lumFile.exists(), "luminosity.json must exist");
        assertTrue(precFile.exists(), "precision.json must exist");

        String lumContent = Files.readString(lumFile.toPath());
        assertTrue(lumContent.contains("enchantment.vanilla_reforged.luminosity"));
        assertTrue(lumContent.contains("\"max_level\": 3"));

        String precContent = Files.readString(precFile.toPath());
        assertTrue(precContent.contains("enchantment.vanilla_reforged.precision"));
        assertTrue(precContent.contains("\"max_level\": 2"));

        // Verify tooltip descriptions
        assertTrue(com.vanillareforged.client.EnchantmentTooltipHandler.ENCHANTMENT_TARGETS.containsKey("luminosity"));
        assertTrue(com.vanillareforged.client.EnchantmentTooltipHandler.ENCHANTMENT_TARGETS.containsKey("precision"));
        assertTrue(com.vanillareforged.client.EnchantmentTooltipHandler.ENCHANTMENT_DESCRIPTIONS.containsKey("luminosity"));
        assertTrue(com.vanillareforged.client.EnchantmentTooltipHandler.ENCHANTMENT_DESCRIPTIONS.containsKey("precision"));

        // Verify en_us.json translations
        File langFile = new File("src/main/resources/assets/vanilla_reforged/lang/en_us.json");
        String langContent = Files.readString(langFile.toPath());
        assertTrue(langContent.contains("\"enchantment.vanilla_reforged.luminosity\": \"Luminosity\""));
        assertTrue(langContent.contains("\"enchantment.vanilla_reforged.precision\": \"Precision\""));
    }

    @Test
    @DisplayName("Verify Stride Nerf (Max Level 1, 1 Block Step-Up) and Scorch Walker Revert (2.5s Duration)")
    public void testStrideNerfAndScorchWalkerRevert() throws IOException {
        // 1. Verify Stride JSON: max_level must be 1
        File strideFile = new File("src/main/resources/data/vanilla_reforged/enchantment/stride.json");
        assertTrue(strideFile.exists(), "stride.json must exist");
        String strideContent = Files.readString(strideFile.toPath());
        assertTrue(strideContent.contains("\"max_level\": 1"), "Stride must be nerfed to max_level 1");

        // 2. Verify Stride step height modifier in ArmorEnchantmentHandler.java is 0.5 (allowing exactly 1 block step-up)
        File handlerFile = new File("src/main/java/com/vanillareforged/enchantment/ArmorEnchantmentHandler.java");
        assertTrue(handlerFile.exists(), "ArmorEnchantmentHandler.java must exist");
        String handlerContent = Files.readString(handlerFile.toPath());
        assertTrue(handlerContent.contains("0.5,"), "Stride modifier must be 0.5 for 1 block step-up");
        assertTrue(handlerContent.contains("EquipmentSlot.LEGS"), "Stride must be checked strictly on LEGS slot");

        // 3. Verify Stride descriptions
        assertEquals("Walk up 1 full block without jumping.",
                com.vanillareforged.client.EnchantmentTooltipHandler.ENCHANTMENT_DESCRIPTIONS.get("stride"));
        File langFile = new File("src/main/resources/assets/vanilla_reforged/lang/en_us.json");
        String langContent = Files.readString(langFile.toPath());
        assertTrue(langContent.contains("\"enchantment.vanilla_reforged.stride.desc\": \"Walk up 1 full block without jumping.\""));

        // 4. Verify Scorch Walker duration is 50 ticks (2.5 seconds)
        assertEquals(50L, com.vanillareforged.enchantment.ArmorEnchantmentHandler.SCORCH_WALKER_DURATION_TICKS,
                "Scorch Walker magma duration must be 50 ticks (2.5 seconds)");
        assertTrue(handlerContent.contains("SCORCH_WALKER_DURATION_TICKS = 50L;"));
        assertTrue(handlerContent.contains("tickTemporaryMagma"));
        assertTrue(handlerContent.contains("cleanupAllTemporaryMagma"));
        assertTrue(handlerContent.contains("Blocks.LAVA.getDefaultState()"));

        // 5. Verify Scorch Walker descriptions
        assertEquals("Walk on lava as temporary magma (2.5s); heat immune.",
                com.vanillareforged.client.EnchantmentTooltipHandler.ENCHANTMENT_DESCRIPTIONS.get("scorch_walker"));
        assertTrue(langContent.contains("\"enchantment.vanilla_reforged.scorch_walker.desc\": \"Walk on lava as temporary magma (2.5s); heat immune.\""));
    }

    @Test
    @DisplayName("Verify Pyrolysis Pickaxe Restriction, Spawner Block Buffs, and Hostile Mob XP Drops")
    public void testPyrolysisPickaxeRestrictionAndSpawnerBuffsAndMobXp() throws IOException {
        // 1. Verify pickaxe enchantable item tag
        File pickaxeTag = new File("src/main/resources/data/vanilla_reforged/tags/item/enchantable/pickaxe.json");
        assertTrue(pickaxeTag.exists(), "pickaxe.json tag must exist");
        String pickaxeContent = Files.readString(pickaxeTag.toPath());
        assertTrue(pickaxeContent.contains("\"#minecraft:pickaxes\""), "Pickaxe tag must contain #minecraft:pickaxes");

        // 2. Verify pyrolysis.json uses pickaxe tag for supported_items and primary_items
        File pyrolysisFile = new File("src/main/resources/data/vanilla_reforged/enchantment/pyrolysis.json");
        assertTrue(pyrolysisFile.exists(), "pyrolysis.json must exist");
        String pyrolysisContent = Files.readString(pyrolysisFile.toPath());
        assertTrue(pyrolysisContent.contains("\"supported_items\": \"#vanilla_reforged:enchantable/pickaxe\""),
                "Pyrolysis must only support pickaxes");
        assertTrue(pyrolysisContent.contains("\"primary_items\": \"#vanilla_reforged:enchantable/pickaxe\""),
                "Pyrolysis primary items must be pickaxes");

        // 3. Verify MiningEnchantmentHandler checks ItemTags.PICKAXES
        File miningHandlerFile = new File("src/main/java/com/vanillareforged/enchantment/MiningEnchantmentHandler.java");
        assertTrue(miningHandlerFile.exists(), "MiningEnchantmentHandler.java must exist");
        String miningContent = Files.readString(miningHandlerFile.toPath());
        assertTrue(miningContent.contains("ItemTags.PICKAXES"), "MiningEnchantmentHandler must check ItemTags.PICKAXES for Pyrolysis");

        // 4. Verify EnchantmentTooltipHandler targets Pickaxes for Pyrolysis
        assertEquals("Pickaxes", com.vanillareforged.client.EnchantmentTooltipHandler.ENCHANTMENT_TARGETS.get("pyrolysis"),
                "Pyrolysis tooltip target must be Pickaxes");

        // 5. Verify Spawner Block buffs in ModConfig
        assertEquals(20, com.vanillareforged.config.ModConfig.SPAWNER_MIN_DELAY, "Spawner min delay must be 20 ticks (1s)");
        assertEquals(60, com.vanillareforged.config.ModConfig.SPAWNER_MAX_DELAY, "Spawner max delay must be 60 ticks (3s)");
        assertEquals(16, com.vanillareforged.config.ModConfig.SPAWNER_MAX_NEARBY_ENTITIES, "Spawner max nearby entities must be buffed to 16");
        assertEquals(4, com.vanillareforged.config.ModConfig.SPAWNER_SPAWN_COUNT, "Spawner spawn count must be 4");

        // 6. Verify MobSpawnerLogicMixin registration and implementation
        File spawnerMixinFile = new File("src/main/java/com/vanillareforged/mixin/MobSpawnerLogicMixin.java");
        assertTrue(spawnerMixinFile.exists(), "MobSpawnerLogicMixin.java must exist");
        String spawnerMixinContent = Files.readString(spawnerMixinFile.toPath());
        assertTrue(spawnerMixinContent.contains("buffUpdateSpawns"), "Must implement buffUpdateSpawns");
        assertTrue(spawnerMixinContent.contains("clampServerTickDelay"), "Must implement clampServerTickDelay");

        File mixinsJsonFile = new File("src/main/resources/vanilla_reforged.mixins.json");
        String mixinsJsonContent = Files.readString(mixinsJsonFile.toPath());
        assertTrue(mixinsJsonContent.contains("\"MobSpawnerLogicMixin\""), "vanilla_reforged.mixins.json must register MobSpawnerLogicMixin");

        // 7. Verify XP Boosts in ModConfig and LivingEntityMixin
        assertEquals(1.20, com.vanillareforged.config.ModConfig.HOSTILE_MOB_XP_MULTIPLIER, 0.001, "Hostile mob XP multiplier must be +20% (1.20)");
        assertEquals(12, com.vanillareforged.config.ModConfig.ZOMBIFIED_PIGLIN_BASE_XP, "Zombified Piglin base XP must be 12 (buffed from 5)");

        File livingMixinFile = new File("src/main/java/com/vanillareforged/mixin/LivingEntityMixin.java");
        assertTrue(livingMixinFile.exists(), "LivingEntityMixin.java must exist");
        String livingMixinContent = Files.readString(livingMixinFile.toPath());
        assertTrue(livingMixinContent.contains("boostHostileAndPiglinXp"), "LivingEntityMixin must implement boostHostileAndPiglinXp");
        assertTrue(livingMixinContent.contains("entity instanceof Monster"), "XP boost must strictly apply only to Monster entities");
        assertTrue(livingMixinContent.contains("entity instanceof ZombifiedPiglinEntity"), "Zombified Piglins must receive additional base XP");
    }

    @Test
    @DisplayName("Verify King Zombie Hitbox Dimensions and 1.5x Model Alignment")
    public void testKingZombieHitboxDimensionsAndScaling() throws IOException {
        // 1. Verify ModEntities hitbox configuration uses dynamic changing dimensions
        File modEntitiesFile = new File("src/main/java/com/vanillareforged/entity/ModEntities.java");
        assertTrue(modEntitiesFile.exists());
        String entitiesContent = Files.readString(modEntitiesFile.toPath());

        assertTrue(entitiesContent.contains("EntityDimensions.changing(0.6f, 1.95f).withEyeHeight(1.74f)"),
            "ModEntities must define King Zombie dimensions with changing(0.6f, 1.95f).withEyeHeight(1.74f)");

        // 2. Verify KingZombieEntity overrides and dimension initialization
        File kingZombieFile = new File("src/main/java/com/vanillareforged/entity/KingZombieEntity.java");
        assertTrue(kingZombieFile.exists());
        String kingZombieContent = Files.readString(kingZombieFile.toPath());

        assertTrue(kingZombieContent.contains("this.calculateDimensions()"),
            "KingZombieEntity must call calculateDimensions() in constructor");
        assertTrue(kingZombieContent.contains("getBaseDimensions(EntityPose pose)"),
            "KingZombieEntity must override getBaseDimensions");

        // 3. Mathematical verification of 1.5x scaled hitbox matching the model
        net.minecraft.entity.EntityDimensions base = net.minecraft.entity.EntityDimensions.changing(0.6f, 1.95f).withEyeHeight(1.74f);
        net.minecraft.entity.EntityDimensions scaled = base.scaled(1.5f);

        assertEquals(0.9f, scaled.width(), 0.001f, "King Zombie scaled width must be exactly 0.9f (0.6f * 1.5)");
        assertEquals(2.925f, scaled.height(), 0.001f, "King Zombie scaled height must be exactly 2.925f (1.95f * 1.5)");
        assertEquals(2.61f, scaled.eyeHeight(), 0.001f, "King Zombie scaled eye height must be exactly 2.61f (1.74f * 1.5)");
        assertFalse(scaled.fixed(), "Scaled dimensions must not be fixed");
    }

    @Test
    @DisplayName("Verify Creeper No Block Destruction, Golden Spear Rebalance, and Piglin Gold Trim Pacification")
    public void testCreeperSpearAndPiglinTrimFeatures() throws IOException {
        // 1. Creeper Explosion Tuning
        assertFalse(com.vanillareforged.config.ModConfig.CREEPER_DESTROYS_BLOCKS,
            "Creeper explosions must not destroy blocks");
        assertEquals(1.5f, com.vanillareforged.config.ModConfig.CREEPER_EXPLOSION_POWER_MULTIPLIER, 0.001f,
            "Creeper blast impact multiplier must be 1.5x");

        File creeperMixinFile = new File("src/main/java/com/vanillareforged/mixin/CreeperEntityMixin.java");
        assertTrue(creeperMixinFile.exists());
        String creeperContent = Files.readString(creeperMixinFile.toPath());
        assertTrue(creeperContent.contains("modifyExplosionPower") && creeperContent.contains("modifyExplosionType"),
            "CreeperEntityMixin must implement modifyExplosionPower and modifyExplosionType using @ModifyArg");
        assertTrue(creeperContent.contains("World.ExplosionSourceType.NONE"),
            "CreeperEntityMixin must set explosion type to NONE to prevent block destruction");
        assertTrue(creeperContent.contains("ModConfig.CREEPER_EXPLOSION_POWER_MULTIPLIER"),
            "CreeperEntityMixin must apply power multiplier for greater blast impact");

        // 2. Golden Spear Rebalance
        File durabilityManagerFile = new File("src/main/java/com/vanillareforged/balance/DurabilityManager.java");
        assertTrue(durabilityManagerFile.exists(), "DurabilityManager.java must exist");
        String durContent = Files.readString(durabilityManagerFile.toPath());
        assertTrue(durContent.contains("registerTool(toolMap, durMap, Items.GOLDEN_SPEAR, new ToolStats(GOLD_TOOL_DURABILITY, 3.6, 1.9, null, GOLD_TOOL_ENCHANTABILITY));"),
            "Golden Spear must be registered in DurabilityManager with gold sword durability (1170), 3.6 damage (60%), 1.9 attack speed, and gold enchantability (22)");
        assertEquals(1170, DurabilityManager.GOLD_TOOL_DURABILITY,
            "Golden Spear must have gold sword durability (1170)");
        assertEquals(22, DurabilityManager.GOLD_TOOL_ENCHANTABILITY,
            "Golden Spear enchantability must match gold tools (22)");

        // 3. Piglin Gold Trim Pacification
        File piglinBrainMixinFile = new File("src/main/java/com/vanillareforged/mixin/PiglinBrainMixin.java");
        assertTrue(piglinBrainMixinFile.exists());
        String piglinBrainContent = Files.readString(piglinBrainMixinFile.toPath());
        assertTrue(piglinBrainContent.contains("isWearingPiglinSafeArmor"),
            "PiglinBrainMixin must inject into isWearingPiglinSafeArmor");
        assertTrue(piglinBrainContent.contains("DataComponentTypes.TRIM"),
            "PiglinBrainMixin must check DataComponentTypes.TRIM");
        assertTrue(piglinBrainContent.contains("ArmorTrimMaterials.GOLD"),
            "PiglinBrainMixin must check for gold trim material");

        File mixinsJsonFile = new File("src/main/resources/vanilla_reforged.mixins.json");
        String mixinsJsonContent = Files.readString(mixinsJsonFile.toPath());
        assertTrue(mixinsJsonContent.contains("\"PiglinBrainMixin\""),
            "vanilla_reforged.mixins.json must register PiglinBrainMixin");
    }
}



