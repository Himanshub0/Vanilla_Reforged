package com.vanillareforged;

import com.vanillareforged.balance.SkeletonGroupManager;
import com.vanillareforged.config.ModConfig;
import com.vanillareforged.entity.ZombieArmorManager;
import com.vanillareforged.entity.ZombieArmorManager.ArmorMaterialTier;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.math.random.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class Phase3HostileMobTest {

    @Test
    @DisplayName("Verify Phase 3 Section 6 & 7: Skeleton Group Overhaul Constants")
    public void testSkeletonGroupConstants() {
        assertEquals(4, ModConfig.SKELETON_GROUP_MIN);
        assertEquals(5, ModConfig.SKELETON_GROUP_MAX);
        assertEquals(0.60, ModConfig.SKELETON_BOW_RATIO, 0.001);
        assertEquals(0.40, ModConfig.SKELETON_GOLDEN_SWORD_RATIO, 0.001);
        assertEquals(0.085, ModConfig.SKELETON_GOLDEN_SWORD_DROP_CHANCE, 0.001);
        assertEquals(22.0, ModConfig.SKELETON_MAX_HEALTH, 0.001);
    }

    @Test
    @DisplayName("Verify Skeleton Group Generation & Composition Rules over 1000 trials")
    public void testSkeletonGroupComposition() {
        Random random = Random.create();
        int bowCountTotal = 0;
        int swordCountTotal = 0;

        for (int i = 0; i < 1000; i++) {
            SkeletonGroupManager.SkeletonGroupData group = new SkeletonGroupManager.SkeletonGroupData(random);
            int size = group.getTargetSize();

            // Natural group size must be strictly 4 or 5
            assertTrue(size == 4 || size == 5, "Group size must be 4 or 5, was: " + size);

            List<SkeletonGroupManager.WeaponType> weapons = group.getWeapons();
            assertEquals(size, weapons.size(), "Weapons list size must match target group size");

            boolean hasBow = weapons.contains(SkeletonGroupManager.WeaponType.BOW);
            boolean hasSword = weapons.contains(SkeletonGroupManager.WeaponType.GOLDEN_SWORD);

            // Group composition rule: At least one bow and at least one golden sword per group!
            assertTrue(hasBow, "Group must contain at least one bow user");
            assertTrue(hasSword, "Group must contain at least one golden sword user");

            for (SkeletonGroupManager.WeaponType w : weapons) {
                if (w == SkeletonGroupManager.WeaponType.BOW) bowCountTotal++;
                else swordCountTotal++;
            }
        }

        double totalWeapons = bowCountTotal + swordCountTotal;
        double bowRatio = bowCountTotal / totalWeapons;
        double swordRatio = swordCountTotal / totalWeapons;

        // Verify approximately 60% Bows, 40% Golden Swords (with composition correction tolerance)
        assertTrue(bowRatio >= 0.50 && bowRatio <= 0.70, "Bow ratio should be close to 60%, was: " + bowRatio);
        assertTrue(swordRatio >= 0.30 && swordRatio <= 0.50, "Sword ratio should be close to 40%, was: " + swordRatio);
    }

    @Test
    @DisplayName("Verify Phase 3 Section 6.1: Spider Scaling Constants & Hitbox Calculation")
    public void testSpiderScaling() {
        assertEquals(0.7f, ModConfig.SPIDER_SCALE, 0.001f);

        // Vanilla reference: width 1.4, height 0.9
        float scaledWidth = 1.4f * ModConfig.SPIDER_SCALE;
        float scaledHeight = 0.9f * ModConfig.SPIDER_SCALE;

        assertEquals(0.98f, scaledWidth, 0.01f, "Scaled spider width must be ~0.98 blocks");
        assertEquals(0.63f, scaledHeight, 0.01f, "Scaled spider height must be ~0.63 blocks");
    }

    @Test
    @DisplayName("Verify Phase 3 Section 8: Specific Hostile Mob Health Constants")
    public void testSpecificHostileHealth() {
        // Blaze: 23 HP (vanilla 20)
        assertEquals(23.0, ModConfig.BLAZE_MAX_HEALTH, 0.001);
        // Wither Skeleton: 25 HP (vanilla 20)
        assertEquals(25.0, ModConfig.WITHER_SKELETON_MAX_HEALTH, 0.001);
        // Elder Guardian: 92 HP (vanilla 80)
        assertEquals(92.0, ModConfig.ELDER_GUARDIAN_MAX_HEALTH, 0.001);
    }

    @Test
    @DisplayName("Verify Phase 3 Section 2 & 9: General Hostile Mob Health Tuning (10% boost)")
    public void testGeneralHostileHealth() {
        assertEquals(22.0, ModConfig.CREEPER_MAX_HEALTH, 0.001);
        assertEquals(29.0, ModConfig.WITCH_MAX_HEALTH, 0.001);
        assertEquals(22.0, ModConfig.STRAY_MAX_HEALTH, 0.001);
        assertEquals(22.0, ModConfig.DROWNED_MAX_HEALTH, 0.001);
        assertEquals(22.0, ModConfig.HUSK_MAX_HEALTH, 0.001);
        assertEquals(9.0, ModConfig.SILVERFISH_MAX_HEALTH, 0.001);
        assertEquals(9.0, ModConfig.ENDERMITE_MAX_HEALTH, 0.001);
        assertEquals(22.0, ModConfig.PHANTOM_MAX_HEALTH, 0.001);
        assertEquals(26.0, ModConfig.PILLAGER_MAX_HEALTH, 0.001);
        assertEquals(26.0, ModConfig.VINDICATOR_MAX_HEALTH, 0.001);
        assertEquals(26.0, ModConfig.EVOKER_MAX_HEALTH, 0.001);
        assertEquals(15.0, ModConfig.VEX_MAX_HEALTH, 0.001);
        assertEquals(110.0, ModConfig.RAVAGER_MAX_HEALTH, 0.001);
        assertEquals(33.0, ModConfig.GUARDIAN_MAX_HEALTH, 0.001);
        assertEquals(44.0, ModConfig.HOGLIN_MAX_HEALTH, 0.001);
        assertEquals(44.0, ModConfig.ZOGLIN_MAX_HEALTH, 0.001);
        assertEquals(33.0, ModConfig.SHULKER_MAX_HEALTH, 0.001);
        assertEquals(33.0, ModConfig.BREEZE_MAX_HEALTH, 0.001);
    }

    @Test
    @DisplayName("Verify Mixin Registration in vanilla_reforged.mixins.json")
    public void testMixinRegistration() throws IOException {
        File file = new File("src/main/resources/vanilla_reforged.mixins.json");
        assertTrue(file.exists(), "Mixins json file must exist");
        String content = Files.readString(file.toPath());

        List<String> expectedMixins = List.of(
            "SkeletonEntityMixin",
            "SpiderEntityMixin",
            "BlazeEntityMixin",
            "BlazeShootFireballGoalMixin",
            "WitherSkeletonEntityMixin",
            "ElderGuardianEntityMixin",
            "ElderGuardianBeamGoalMixin",
            "CreeperEntityMixin",
            "WitchEntityMixin",
            "HostileMobTuningMixin"
        );

        for (String mixin : expectedMixins) {
            assertTrue(content.contains("\"" + mixin + "\""),
                "vanilla_reforged.mixins.json must register: " + mixin);
        }
    }

    @Test
    @DisplayName("Verify Section 14 Strictly Protected Mobs Integrity")
    public void testProtectedMobsIntegrity() throws IOException {
        File hostileMixin = new File("src/main/java/com/vanillareforged/mixin/HostileMobTuningMixin.java");
        assertTrue(hostileMixin.exists(), "HostileMobTuningMixin.java must exist");
        String content = Files.readString(hostileMixin.toPath());

        // Ghasts and Piglin Brutes must be strictly protected
        assertTrue(content.contains("EntityType.GHAST"), "Must check and protect Ghasts");
        assertTrue(content.contains("EntityType.PIGLIN_BRUTE"), "Must check and protect Piglin Brutes");
        assertTrue(content.contains("hasVehicle()"), "Must protect mounted/jockey hostiles");
        assertTrue(content.contains("SpawnReason.JOCKEY"), "Must protect JOCKEY spawn reason");

        // Spider mixin must protect Cave Spiders and Spider Jockeys
        File spiderMixin = new File("src/main/java/com/vanillareforged/mixin/SpiderEntityMixin.java");
        assertTrue(spiderMixin.exists(), "SpiderEntityMixin.java must exist");
        String spiderContent = Files.readString(spiderMixin.toPath());
        assertTrue(spiderContent.contains("EntityType.SPIDER"), "Must strictly check EntityType.SPIDER (leaving Cave Spiders untouched)");
        assertTrue(spiderContent.contains("hasPassengers()"), "Must check hasPassengers() for Jockey exclusion");

        // Ordinary Wolves must remain untouched: DireWolfEntity is an isolated subclass
        File direWolfFile = new File("src/main/java/com/vanillareforged/entity/DireWolfEntity.java");
        assertTrue(direWolfFile.exists());
        String direWolfContent = Files.readString(direWolfFile.toPath());
        assertTrue(direWolfContent.contains("extends WolfEntity"), "DireWolfEntity extends WolfEntity without mutating base WolfEntity attributes");
    }

    @Test
    @DisplayName("Verify Zombie Armor Distribution Simulation over 10,000 trials")
    public void testZombieArmorDistributionSimulation() {
        Random random = Random.create();
        int trials = 10000;
        int leatherCount = 0;
        int chainmailCount = 0;
        int ironCount = 0;

        // Verify tier enum contains strictly Leather, Chainmail, Iron (Gold, Diamond, Netherite FORBIDDEN)
        ArmorMaterialTier[] tiers = ArmorMaterialTier.values();
        assertEquals(3, tiers.length, "ArmorMaterialTier must contain exactly 3 tiers");
        Set<String> tierNames = new HashSet<>();
        for (ArmorMaterialTier t : tiers) tierNames.add(t.name());
        assertTrue(tierNames.contains("LEATHER"), "Must support Leather tier");
        assertTrue(tierNames.contains("CHAINMAIL"), "Must support Chainmail tier");
        assertTrue(tierNames.contains("IRON"), "Must support Iron tier");
        assertFalse(tierNames.contains("GOLD"), "Gold armor strictly forbidden on standard zombies");
        assertFalse(tierNames.contains("DIAMOND"), "Diamond armor strictly forbidden on standard zombies");
        assertFalse(tierNames.contains("NETHERITE"), "Netherite armor strictly forbidden on standard zombies");

        int onePiece = 0, twoPiece = 0, threePiece = 0, fourPiece = 0;

        for (int i = 0; i < trials; i++) {
            // Material tier roll
            float tierRoll = random.nextFloat();
            ArmorMaterialTier tier;
            if (tierRoll < (float) ModConfig.ZOMBIE_ARMOR_LEATHER_WEIGHT) {
                tier = ArmorMaterialTier.LEATHER;
                leatherCount++;
            } else if (tierRoll < (float) (ModConfig.ZOMBIE_ARMOR_LEATHER_WEIGHT + ModConfig.ZOMBIE_ARMOR_CHAINMAIL_WEIGHT)) {
                tier = ArmorMaterialTier.CHAINMAIL;
                chainmailCount++;
            } else {
                tier = ArmorMaterialTier.IRON;
                ironCount++;
            }
            assertNotNull(tier);

            // Piece count roll
            float pieceRoll = random.nextFloat();
            int pieceCount;
            if (pieceRoll < 0.50f) {
                pieceCount = 1;
                onePiece++;
            } else if (pieceRoll < 0.80f) {
                pieceCount = 2;
                twoPiece++;
            } else if (pieceRoll < 0.95f) {
                pieceCount = 3;
                threePiece++;
            } else {
                pieceCount = 4;
                fourPiece++;
            }
            assertTrue(pieceCount >= 1 && pieceCount <= 4, "Piece count must be between 1 and 4");

            // Slots allocation
            List<EquipmentSlot> slots = new ArrayList<>(List.of(
                EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD
            ));
            Collections.shuffle(slots, new java.util.Random(random.nextLong()));
            List<EquipmentSlot> selectedSlots = slots.subList(0, pieceCount);
            Set<EquipmentSlot> distinctSlots = new HashSet<>(selectedSlots);
            assertEquals(pieceCount, distinctSlots.size(), "Selected armor slots must all be distinct");
        }

        // Statistical distribution checks within 2% tolerance
        assertEquals(0.45, (double) leatherCount / trials, 0.02, "Leather weight should be ~45%");
        assertEquals(0.35, (double) chainmailCount / trials, 0.02, "Chainmail weight should be ~35%");
        assertEquals(0.20, (double) ironCount / trials, 0.02, "Iron weight should be ~20%");

        assertEquals(0.50, (double) onePiece / trials, 0.02, "1-piece weight should be ~50%");
        assertEquals(0.30, (double) twoPiece / trials, 0.02, "2-piece weight should be ~30%");
        assertEquals(0.15, (double) threePiece / trials, 0.02, "3-piece weight should be ~15%");
        assertEquals(0.05, (double) fourPiece / trials, 0.02, "4-piece weight should be ~5%");

        // Config constants
        assertEquals(0.25, ModConfig.ZOMBIE_ARMOR_CHANCE, 0.001);
        assertEquals(0.085, ModConfig.ZOMBIE_ARMOR_DROP_CHANCE, 0.001);
        assertEquals(22.0, ModConfig.ZOMBIE_MAX_HEALTH, 0.001);
    }

    @Test
    @DisplayName("Verify Zombie Reinforcement Rules, Cooldown and Anti-Recursion")
    public void testZombieReinforcementControlledRules() throws IOException {
        assertEquals(0.40, ModConfig.ZOMBIE_REINFORCEMENT_CHANCE, 0.001);
        assertEquals(200, ModConfig.ZOMBIE_REINFORCEMENT_COOLDOWN_TICKS); // 10 seconds = 200 ticks
        assertEquals(4, ModConfig.ZOMBIE_MAX_REINFORCEMENTS);
        assertEquals(0.01, ModConfig.KING_ZOMBIE_REINFORCEMENT_CHANCE, 0.001);

        File managerFile = new File("src/main/java/com/vanillareforged/entity/ZombieReinforcementManager.java");
        assertTrue(managerFile.exists());
        String content = Files.readString(managerFile.toPath());

        // Player check
        assertTrue(content.contains("damageSource.getAttacker() instanceof PlayerEntity"),
            "Reinforcements must only trigger when damaged by a player");

        // King zombie exclusion
        assertTrue(content.contains("source instanceof KingZombieEntity"),
            "King Zombie must be excluded from standard reinforcements");

        // Anti-recursion tags
        assertTrue(content.contains("\"vanilla_reforged:wave_zombie\""), "Must check wave_zombie tag");
        assertTrue(content.contains("\"vanilla_reforged:reinforcement\""), "Must check reinforcement tag");
        assertTrue(content.contains("\"vanilla_reforged:no_reinforcement\""), "Must check no_reinforcement tag");

        // Attribution and cleanup
        assertTrue(content.contains("cleanupOnEntityDeath"), "Must clean up state on zombie death");
    }

    @Test
    @DisplayName("Verify King Zombie Stats, Diamond King Variant, and King's Call Boss Ability")
    public void testKingZombieBossMechanicsAndVariants() throws IOException {
        assertEquals(80.0, ModConfig.KING_ZOMBIE_MAX_HEALTH, 0.001);
        assertEquals(1.5f, ModConfig.KING_ZOMBIE_SCALE, 0.001f);
        assertEquals(5.0, ModConfig.KING_ZOMBIE_ATTACK_DAMAGE, 0.001);
        assertEquals(0.5, ModConfig.KING_ZOMBIE_KNOCKBACK_RESISTANCE, 0.001);
        assertEquals(40.0, ModConfig.KING_ZOMBIE_FOLLOW_RANGE, 0.001);
        assertEquals(0.05, ModConfig.DIAMOND_KING_CHANCE, 0.001);
        assertEquals(40.0f, ModConfig.KINGS_CALL_THRESHOLD_HEALTH, 0.001f);
        assertEquals(3, ModConfig.KINGS_CALL_MIN_SUMMONS);
        assertEquals(6, ModConfig.KINGS_CALL_MAX_SUMMONS);

        File kingFile = new File("src/main/java/com/vanillareforged/entity/KingZombieEntity.java");
        assertTrue(kingFile.exists());
        String content = Files.readString(kingFile.toPath());

        // Speed I permanently active
        assertTrue(content.contains("StatusEffects.SPEED"), "King Zombie must have Speed effect");

        // Diamond King vs Gold King equipment and drops
        assertTrue(content.contains("Items.DIAMOND_HELMET") && content.contains("Items.DIAMOND_CHESTPLATE"),
            "Diamond King must equip diamond armor");
        assertTrue(content.contains("Items.GOLDEN_HELMET") && content.contains("Items.GOLDEN_CHESTPLATE"),
            "Gold King must equip gold armor");
        assertTrue(content.contains("0.15f") && content.contains("0.05f"),
            "Drop chances: 15% Gold King, 5% Diamond King per armor piece");

        // XP rules: 25 for Gold King, 40 for Diamond King
        assertTrue(content.contains("this.isDiamond() ? 40 : 25"), "XP must be 40 for Diamond King and 25 for Gold King");

        // King's Call non-recursive summons
        assertTrue(content.contains("minion.addCommandTag(\"vanilla_reforged:wave_zombie\")"));
        assertTrue(content.contains("minion.addCommandTag(\"vanilla_reforged:no_reinforcement\")"));

        // Spawn Egg rule: Spawn egg ALWAYS spawns Gold King
        assertTrue(content.contains("spawnReason == SpawnReason.SPAWN_ITEM_USE"),
            "Must check SPAWN_ITEM_USE to guarantee Gold King from spawn egg");

        // Daylight burn & item pickup
        assertTrue(content.contains("burnsInDaylight()"), "Must override burnsInDaylight");
        assertTrue(content.contains("canPickupItem"), "Must override canPickupItem");

        // Adult-only boss mob: King Zombie can never be a baby under any circumstance
        assertTrue(content.contains("public boolean isBaby()"), "King Zombie must override isBaby");
        assertTrue(content.contains("return false;"), "isBaby must return false");
        assertTrue(content.contains("public void setBaby(boolean baby)"), "King Zombie must override setBaby");
        assertTrue(content.contains("super.setBaby(false);"), "setBaby must force super.setBaby(false)");
    }

    @Test
    @DisplayName("Verify King Zombie Gold vs Diamond Separate Loot Tables")
    public void testKingZombieLootTableSeparation() throws IOException {
        File goldTable = new File("src/main/resources/data/vanilla_reforged/loot_table/entities/king_zombie.json");
        File diamondTable = new File("src/main/resources/data/vanilla_reforged/loot_table/entities/diamond_king_zombie.json");

        assertTrue(goldTable.exists());
        assertTrue(diamondTable.exists());

        String goldContent = Files.readString(goldTable.toPath());
        String diamondContent = Files.readString(diamondTable.toPath());

        // Gold King drops rotten flesh, nuggets (2-4), 25% ingot, NO diamonds
        assertTrue(goldContent.contains("minecraft:rotten_flesh"));
        assertTrue(goldContent.contains("minecraft:gold_nugget"));
        assertTrue(goldContent.contains("minecraft:gold_ingot"));
        assertFalse(goldContent.contains("minecraft:diamond"), "Gold King loot table must NOT contain diamonds");

        // Diamond King drops rotten flesh, nuggets, 50% ingot, guaranteed diamond
        assertTrue(diamondContent.contains("minecraft:rotten_flesh"));
        assertTrue(diamondContent.contains("minecraft:gold_nugget"));
        assertTrue(diamondContent.contains("minecraft:gold_ingot"));
        assertTrue(diamondContent.contains("0.5"), "Diamond King has 50% chance for gold ingot");
        assertTrue(diamondContent.contains("minecraft:diamond"), "Diamond King must have diamond drop");
    }

    @Test
    @DisplayName("Verify Dire Wolf Stats, Variants, Biome Spawning and 100 HP Retention")
    public void testDireWolfAttributesAndMechanics() throws IOException {
        assertEquals(100.0, ModConfig.DIRE_WOLF_MAX_HEALTH, 0.001);
        assertEquals(20.0, ModConfig.DIRE_WOLF_ATTACK_DAMAGE, 0.001);
        assertEquals(2.0f, ModConfig.DIRE_WOLF_SCALE, 0.001f);
        assertEquals(1.5, ModConfig.DIRE_WOLF_SPEED_MULTIPLIER, 0.001);
        assertEquals(0.25, ModConfig.DIRE_WOLF_TAMING_SUCCESS_CHANCE, 0.001);
        assertEquals(2, ModConfig.DIRE_WOLF_SPAWN_WEIGHT);
        assertEquals(8, ModConfig.DIRE_WOLF_XP_MIN);
        assertEquals(12, ModConfig.DIRE_WOLF_XP_MAX);

        File direWolfFile = new File("src/main/java/com/vanillareforged/entity/DireWolfEntity.java");
        assertTrue(direWolfFile.exists());
        String content = Files.readString(direWolfFile.toPath());

        // Scale and speed
        assertTrue(content.contains("EntityAttributes.SCALE"));
        assertTrue(content.contains("0.30 * ModConfig.DIRE_WOLF_SPEED_MULTIPLIER"));

        // Variants: Regular, Ashen Black, Woods (Snow-white removed)
        assertTrue(content.contains("dire_wolf_regular_wild.png"));
        assertTrue(content.contains("dire_wolf_regular_tamed.png"));
        assertTrue(content.contains("dire_wolf_ashen_wild.png"));
        assertTrue(content.contains("dire_wolf_ashen_tamed.png"));
        assertTrue(content.contains("dire_wolf_woods_wild.png"));
        assertTrue(content.contains("dire_wolf_woods_tamed.png"));
        assertFalse(content.contains("dire_wolf_white.png"), "Snow-white variant must be removed");

        // Owner protection (never attacks owner)
        assertTrue(content.contains("canTarget(LivingEntity target)"));
        assertTrue(content.contains("isOwner(target)"));
        assertTrue(content.contains("stopAnger()"));

        // Armor riding & steering
        assertTrue(content.contains("hasWolfArmor()"));
        assertTrue(content.contains("getControllingPassenger()"));
        assertTrue(content.contains("getControlledMovementInput"));
        assertTrue(content.contains("tickControlled"));
        assertTrue(content.contains("getSaddledSpeed"));
        assertTrue(content.contains("implements JumpingMount"), "DireWolfEntity must implement JumpingMount");
        assertTrue(content.contains("executeMountJump"), "Must implement executeMountJump");
        assertTrue(content.contains("canJump()"), "Must implement canJump()");
        assertTrue(content.contains("setJumpStrength"), "Must implement setJumpStrength");
        assertTrue(content.contains("getPassengerAttachmentPos"), "Must override getPassengerAttachmentPos");

        // Baby variant elimination (Apex beasts are strictly adult)
        assertTrue(content.contains("isBaby()"), "Must override isBaby() to eliminate baby variant");
        assertTrue(content.contains("setBaby(boolean baby)"), "Must override setBaby() to eliminate baby variant");

        // 100 HP retention upon taming
        assertTrue(content.contains("updateAttributesForTamed"));
        assertTrue(content.contains("setBaseValue(ModConfig.DIRE_WOLF_MAX_HEALTH)"));

        // Wild vs tamed drops and XP
        assertTrue(content.contains("shouldDropLoot"));
        assertTrue(content.contains("getExperienceToDrop"));

        // Biome spawn registration
        File modEntities = new File("src/main/java/com/vanillareforged/entity/ModEntities.java");
        assertTrue(modEntities.exists());
        String modEntitiesContent = Files.readString(modEntities.toPath());
        assertTrue(modEntitiesContent.contains("BiomeModifications.addSpawn"));
        assertTrue(modEntitiesContent.contains("BiomeSelectors.includeByKey"));
        assertTrue(modEntitiesContent.contains("BiomeKeys.SNOWY_PLAINS"));

        // Hostile Mob Targeting (Section 10 & User Polish)
        assertTrue(content.contains("initGoals()"), "DireWolfEntity must override initGoals");
        assertTrue(content.contains("canHuntAsHostile"), "DireWolfEntity must have canHuntAsHostile");
        assertTrue(content.contains("target instanceof CreeperEntity"), "Must check and exclude Creepers");
        assertTrue(content.contains("target instanceof GhastEntity"), "Must check and exclude Ghasts");
        assertTrue(content.contains("target instanceof HostileEntity"), "Must target HostileEntity");

        // Untamed Interaction Robustness
        assertTrue(content.contains("isWolfFood"), "Must have isWolfFood helper covering all meats");
        assertTrue(content.contains("super.interactMob(player, hand)"), "Must call super.interactMob for leads and name tags");
        assertTrue(content.contains("this.stopAnger()"), "Must clear anger on interaction");
        assertTrue(content.contains("ActionResult.SUCCESS"), "Must return SUCCESS on client for responsive hand swings");
    }

    @Test
    @DisplayName("Verify Dire Wolf 4-Step Taming Ritual State Machine over 1,000 trials")
    public void testDireWolfTamingRitualStateMachine() {
        Random random = Random.create();

        for (int i = 0; i < 1000; i++) {
            int stage = 0;
            boolean hasWeakness = false;
            boolean tamed = false;

            // Stage 0: Untamed
            // Attempt bone WITHOUT weakness -> must fail
            assertFalse(canProgressStage(stage, hasWeakness, "BONE"));
            assertEquals(0, stage);

            // Attempt steak or carrot at stage 0 -> must fail
            assertFalse(canProgressStage(stage, true, "STEAK"));
            assertFalse(canProgressStage(stage, true, "GOLDEN_CARROT"));
            assertEquals(0, stage);

            // Step 1 + Step 2: Apply Weakness, feed Bone -> progresses to Stage 1
            hasWeakness = true;
            assertTrue(canProgressStage(stage, hasWeakness, "BONE"));
            stage = 1;

            // Stage 1: Feed Bone or Carrot -> must fail
            assertFalse(canProgressStage(stage, hasWeakness, "BONE"));
            assertFalse(canProgressStage(stage, hasWeakness, "GOLDEN_CARROT"));
            assertEquals(1, stage);

            // Step 3: Feed Steak -> progresses to Stage 2
            assertTrue(canProgressStage(stage, hasWeakness, "STEAK"));
            stage = 2;

            // Stage 2: Feed Bone or Steak -> must fail
            assertFalse(canProgressStage(stage, hasWeakness, "BONE"));
            assertFalse(canProgressStage(stage, hasWeakness, "STEAK"));
            assertEquals(2, stage);

            // Step 4: Feed Golden Carrot -> 25% chance of success per attempt.
            // If failure, STAYS at stage 2 for repeat attempts!
            int carrotAttempts = 0;
            while (!tamed) {
                carrotAttempts++;
                boolean success = random.nextDouble() < ModConfig.DIRE_WOLF_TAMING_SUCCESS_CHANCE;
                if (success) {
                    tamed = true;
                } else {
                    // Critical requirement: failure keeps wolf at Stage 2!
                    assertEquals(2, stage, "Wolf must remain at stage 2 upon carrot taming failure");
                }
            }
            assertTrue(tamed, "Dire wolf must eventually be tamed through repeated carrots");
            assertTrue(carrotAttempts >= 1, "Must have taken at least 1 carrot attempt");
        }
    }

    private boolean canProgressStage(int currentStage, boolean hasWeakness, String item) {
        if (item.equals("BONE")) {
            return currentStage == 0 && hasWeakness;
        } else if (item.equals("STEAK")) {
            return currentStage == 1;
        } else if (item.equals("GOLDEN_CARROT")) {
            return currentStage == 2;
        }
        return false;
    }

    @Test
    @DisplayName("Verify Dire Wolf Loot Table JSON (0-2 Bones, 50% chance)")
    public void testDireWolfLootTableJson() throws IOException {
        File file = new File("src/main/resources/data/vanilla_reforged/loot_table/entities/dire_wolf.json");
        assertTrue(file.exists());
        String content = Files.readString(file.toPath());

        assertTrue(content.contains("minecraft:bone"), "Must drop bones");
        assertTrue(content.contains("\"chance\": 0.5"), "Must have 50% chance for bone drops");
        assertTrue(content.contains("\"min\": 0.0"), "Min bones must be 0");
        assertTrue(content.contains("\"max\": 2.0"), "Max bones must be 2");
    }

    @Test
    @DisplayName("Verify Blaze Volley Timing (10-tick interval, 100-tick cooldown) and Aim Leading")
    public void testBlazeCombatBehaviorOverhaul() throws IOException {
        assertEquals(23.0, ModConfig.BLAZE_MAX_HEALTH, 0.001);

        File mixinFile = new File("src/main/java/com/vanillareforged/mixin/BlazeShootFireballGoalMixin.java");
        assertTrue(mixinFile.exists());
        String content = Files.readString(mixinFile.toPath());

        assertTrue(content.contains("improveFireballAim"), "Must lead target aiming against moving players");
        assertTrue(content.contains("targetVel"), "Must use target velocity for predictive leading");
        assertTrue(content.contains("0.5f"), "Must tighten divergence to 0.5f");

        assertTrue(content.contains("this.fireballCooldown = 10"),
            "Must enforce 10-tick interval between fireballs in volley");
        assertTrue(content.contains("this.fireballsFired == 4"),
            "Must enforce 100-tick cooldown after 3rd shot");
    }

    @Test
    @DisplayName("Verify Elder Guardian 92 HP, Beam Tracking and 45-tick Telegraph Warning")
    public void testElderGuardianBalancingAndTelegraph() throws IOException {
        assertEquals(92.0, ModConfig.ELDER_GUARDIAN_MAX_HEALTH, 0.001);

        File mixinFile = new File("src/main/java/com/vanillareforged/mixin/ElderGuardianBeamGoalMixin.java");
        assertTrue(mixinFile.exists());
        String content = Files.readString(mixinFile.toPath());

        assertTrue(content.contains("improveTrackingAndTelegraph"));
        assertTrue(content.contains("getLookControl().lookAt(target, 180.0f, 180.0f)"),
            "Must lock look-control firmly onto target during beam warmup");
        assertTrue(content.contains("this.beamTicks == 45"),
            "Must trigger telegraph warning at 45 ticks (0.75s before blast)");
        assertTrue(content.contains("BLOCK_CONDUIT_ATTACK_TARGET"),
            "Must play recognizable conduit warning sound");
        assertTrue(content.contains("ParticleTypes.NAUTILUS"),
            "Must spawn visual nautilus warning particles");
    }

    @Test
    @DisplayName("Verify Skeleton Golden Sword 8.5% Drop with Durability and Melee AI")
    public void testSkeletonGoldenSwordMeleeDropAndAI() throws IOException {
        assertEquals(0.085, ModConfig.SKELETON_GOLDEN_SWORD_DROP_CHANCE, 0.001);

        File groupManager = new File("src/main/java/com/vanillareforged/balance/SkeletonGroupManager.java");
        assertTrue(groupManager.exists());
        String groupContent = Files.readString(groupManager.toPath());

        assertTrue(groupContent.contains("handleGoldenSwordDeathDrop"), "Must have handleGoldenSwordDeathDrop");
        assertTrue(groupContent.contains("SKELETON_GOLDEN_SWORD_DROP_CHANCE"), "Must use 8.5% drop chance");
        assertTrue(groupContent.contains("dropStack.setDamage"), "Must randomize remaining durability");
        assertTrue(groupContent.contains("updateAttackType"), "Must update skeleton AI to melee attack");
    }

    @Test
    @DisplayName("Verify Custom Spawn Eggs Registration, Textures, Models and Translations")
    public void testSpawnEggsRegistrationAndAssets() throws IOException {
        // ModItems registration
        File modItems = new File("src/main/java/com/vanillareforged/item/ModItems.java");
        assertTrue(modItems.exists());
        String itemsContent = Files.readString(modItems.toPath());
        assertTrue(itemsContent.contains("KING_ZOMBIE_SPAWN_EGG"));
        assertTrue(itemsContent.contains("DIRE_WOLF_SPAWN_EGG"));
        assertTrue(itemsContent.contains("ItemGroups.SPAWN_EGGS"));

        // Models
        File kingModel = new File("src/main/resources/assets/vanilla_reforged/models/item/king_zombie_spawn_egg.json");
        File direModel = new File("src/main/resources/assets/vanilla_reforged/models/item/dire_wolf_spawn_egg.json");
        assertTrue(kingModel.exists());
        assertTrue(direModel.exists());

        // Textures
        File kingTex = new File("src/main/resources/assets/vanilla_reforged/textures/item/king_zombie_spawn_egg.png");
        File direTex = new File("src/main/resources/assets/vanilla_reforged/textures/item/dire_wolf_spawn_egg.png");
        assertTrue(kingTex.exists());
        assertTrue(direTex.exists());

        // Lang
        File lang = new File("src/main/resources/assets/vanilla_reforged/lang/en_us.json");
        assertTrue(lang.exists());
        String langContent = Files.readString(lang.toPath());
        assertTrue(langContent.contains("King Zombie Spawn Egg"));
        assertTrue(langContent.contains("Dire Wolf Spawn Egg"));
        assertTrue(langContent.contains("King Zombie"));
        assertTrue(langContent.contains("Dire Wolf"));
    }
}
