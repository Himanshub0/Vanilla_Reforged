package com.vanillareforged.client;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Language;

import java.util.*;

/**
 * Client tooltip handler that renders small, sweet, and square descriptions for enchanted books
 * and enchanted gear. Displays supported target equipment ("Fits: Swords, Axes") on books
 * and neatly word-wrapped descriptions (~32 characters wide) forming an elegant square box.
 */
public final class EnchantmentTooltipHandler {

    /**
     * Target equipment / tools that each enchantment can be applied to.
     */
    public static final Map<String, String> ENCHANTMENT_TARGETS = new LinkedHashMap<>();

    /**
     * Built-in concise, sweet descriptions for every enchantment in the mod and vanilla.
     */
    public static final Map<String, String> ENCHANTMENT_DESCRIPTIONS = new LinkedHashMap<>();

    static {
        // =========================================================================
        // TARGET TOOLS & EQUIPMENT MAPPING
        // =========================================================================
        // --- 4 Curses ---
        ENCHANTMENT_TARGETS.put("curse_of_breaking", "All Gear");
        ENCHANTMENT_TARGETS.put("curse_of_clumsiness", "Tools, Weapons");
        ENCHANTMENT_TARGETS.put("curse_of_displacement", "Weapons");
        ENCHANTMENT_TARGETS.put("curse_of_double_edge", "Weapons");
        ENCHANTMENT_TARGETS.put("binding_curse", "Wearable Gear");
        ENCHANTMENT_TARGETS.put("curse_of_binding", "Wearable Gear");
        ENCHANTMENT_TARGETS.put("vanishing_curse", "All Gear");
        ENCHANTMENT_TARGETS.put("curse_of_vanishing", "All Gear");

        // --- Phase 2 Combat Enchantments ---
        ENCHANTMENT_TARGETS.put("breeze_burst", "Bows");
        ENCHANTMENT_TARGETS.put("ice_aspect", "Swords");
        ENCHANTMENT_TARGETS.put("outreach", "Swords");
        ENCHANTMENT_TARGETS.put("swift_strike", "Swords, Axes");
        ENCHANTMENT_TARGETS.put("toxic", "Bows, Crossbows");
        ENCHANTMENT_TARGETS.put("precision", "Bows, Crossbows");
        ENCHANTMENT_TARGETS.put("websnare", "Swords");

        // --- Phase 2 Mining & Utility Enchantments ---
        ENCHANTMENT_TARGETS.put("crabs_touch", "Mining Tools, Shears");
        ENCHANTMENT_TARGETS.put("gluttony", "Tools, Swords, Shears");
        ENCHANTMENT_TARGETS.put("pyrolysis", "Pickaxes");

        // --- Phase 2 Armor & Survival Enchantments ---
        ENCHANTMENT_TARGETS.put("kinetic_protection", "Helmets");
        ENCHANTMENT_TARGETS.put("luminosity", "Chestplates");
        ENCHANTMENT_TARGETS.put("retrieval", "Chestplates");
        ENCHANTMENT_TARGETS.put("scorch_walker", "Boots");
        ENCHANTMENT_TARGETS.put("skyguard", "Elytra");
        ENCHANTMENT_TARGETS.put("stride", "Leggings");
        ENCHANTMENT_TARGETS.put("vitality", "Chestplates");

        // --- Phase 2 Elytra & Movement ---
        ENCHANTMENT_TARGETS.put("graviole", "Elytra");

        // --- Vanilla Armor Enchantments ---
        ENCHANTMENT_TARGETS.put("protection", "All Armor");
        ENCHANTMENT_TARGETS.put("fire_protection", "All Armor");
        ENCHANTMENT_TARGETS.put("feather_falling", "Boots");
        ENCHANTMENT_TARGETS.put("blast_protection", "All Armor");
        ENCHANTMENT_TARGETS.put("projectile_protection", "All Armor");
        ENCHANTMENT_TARGETS.put("respiration", "Helmets");
        ENCHANTMENT_TARGETS.put("aqua_affinity", "Helmets");
        ENCHANTMENT_TARGETS.put("thorns", "All Armor");
        ENCHANTMENT_TARGETS.put("depth_strider", "Boots");
        ENCHANTMENT_TARGETS.put("frost_walker", "Boots");
        ENCHANTMENT_TARGETS.put("soul_speed", "Boots");
        ENCHANTMENT_TARGETS.put("swift_sneak", "Leggings");

        // --- Vanilla Weapons & Combat ---
        ENCHANTMENT_TARGETS.put("sharpness", "Swords, Axes, Tridents");
        ENCHANTMENT_TARGETS.put("smite", "Swords, Axes, Maces");
        ENCHANTMENT_TARGETS.put("bane_of_arthropods", "Swords, Axes, Maces");
        ENCHANTMENT_TARGETS.put("knockback", "Swords");
        ENCHANTMENT_TARGETS.put("fire_aspect", "Swords");
        ENCHANTMENT_TARGETS.put("looting", "Swords");
        ENCHANTMENT_TARGETS.put("sweeping_edge", "Swords");

        // --- Vanilla Tools Enchantments ---
        ENCHANTMENT_TARGETS.put("efficiency", "Mining Tools");
        ENCHANTMENT_TARGETS.put("silk_touch", "Tools & Swords");
        ENCHANTMENT_TARGETS.put("unbreaking", "Any Gear");
        ENCHANTMENT_TARGETS.put("fortune", "Mining Tools");

        // --- Vanilla Bow & Crossbow ---
        ENCHANTMENT_TARGETS.put("power", "Bows, Crossbows");
        ENCHANTMENT_TARGETS.put("punch", "Bows");
        ENCHANTMENT_TARGETS.put("flame", "Bows");
        ENCHANTMENT_TARGETS.put("infinity", "Bows");
        ENCHANTMENT_TARGETS.put("multishot", "Crossbows");
        ENCHANTMENT_TARGETS.put("quick_charge", "Crossbows");
        ENCHANTMENT_TARGETS.put("piercing", "Crossbows");

        // --- Vanilla Trident ---
        ENCHANTMENT_TARGETS.put("loyalty", "Tridents");
        ENCHANTMENT_TARGETS.put("impaling", "Tridents");
        ENCHANTMENT_TARGETS.put("riptide", "Tridents");
        ENCHANTMENT_TARGETS.put("channeling", "Tridents");

        // --- Vanilla Fishing & Utility ---
        ENCHANTMENT_TARGETS.put("luck_of_the_sea", "Fishing Rods");
        ENCHANTMENT_TARGETS.put("lure", "Fishing Rods");
        ENCHANTMENT_TARGETS.put("mending", "Any Gear");

        // --- Vanilla Mace (1.21) ---
        ENCHANTMENT_TARGETS.put("density", "Maces");
        ENCHANTMENT_TARGETS.put("breach", "Maces");
        ENCHANTMENT_TARGETS.put("wind_burst", "Maces");

        // =========================================================================
        // SWEET & CONCISE DESCRIPTIONS (Wrapped to ~32 chars for square tooltips)
        // =========================================================================
        // --- 4 Curses ---
        ENCHANTMENT_DESCRIPTIONS.put("curse_of_breaking", "Gear durability depletes 4x faster during use.");
        ENCHANTMENT_DESCRIPTIONS.put("curse_of_clumsiness", "15% chance to drop held item when mining or attacking.");
        ENCHANTMENT_DESCRIPTIONS.put("curse_of_displacement", "Attacks may teleport enemies behind you.");
        ENCHANTMENT_DESCRIPTIONS.put("curse_of_double_edge", "40% recoil damage chance on attack.");
        ENCHANTMENT_DESCRIPTIONS.put("binding_curse", "Prevents item removal once equipped.");
        ENCHANTMENT_DESCRIPTIONS.put("curse_of_binding", "Prevents item removal once equipped.");
        ENCHANTMENT_DESCRIPTIONS.put("vanishing_curse", "Item vanishes permanently upon death.");
        ENCHANTMENT_DESCRIPTIONS.put("curse_of_vanishing", "Item vanishes permanently upon death.");

        // --- Phase 2 Combat Enchantments ---
        ENCHANTMENT_DESCRIPTIONS.put("breeze_burst", "Arrows blast wind charges on block impact.");
        ENCHANTMENT_DESCRIPTIONS.put("ice_aspect", "Chance to freeze struck foes in ice for 3 seconds.");
        ENCHANTMENT_DESCRIPTIONS.put("outreach", "Extends melee attack reach (+0.5 blocks per level).");
        ENCHANTMENT_DESCRIPTIONS.put("swift_strike", "Increases weapon attack speed (+0.3 per level).");
        ENCHANTMENT_DESCRIPTIONS.put("toxic", "Arrows inflict 11s Poison (undead immune).");
        ENCHANTMENT_DESCRIPTIONS.put("precision", "Arrows fly horizontally with zero-gravity trajectory.");
        ENCHANTMENT_DESCRIPTIONS.put("websnare", "Chance to trap targets in cobwebs at their feet.");

        // --- Phase 2 Mining & Utility Enchantments ---
        ENCHANTMENT_DESCRIPTIONS.put("crabs_touch", "Extends block reach distance (+1 block per level).");
        ENCHANTMENT_DESCRIPTIONS.put("gluttony", "Mined drops and XP vacuum directly into inventory.");
        ENCHANTMENT_DESCRIPTIONS.put("pyrolysis", "Auto-smelts mined blocks into items instantly.");

        // --- Phase 2 Armor & Survival Enchantments ---
        ENCHANTMENT_DESCRIPTIONS.put("kinetic_protection", "Absorbs Elytra crash wall impact damage.");
        ENCHANTMENT_DESCRIPTIONS.put("luminosity", "Dynamically illuminates dark areas around the wearer.");
        ENCHANTMENT_DESCRIPTIONS.put("retrieval", "Chance to catch incoming arrows into inventory.");
        ENCHANTMENT_DESCRIPTIONS.put("scorch_walker", "Walk on lava as temporary magma (2.5s); heat immune.");
        ENCHANTMENT_DESCRIPTIONS.put("skyguard", "Reduces all damage while flying with Elytra.");
        ENCHANTMENT_DESCRIPTIONS.put("stride", "Walk up 1 full block without jumping.");
        ENCHANTMENT_DESCRIPTIONS.put("vitality", "Increases maximum health (+1 heart per level).");

        // --- Phase 2 Elytra & Movement ---
        ENCHANTMENT_DESCRIPTIONS.put("graviole", "Low-gravity gliding for extended flight.");

        // --- Vanilla Armor Enchantments ---
        ENCHANTMENT_DESCRIPTIONS.put("protection", "Reduces damage from all common sources.");
        ENCHANTMENT_DESCRIPTIONS.put("fire_protection", "Cuts fire damage; burns out faster.");
        ENCHANTMENT_DESCRIPTIONS.put("feather_falling", "Reduces fall and ender pearl damage.");
        ENCHANTMENT_DESCRIPTIONS.put("blast_protection", "Cuts explosion damage and knockback.");
        ENCHANTMENT_DESCRIPTIONS.put("projectile_protection", "Cuts damage from arrows and projectiles.");
        ENCHANTMENT_DESCRIPTIONS.put("respiration", "Extends underwater breathing and vision.");
        ENCHANTMENT_DESCRIPTIONS.put("aqua_affinity", "Increases underwater mining speed.");
        ENCHANTMENT_DESCRIPTIONS.put("thorns", "Chance to reflect damage back to attackers.");
        ENCHANTMENT_DESCRIPTIONS.put("depth_strider", "Increases underwater swimming speed.");
        ENCHANTMENT_DESCRIPTIONS.put("frost_walker", "Freezes water underfoot into ice.");
        ENCHANTMENT_DESCRIPTIONS.put("soul_speed", "Increases movement speed on soul sand.");
        ENCHANTMENT_DESCRIPTIONS.put("swift_sneak", "Increases sneaking movement speed.");

        // --- Vanilla Weapons Enchantments ---
        ENCHANTMENT_DESCRIPTIONS.put("sharpness", "Increases melee strike damage dealt to targets.");
        ENCHANTMENT_DESCRIPTIONS.put("smite", "Deals heavy bonus damage to undead mobs.");
        ENCHANTMENT_DESCRIPTIONS.put("bane_of_arthropods", "Deals extra damage and slows arthropods.");
        ENCHANTMENT_DESCRIPTIONS.put("knockback", "Knocks struck enemies backward.");
        ENCHANTMENT_DESCRIPTIONS.put("fire_aspect", "Ignites struck targets in flame.");
        ENCHANTMENT_DESCRIPTIONS.put("looting", "Boosts mob drops and rare loot chances.");
        ENCHANTMENT_DESCRIPTIONS.put("sweeping_edge", "Increases sweeping swing damage.");

        // --- Vanilla Tools Enchantments ---
        ENCHANTMENT_DESCRIPTIONS.put("efficiency", "Increases block mining speed.");
        ENCHANTMENT_DESCRIPTIONS.put("silk_touch", "Mines fragile blocks intact.");
        ENCHANTMENT_DESCRIPTIONS.put("unbreaking", "Chance to avoid losing durability.");
        ENCHANTMENT_DESCRIPTIONS.put("fortune", "Multiplies drops from mined ores.");

        // --- Vanilla Bow & Crossbow Enchantments ---
        ENCHANTMENT_DESCRIPTIONS.put("power", "Increases arrow strike damage.");
        ENCHANTMENT_DESCRIPTIONS.put("punch", "Knocks targets back with arrows.");
        ENCHANTMENT_DESCRIPTIONS.put("flame", "Ignites shot arrows in flame.");
        ENCHANTMENT_DESCRIPTIONS.put("infinity", "Shoots arrows without consuming ammo.");
        ENCHANTMENT_DESCRIPTIONS.put("multishot", "Shoots 3 projectiles for the cost of 1.");
        ENCHANTMENT_DESCRIPTIONS.put("quick_charge", "Decreases crossbow reload time.");
        ENCHANTMENT_DESCRIPTIONS.put("piercing", "Arrows pierce entities and can be recovered.");

        // --- Vanilla Trident Enchantments ---
        ENCHANTMENT_DESCRIPTIONS.put("loyalty", "Returns thrown trident to your hand.");
        ENCHANTMENT_DESCRIPTIONS.put("impaling", "Deals extra damage to aquatic mobs.");
        ENCHANTMENT_DESCRIPTIONS.put("riptide", "Launches you forward in rain or water.");
        ENCHANTMENT_DESCRIPTIONS.put("channeling", "Strikes lightning during thunderstorms.");

        // --- Vanilla Fishing & Utility ---
        ENCHANTMENT_DESCRIPTIONS.put("luck_of_the_sea", "Increases chance of valuable fishing catch.");
        ENCHANTMENT_DESCRIPTIONS.put("lure", "Decreases wait time for fish to bite.");
        ENCHANTMENT_DESCRIPTIONS.put("mending", "Repairs durability using experience orbs.");

        // --- Vanilla Mace (1.21) ---
        ENCHANTMENT_DESCRIPTIONS.put("density", "Increases smash damage based on fall height.");
        ENCHANTMENT_DESCRIPTIONS.put("breach", "Pierces target armor on smash attacks.");
        ENCHANTMENT_DESCRIPTIONS.put("wind_burst", "Launches you upward after a smash attack.");
    }

