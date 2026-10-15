# ⚔️ Vanilla Reforged

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1%20%2F%201.21.11-brightgreen.svg)](https://minecraft.net/)
[![Fabric](https://img.shields.io/badge/Loader-Fabric-blue.svg)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A comprehensive Vanilla+ overhaul mod for **Minecraft Fabric 1.21.1 / 1.21.11** that revitalizes progression, enchanting, combat mechanics, hostile mob encounters, companion mounts, and the economy—all while strictly preserving the authentic vanilla spirit and aesthetic.

---

## 🌟 Key Features

### 💎 1. Progression Rework
* **Diamond Crystals:** Mining diamond ores now yields **Diamond Crystals** instead of raw diamonds. Four crystals can be crafted into a Diamond.
* **Smithing Table Diamond Progression:** Diamond equipment cannot be crafted directly in a crafting table. Instead, upgrade iron gear using the **Diamond Upgrade Smithing Template** and Diamond Crystals in the Smithing Table.
* **Template Duplication:** The Diamond Upgrade Smithing Template is rare and valuable, but can be duplicated using diamonds, Netherrack, and Diamond Crystals.
* **Villager Economy Balance:** Armorers, Toolsmiths, and Weaponsmiths no longer sell diamond tools, weapons, or armor for emeralds. Diamond tier must be earned through exploration and mining.

---

### ⚡ 2. Enchanting Overhaul & Dual-Resource System
* **Dual Reagents in Enchanting Table:**
  * **Lapis Lazuli:** Standard vanilla enchanting pool.
  * **Gold Ingots:** Unlocks custom Vanilla Reforged enchantments with an effective +2 power level boost.
* **20 New Custom Enchantments & Curses:**
  * **Combat:**
    * `Ice Aspect`: Freezing slowness on attack.
    * `Breeze Burst`: Bow arrows generate wind charges upon impact.
    * `Outreach`: Extends melee attack reach (+0.5 blocks per level).
    * `Swift Strike`: Decreases weapon attack cooldown for faster swings.
    * `Toxic`: Ranged arrows inflict poison damage (undead mobs immune).
    * `Websnare`: Chance to spawn cobwebs at the victim's feet.
  * **Mining & Utility:**
    * `Pyrolysis`: **Pickaxe-exclusive** instant auto-smelting for ores and minerals with furnace crackle and flame particles.
    * `Gluttony`: Direct inventory vacuum for mined block drops and experience orbs.
    * `Crab's Touch`: Increases block reach and building distance.
  * **Armor & Movement:**
    * `Stride`: Step up 1 full block smoothly without jumping (strictly 1 level max, cannot jump fences/2-block walls).
    * `Scorch Walker`: The Nether counterpart to Frost Walker. Turns lava surface into temporary magma beneath your feet that reverts back after 2.5 seconds. Immune to hot floor damage.
    * `Vitality`: Boosts maximum health (+2 HP per level, up to 13 hearts).
    * `Kinetic Protection`: Helmet enchantment reducing Elytra kinetic crash damage.
    * `Retrieval`: Chestplate enchantment with a chance to catch incoming enemy arrows into your inventory.
    * `Skyguard`: Elytra protection enchantment reducing damage taken during flight.
    * `Graviole`: Reduces gravity while gliding for extended flight distances.
  * **Curses:**
    * `Curse of Breaking`: Multiplies durability loss by 4x.
    * `Curse of Clumsiness`: 15% chance to drop held item when mining or attacking.
    * `Curse of Displacement`: Randomly teleports enemies behind the attacker.
    * `Curse of Double Edge`: Recoil damage chance back to the attacker.
* **Square, Sweet Tooltips:** Every enchantment book and gear tooltip features formatted item compatibility badges (`Fits: [Items]`) and concise mechanical descriptions.

---

### 🧟 3. Hostile Mobs & King Zombie Boss
* **King Zombie Boss:**
  * Rare natural spawn (100 HP, 1.5x model scale, full Golden Armor, or 5% rare Diamond King variant).
  * **Dynamic Hitbox:** Custom 1.5x hitbox ($0.9\text{m} \times 2.925\text{m}$) aligned to the towering visual model.
  * **King's Call:** When falling below 50 HP for the first time, lets out a bellowing sound cue and summons a non-recursive reinforcement horde of 5–10 zombies.
* **Skeleton Tactics:** Natural spawns group into squads of 4–5 containing both long-range archers and Golden Sword melee shock troops with pursuit AI.
* **Agile Spiders:** Standalone spiders scaled to 0.7x for fast, scurrying swarm combat (Spider Jockeys strictly preserved at vanilla 1.0x).
* **XP Drops & Hostile Balancing:** All hostile mobs drop **+20% more experience**. Zombie Piglins drop increased base XP (15 XP total) for rewarding Nether farming.
* **Buffed Mob Spawners:** Mob spawner cages spawn every 1–3 seconds with an entity cap increased to 16, supercharging player mob farm designs.
* **Protected Mobs:** Ghasts, Piglin Brutes, ordinary wolves, and jockeys retain 100% vanilla behavior.

---

### 🐺 4. Dire Wolf Companion & Mount
* **Majestic Mount:** 100 HP, 20 attack damage (5x normal wolf), 2.0x model scale, and 1.5x movement speed.
* **Visual Variants:** Pale, Ashen Black, and Woods coats with sinister glowing red eyes when wild and loyal natural eyes once tamed.
* **4-Step Taming Ritual:**
  1. Inflict **Splash Potion of Weakness**.
  2. Feed **Bone** (progresses to step 1).
  3. Feed **Cooked Steak** (progresses to step 2).
  4. Feed **Golden Carrot** (25% chance of success; failure allows immediate retrying without resetting ritual).
* **Mounting:** Equip with Wolf Armor to ride and steer as an agile, terrain-leaping companion.

---

### 💰 5. Economy & Durability Balancing
* **Viable Gold Equipment:** Golden tools and armor receive substantial durability buffs, giving gold gear a distinct high-speed, enchantable niche in gameplay.
* **Villager Gold Trades:** Armorer, Toolsmith, and Weaponsmith villagers offer enchanted golden equipment in exchange for gold ingots.
* **Mid-Barter Piglin Drop Safety:** Piglins drop gold nuggets upon death rather than raw ingots.

---

## 🛠️ Installation

### Requirements
- **Minecraft:** 1.21.1 or 1.21.11
- **Fabric Loader:** `>= 0.16.0`
- **Fabric API:** `>= 0.100.0`
- **Java:** `21` or newer

### Client & Server Setup
1. Download the latest `vanilla-reforged-1.0.0.jar` from [Releases](https://github.com/Himanshub0/Vanilla_Reforged/releases).
2. Place the `.jar` inside your `.minecraft/mods` folder (or your server's `mods` folder).
3. Ensure **Fabric API** is also present in your `mods` folder.
4. Launch Minecraft!

---

## 💻 Building from Source

To compile and build Vanilla Reforged yourself:

```bash
git clone https://github.com/Himanshub0/Vanilla_Reforged.git
cd Vanilla_Reforged
./gradlew build
```

The resulting mod jar will be generated at:
`build/libs/vanilla-reforged-1.0.0.jar`

To run the automated test suite:
```bash
./gradlew test
```

---

## 📜 License
Vanilla Reforged is licensed under the [MIT License](LICENSE).
