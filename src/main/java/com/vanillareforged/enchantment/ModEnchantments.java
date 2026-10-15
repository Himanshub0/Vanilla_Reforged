package com.vanillareforged.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class ModEnchantments {
    // Combat / Offensive Curses
    public static final RegistryKey<Enchantment> CURSE_OF_BREAKING = key("curse_of_breaking");
    public static final RegistryKey<Enchantment> CURSE_OF_CLUMSINESS = key("curse_of_clumsiness");
    public static final RegistryKey<Enchantment> CURSE_OF_DISPLACEMENT = key("curse_of_displacement");
    public static final RegistryKey<Enchantment> CURSE_OF_DOUBLE_EDGE = key("curse_of_double_edge");

    // Combat & Weapon Enchantments
    public static final RegistryKey<Enchantment> BREEZE_BURST = key("breeze_burst");
    public static final RegistryKey<Enchantment> ICE_ASPECT = key("ice_aspect");
    public static final RegistryKey<Enchantment> OUTREACH = key("outreach");
    public static final RegistryKey<Enchantment> SWIFT_STRIKE = key("swift_strike");
    public static final RegistryKey<Enchantment> TOXIC = key("toxic");
    public static final RegistryKey<Enchantment> WEBSNARE = key("websnare");
    public static final RegistryKey<Enchantment> PRECISION = key("precision");

    // Mining & Harvesting Enchantments
    public static final RegistryKey<Enchantment> CRABS_TOUCH = key("crabs_touch");
    public static final RegistryKey<Enchantment> GLUTTONY = key("gluttony");
    public static final RegistryKey<Enchantment> PYROLYSIS = key("pyrolysis");

    // Armor & Utility Enchantments
    public static final RegistryKey<Enchantment> KINETIC_PROTECTION = key("kinetic_protection");
    public static final RegistryKey<Enchantment> RETRIEVAL = key("retrieval");
    public static final RegistryKey<Enchantment> SCORCH_WALKER = key("scorch_walker");
    public static final RegistryKey<Enchantment> SKYGUARD = key("skyguard");
    public static final RegistryKey<Enchantment> STRIDE = key("stride");
    public static final RegistryKey<Enchantment> VITALITY = key("vitality");
    public static final RegistryKey<Enchantment> GRAVIOLE = key("graviole");
    public static final RegistryKey<Enchantment> LUMINOSITY = key("luminosity");

    private static RegistryKey<Enchantment> key(String name) {
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of("vanilla_reforged", name));
    }

    public static void initialize() {
        // Class loaded, static fields initialized
    }

    private ModEnchantments() {}
}