    private EnchantmentTooltipHandler() {
    }

    public static void register() {
        ItemTooltipCallback.EVENT.register((ItemStack stack, Item.TooltipContext context, TooltipType type, List<Text> lines) -> {
            appendEnchantmentDescriptions(stack, lines);
        });
    }

    /**
     * Appends sweet, square descriptive tooltips and target equipment for every enchantment.
     * On enchanted books, prepends a clean "Fits: [Items]" badge.
     *
     * @param stack The item stack being hovered over.
     * @param lines The tooltip text lines list.
     * @return true if any description was added, false otherwise.
     */
    public static boolean appendEnchantmentDescriptions(ItemStack stack, List<Text> lines) {
        if (stack == null || stack.isEmpty() || lines == null) {
            return false;
        }

        // Gather all enchantments from regular gear, stored enchantments, and helper
        Map<RegistryEntry<Enchantment>, Integer> allEnchants = new LinkedHashMap<>();

        ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchants != null) {
            for (var entry : enchants.getEnchantmentEntries()) {
                allEnchants.put(entry.getKey(), entry.getIntValue());
            }
        }

        ItemEnchantmentsComponent stored = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
        if (stored != null) {
            for (var entry : stored.getEnchantmentEntries()) {
                allEnchants.put(entry.getKey(), entry.getIntValue());
            }
        }

