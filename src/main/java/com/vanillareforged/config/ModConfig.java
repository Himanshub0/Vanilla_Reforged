package com.vanillareforged.config;

/**
 * Phase 3 Configuration and Tuning Parameters
 * All values default strictly to the VANILLA REFORGED Phase 3 Master Specification.
 */
public class ModConfig {
    // Section 3: Zombie Family
    public static double ZOMBIE_MAX_HEALTH = 22.0;
    public static double ZOMBIE_REINFORCEMENT_CHANCE = 0.40;
    public static int ZOMBIE_REINFORCEMENT_COOLDOWN_TICKS = 200; // 10 seconds
    public static int ZOMBIE_MAX_REINFORCEMENTS = 4;
    public static double ZOMBIE_ARMOR_CHANCE = 0.25;
    public static double KING_ZOMBIE_REINFORCEMENT_CHANCE = 0.01;
    public static double DIAMOND_KING_CHANCE = 0.05;
    public static double ZOMBIE_ARMOR_DROP_CHANCE = 0.085;

    // Armor distribution weights among armored spawns (must sum to 1.0)
    public static double ZOMBIE_ARMOR_LEATHER_WEIGHT = 0.45;
    public static double ZOMBIE_ARMOR_CHAINMAIL_WEIGHT = 0.35;
    public static double ZOMBIE_ARMOR_IRON_WEIGHT = 0.20;

    // Section 4: King Zombie
    public static double KING_ZOMBIE_MAX_HEALTH = 80.0;
    public static float KING_ZOMBIE_SCALE = 1.5f;
    public static double KING_ZOMBIE_ATTACK_DAMAGE = 5.0;
    public static double KING_ZOMBIE_KNOCKBACK_RESISTANCE = 0.5;
    public static double KING_ZOMBIE_FOLLOW_RANGE = 40.0;
    public static float KINGS_CALL_THRESHOLD_HEALTH = 40.0f;
    public static int KINGS_CALL_MIN_SUMMONS = 3;
    public static int KINGS_CALL_MAX_SUMMONS = 6;
    public static double KINGS_CALL_TRIGGER_RELIABILITY = 1.0;

    // Section 6: Skeletons & Spiders
    public static int SKELETON_GROUP_MIN = 4;
    public static int SKELETON_GROUP_MAX = 5;
    public static double SKELETON_BOW_RATIO = 0.60;
    public static double SKELETON_GOLDEN_SWORD_RATIO = 0.40;
    public static double SKELETON_GOLDEN_SWORD_DROP_CHANCE = 0.085;
    public static float SPIDER_SCALE = 0.7f;

    // Section 8: Specific Hostile Mobs
    public static double BLAZE_MAX_HEALTH = 23.0;
    public static double WITHER_SKELETON_MAX_HEALTH = 25.0;
    public static double ELDER_GUARDIAN_MAX_HEALTH = 92.0;

    // Section 2 & 9: General Hostile Mob Health Tuning (10% Increase)
    public static double CREEPER_MAX_HEALTH = 22.0;
    public static boolean CREEPER_DESTROYS_BLOCKS = false;
    public static float CREEPER_EXPLOSION_POWER_MULTIPLIER = 1.5f;
    public static double WITCH_MAX_HEALTH = 29.0;
    public static double SKELETON_MAX_HEALTH = 22.0;
    public static double STRAY_MAX_HEALTH = 22.0;
    public static double DROWNED_MAX_HEALTH = 22.0;
    public static double HUSK_MAX_HEALTH = 22.0;
    public static double SILVERFISH_MAX_HEALTH = 9.0;
    public static double ENDERMITE_MAX_HEALTH = 9.0;
    public static double PHANTOM_MAX_HEALTH = 22.0;
    public static double PILLAGER_MAX_HEALTH = 26.0;
    public static double VINDICATOR_MAX_HEALTH = 26.0;
    public static double EVOKER_MAX_HEALTH = 26.0;
    public static double VEX_MAX_HEALTH = 15.0;
    public static double RAVAGER_MAX_HEALTH = 110.0;
    public static double GUARDIAN_MAX_HEALTH = 33.0;
    public static double HOGLIN_MAX_HEALTH = 44.0;
    public static double ZOGLIN_MAX_HEALTH = 44.0;
    public static double SHULKER_MAX_HEALTH = 33.0;
    public static double BREEZE_MAX_HEALTH = 33.0;

    // Section 10: Dire Wolf (Stats & Mount Dynamics)
    public static double DIRE_WOLF_MAX_HEALTH = 100.0;
    public static double DIRE_WOLF_ATTACK_DAMAGE = 20.0; // 5x normal wolf (4.0 * 5)
    public static float DIRE_WOLF_SCALE = 2.0f;
    public static double DIRE_WOLF_SPEED = 0.255; // 11.0 blocks/sec (0.255 * 43.17 = 11.0 blocks/sec)
    public static double DIRE_WOLF_SPEED_MULTIPLIER = 1.5;
    public static double DIRE_WOLF_JUMP_STRENGTH = 0.70; // Horse-like jump (~2.0 - 2.5 blocks)
    public static double DIRE_WOLF_TAMING_SUCCESS_CHANCE = 0.25;
    public static int DIRE_WOLF_SPAWN_WEIGHT = 2;
    public static int DIRE_WOLF_XP_MIN = 8;
    public static int DIRE_WOLF_XP_MAX = 12;

    // Mob Spawner Block Buffs (Continuous Mob Farming)
    public static int SPAWNER_MIN_DELAY = 20; // 1.0 second (vanilla: 200 / 10s)
    public static int SPAWNER_MAX_DELAY = 60; // 3.0 seconds (vanilla: 800 / 40s)
    public static int SPAWNER_MAX_NEARBY_ENTITIES = 16; // vanilla: 6 (prevents spawner stalling in mob farms)
    public static int SPAWNER_SPAWN_COUNT = 4; // vanilla: 4

    // Experience Drops Tuning
    public static double HOSTILE_MOB_XP_MULTIPLIER = 1.20; // +20% more XP dropped by all hostile mobs
    public static int ZOMBIFIED_PIGLIN_BASE_XP = 12; // Base XP for Zombie Piglins (vanilla: 5)

    // Section 11: Crossbow Ammunition (Amethyst & Echo Shards)
    public static float ECHO_SHARD_SONIC_BOOM_DAMAGE = 12.0f; // 40% of Warden's 30.0 sonic boom
    public static int AMETHYST_SHARD_WEAKNESS_TICKS = 2400; // 2 minutes (120s * 20t)
}