        ItemEnchantmentsComponent helperEnchants = EnchantmentHelper.getEnchantments(stack);
        if (helperEnchants != null && !helperEnchants.isEmpty()) {
            for (var entry : helperEnchants.getEnchantmentEntries()) {
                allEnchants.putIfAbsent(entry.getKey(), entry.getIntValue());
            }
        }

        if (allEnchants.isEmpty()) {
            return false;
        }

        boolean isEnchantedBook = stack.isOf(Items.ENCHANTED_BOOK) || (stored != null && !stored.isEmpty());

        // Sort enchantments by name length descending so compound names match before substrings
        List<Map.Entry<RegistryEntry<Enchantment>, Integer>> sortedEnchants = new ArrayList<>(allEnchants.entrySet());
        sortedEnchants.sort((a, b) -> {
            String nameA = a.getKey().value().description().getString();
            String nameB = b.getKey().value().description().getString();
            return Integer.compare(nameB.length(), nameA.length());
        });

        boolean anyAdded = false;
        Set<RegistryEntry<Enchantment>> processed = new HashSet<>();

        // 1. Search existing tooltip lines to insert descriptions right below matching enchantment lines
        for (int i = 0; i < lines.size(); i++) {
            Text line = lines.get(i);
            for (Map.Entry<RegistryEntry<Enchantment>, Integer> entry : sortedEnchants) {
                RegistryEntry<Enchantment> ench = entry.getKey();
                int level = entry.getValue();

                if (!processed.contains(ench) && matchesLine(line, ench, level)) {
                    // Check if already followed by our description to prevent duplicate stacking
                    if (i + 1 < lines.size() && isDescriptionLine(lines.get(i + 1))) {
                        processed.add(ench);
                        break;
                    }

                    List<Text> descLines = createDescriptionLines(ench, isEnchantedBook);
                    if (!descLines.isEmpty()) {
                        processed.add(ench);
                        lines.addAll(i + 1, descLines);
                        anyAdded = true;
                        i += descLines.size(); // advance past newly inserted description lines
                        break;
                    }
                }
            }
        }

        // 2. Fallback: If any enchantment wasn't located by line matching, append cleanly at bottom
        for (Map.Entry<RegistryEntry<Enchantment>, Integer> entry : sortedEnchants) {
            RegistryEntry<Enchantment> ench = entry.getKey();
            if (!processed.contains(ench)) {
                String path = getEnchantmentPath(ench);
                if (!hasDescriptionInList(lines, path)) {
                    List<Text> descLines = createDescriptionLines(ench, isEnchantedBook);
                    if (!descLines.isEmpty()) {
                        lines.addAll(descLines);
                        processed.add(ench);
                        anyAdded = true;
                    }
                }
            }
        }

        return anyAdded;
    }

    private static boolean isDescriptionLine(Text line) {
        String s = line.getString();
        return s.contains("▪") || s.contains("▫");
    }

    private static boolean hasDescriptionInList(List<Text> lines, String path) {
        String fallbackDesc = ENCHANTMENT_DESCRIPTIONS.get(path);
        for (Text line : lines) {
            String s = line.getString();
            if (s.contains("▪") || s.contains("▫")) {
                if (fallbackDesc != null && s.contains(fallbackDesc)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean matchesLine(Text line, RegistryEntry<Enchantment> ench, int level) {
        String lineStr = line.getString().replace('\u00A0', ' ').trim();
        if (lineStr.isEmpty()) {
            return false;
        }

        // Match against exact formatted name (e.g., "Sharpness V" or "Mending")
        Text expectedName = Enchantment.getName(ench, level);
        String expectedStr = expectedName.getString().replace('\u00A0', ' ').trim();
        if (lineStr.equalsIgnoreCase(expectedStr)) {
            return true;
        }

        // Match against translation key
        Text enchDesc = ench.value().description();
        if (enchDesc.getContent() instanceof TranslatableTextContent trans) {
            String translated = Language.getInstance().get(trans.getKey()).replace('\u00A0', ' ').trim();
            if (!translated.isEmpty() && (lineStr.equalsIgnoreCase(translated) || lineStr.toLowerCase().startsWith(translated.toLowerCase() + " "))) {
                return true;
            }
        }

        // Match against base display name
        String rawName = enchDesc.getString().replace('\u00A0', ' ').trim();
        return !rawName.isEmpty() && (lineStr.equalsIgnoreCase(rawName) || lineStr.toLowerCase().startsWith(rawName.toLowerCase() + " "));
    }

    public static String getEnchantmentPath(RegistryEntry<Enchantment> ench) {
        Optional<RegistryKey<Enchantment>> keyOpt = ench.getKey();
        if (keyOpt.isPresent()) {
            return keyOpt.get().getValue().getPath();
        }

        Text enchDesc = ench.value().description();
        if (enchDesc.getContent() instanceof TranslatableTextContent trans) {
            String transKey = trans.getKey();
            int lastDot = transKey.lastIndexOf('.');
            if (lastDot != -1 && lastDot < transKey.length() - 1) {
                return transKey.substring(lastDot + 1);
            }
        }

        return enchDesc.getString().toLowerCase().replace(' ', '_');
    }

    public static String getEnchantmentNamespace(RegistryEntry<Enchantment> ench) {
        Optional<RegistryKey<Enchantment>> keyOpt = ench.getKey();
        if (keyOpt.isPresent()) {
            return keyOpt.get().getValue().getNamespace();
        }

        Text enchDesc = ench.value().description();
        if (enchDesc.getContent() instanceof TranslatableTextContent trans) {
            String transKey = trans.getKey();
            if (transKey.startsWith("enchantment.vanilla_reforged.")) {
                return "vanilla_reforged";
            }
        }

        return "minecraft";
    }

    /**
     * Resolves the target equipment/tools for an enchantment.
     */
    public static String getEnchantmentTargets(RegistryEntry<Enchantment> ench) {
        String path = getEnchantmentPath(ench);
        String mapped = ENCHANTMENT_TARGETS.get(path);
        if (mapped != null) {
            return mapped;
        }
        mapped = ENCHANTMENT_TARGETS.get(path.toLowerCase());
        if (mapped != null) {
            return mapped;
        }

        // Dynamic inspection fallback using sample items
        try {
            boolean sword = isItemSupported(ench, Items.DIAMOND_SWORD);
            boolean axe = isItemSupported(ench, Items.DIAMOND_AXE);
            boolean pick = isItemSupported(ench, Items.DIAMOND_PICKAXE);
            boolean shovel = isItemSupported(ench, Items.DIAMOND_SHOVEL);
            boolean bow = isItemSupported(ench, Items.BOW);
            boolean xbow = isItemSupported(ench, Items.CROSSBOW);
            boolean trident = isItemSupported(ench, Items.TRIDENT);
            boolean mace = isItemSupported(ench, Items.MACE);
            boolean helmet = isItemSupported(ench, Items.DIAMOND_HELMET);
            boolean chest = isItemSupported(ench, Items.DIAMOND_CHESTPLATE);
            boolean legs = isItemSupported(ench, Items.DIAMOND_LEGGINGS);
            boolean boots = isItemSupported(ench, Items.DIAMOND_BOOTS);
            boolean elytra = isItemSupported(ench, Items.ELYTRA);
            boolean rod = isItemSupported(ench, Items.FISHING_ROD);

            boolean allArmor = helmet && chest && legs && boots;
            boolean allTools = pick && shovel && axe;

            if (allArmor && allTools && sword) return "Any Gear";
            if (allArmor) return "All Armor";
            if (allTools && sword) return "Tools & Swords";
            if (allTools) return "Mining Tools";
            if (sword && axe && trident) return "Swords, Axes, Tridents";
            if (sword && axe) return "Swords, Axes";
            if (sword) return "Swords";
            if (bow && xbow) return "Bows, Crossbows";
            if (bow) return "Bows";
            if (xbow) return "Crossbows";
            if (trident) return "Tridents";
            if (mace) return "Maces";
            if (elytra) return "Elytra";
            if (helmet) return "Helmets";
            if (chest) return "Chestplates";
            if (legs) return "Leggings";
            if (boots) return "Boots";
            if (rod) return "Fishing Rods";
        } catch (Exception ignored) {
        }

        return "Tools / Armor";
    }

    private static boolean isItemSupported(RegistryEntry<Enchantment> ench, Item item) {
        return ench.value().isSupportedItem(new ItemStack(item));
    }

    /**
     * Splits text into short, balanced lines (~32 characters max) so tooltips form an
     * elegant square shape rather than sprawling across the entire screen horizontally.
     */
    public static List<String> wrapText(String text, int maxLineLength) {
        List<String> result = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return result;
        }

        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            if (current.length() == 0) {
                current.append(word);
            } else if (current.length() + 1 + word.length() <= maxLineLength) {
                current.append(" ").append(word);
            } else {
                result.add(current.toString());
                current = new StringBuilder(word);
            }
        }

        if (current.length() > 0) {
            result.add(current.toString());
        }

        return result;
    }

    /**
     * Creates clean, small, sweet, and square tooltip lines for an enchantment.
     */
    public static List<Text> createDescriptionLines(RegistryEntry<Enchantment> ench, boolean isEnchantedBook) {
        List<Text> lines = new ArrayList<>();
        String path = getEnchantmentPath(ench);
        String namespace = getEnchantmentNamespace(ench);
        Language lang = Language.getInstance();

        boolean isCurse = ench.isIn(EnchantmentTags.CURSE)
            || path.contains("curse")
            || path.contains("binding")
            || path.contains("vanishing")
            || ench.value().description().getString().toLowerCase().contains("curse");

        // 1. Target Tools / Applicable Items line (on Enchanted Books)
        if (isEnchantedBook) {
            String targets = getEnchantmentTargets(ench);
            MutableText targetLine = Text.literal("  ▫ ").formatted(isCurse ? Formatting.DARK_RED : Formatting.DARK_AQUA)
                .append(Text.literal("Fits: ").formatted(Formatting.DARK_GRAY))
                .append(Text.literal(targets).formatted(isCurse ? Formatting.RED : Formatting.YELLOW));
            lines.add(targetLine);
        }

        // 2. Short, sweet description (word-wrapped to ~32 chars forming a square box)
        String rawDesc = null;
        String descKey = "enchantment." + namespace + "." + path + ".desc";

        if (lang.hasTranslation(descKey)) {
            rawDesc = lang.get(descKey);
        } else if (path.equals("binding_curse") && lang.hasTranslation("enchantment.minecraft.curse_of_binding.desc")) {
            rawDesc = lang.get("enchantment.minecraft.curse_of_binding.desc");
        } else if (path.equals("vanishing_curse") && lang.hasTranslation("enchantment.minecraft.curse_of_vanishing.desc")) {
            rawDesc = lang.get("enchantment.minecraft.curse_of_vanishing.desc");
        } else {
            rawDesc = ENCHANTMENT_DESCRIPTIONS.get(path);
            if (rawDesc == null) {
                rawDesc = ENCHANTMENT_DESCRIPTIONS.get(path.toLowerCase());
            }
        }

        if (rawDesc != null && !rawDesc.isEmpty()) {
            List<String> wrapped = wrapText(rawDesc, 32);
            for (int i = 0; i < wrapped.size(); i++) {
                String lineStr = wrapped.get(i);
                MutableText textLine;
                if (i == 0) {
                    textLine = Text.literal("  ▪ ").formatted(isCurse ? Formatting.RED : Formatting.AQUA)
                        .append(Text.literal(lineStr).formatted(isCurse ? Formatting.RED : Formatting.GRAY));
                } else {
                    textLine = Text.literal("    ").formatted(Formatting.GRAY)
                        .append(Text.literal(lineStr).formatted(isCurse ? Formatting.RED : Formatting.GRAY));
                }
                lines.add(textLine);
            }
        }

        return lines;
    }
}
