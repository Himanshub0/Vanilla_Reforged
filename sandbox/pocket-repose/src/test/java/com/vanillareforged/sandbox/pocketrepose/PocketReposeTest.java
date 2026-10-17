package com.vanillareforged.sandbox.pocketrepose;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vanillareforged.sandbox.pocketrepose.item.KeystoneItem;
import com.vanillareforged.sandbox.pocketrepose.world.PocketReposeManager;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class PocketReposeTest {

    private static final Gson GSON = new Gson();

    @Test
    void testSectorCenterCoordinateCalculation() {
        BlockPos sector0 = PocketReposeManager.getSectorCenter(0);
        assertEquals(0, sector0.getX());
        assertEquals(64, sector0.getY());
        assertEquals(0, sector0.getZ());

        BlockPos sector1 = PocketReposeManager.getSectorCenter(1);
        assertEquals(1024, sector1.getX());
        assertEquals(64, sector1.getY());
        assertEquals(0, sector1.getZ());

        BlockPos sector2 = PocketReposeManager.getSectorCenter(2);
        assertEquals(2048, sector2.getX());
        assertEquals(64, sector2.getY());
        assertEquals(0, sector2.getZ());

        BlockPos sector50 = PocketReposeManager.getSectorCenter(50);
        assertEquals(51200, sector50.getX());
        assertEquals(64, sector50.getY());
        assertEquals(0, sector50.getZ());

        // Isolation distance verification: sectors must be separated by >= 1024 blocks
        assertTrue(sector1.getX() - sector0.getX() >= 1024);
        assertTrue(sector2.getX() - sector1.getX() >= 1024);
    }

    @Test
    void testKeystoneSanitization() {
        assertEquals("cozy_island", PocketReposeManager.sanitizeKeystoneName("Cozy Island"));
        assertEquals("my_secret_haven", PocketReposeManager.sanitizeKeystoneName("  My Secret Haven  "));
        assertEquals("vault_99", PocketReposeManager.sanitizeKeystoneName("Vault #99!"));
        assertEquals("test_room", PocketReposeManager.sanitizeKeystoneName("test---room"));
        assertEquals("", PocketReposeManager.sanitizeKeystoneName(""));
        assertEquals("", PocketReposeManager.sanitizeKeystoneName(null));
        assertEquals("", PocketReposeManager.sanitizeKeystoneName("   "));
    }

    @Test
    void testSuitcaseRecipeIntegrity() {
        InputStream stream = getClass().getResourceAsStream("/data/pocket_repose/recipe/suitcase.json");
        assertNotNull(stream, "Suitcase recipe JSON must exist on classpath");

        JsonObject json = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
        assertEquals("minecraft:crafting_shaped", json.get("type").getAsString());

        JsonObject key = json.getAsJsonObject("key");
        assertTrue(key.has("G"), "Must require Gold");
        assertTrue(key.has("C"), "Must require Ender Chest");
        assertTrue(key.has("L"), "Must require Leather");

        JsonObject result = json.getAsJsonObject("result");
        assertEquals("pocket_repose:suitcase", result.get("id").getAsString());
        assertEquals(1, result.get("count").getAsInt());
    }

    @Test
    void testKeystoneRecipeIntegrity() {
        InputStream stream = getClass().getResourceAsStream("/data/pocket_repose/recipe/keystone.json");
        assertNotNull(stream, "Keystone recipe JSON must exist on classpath");

        JsonObject json = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
        assertEquals("minecraft:crafting_shaped", json.get("type").getAsString());

        JsonObject key = json.getAsJsonObject("key");
        assertTrue(key.has("G"), "Must require Gold");
        assertTrue(key.has("N"), "Must require Netherite Scrap");
        assertTrue(key.has("T"), "Must require Tripwire Hook");

        JsonObject result = json.getAsJsonObject("result");
        assertEquals("pocket_repose:keystone", result.get("id").getAsString());
        assertEquals(1, result.get("count").getAsInt());
    }

    @Test
    void testDimensionConfigurationIntegrity() {
        InputStream stream = getClass().getResourceAsStream("/data/pocket_repose/dimension/pocket_dimension.json");
        assertNotNull(stream, "Pocket dimension JSON must exist on classpath");

        JsonObject json = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
        assertEquals("pocket_repose:pocket_dimension_type", json.get("type").getAsString());

        JsonObject generator = json.getAsJsonObject("generator");
        assertEquals("minecraft:flat", generator.get("type").getAsString());
        JsonObject settings = generator.getAsJsonObject("settings");
        assertEquals("minecraft:mushroom_fields", settings.get("biome").getAsString(),
            "Biome must be mushroom fields to naturally prevent hostile monster spawns");
    }

    @Test
    void testEnglishTranslationIntegrity() {
        InputStream stream = getClass().getResourceAsStream("/assets/pocket_repose/lang/en_us.json");
        assertNotNull(stream, "en_us.json translation must exist on classpath");

        JsonObject json = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
        assertTrue(json.has("block.pocket_repose.suitcase"));
        assertTrue(json.has("block.pocket_repose.portal"));
        assertTrue(json.has("item.pocket_repose.keystone"));
        assertTrue(json.has("itemgroup.pocket_repose"));
        assertTrue(json.has("tooltip.pocket_repose.suitcase"));
        assertTrue(json.has("tooltip.pocket_repose.keystone"));
    }

    @Test
    void testStructureTemplateFileIntegrity() {
        InputStream stream = getClass().getResourceAsStream("/data/pocket_repose/structure/pocket_island_01.nbt");
        assertNotNull(stream, "Pocket island NBT structure template must exist on classpath");
    }

    @Test
    void testTeleportCooldownLogic() {
        java.util.UUID testPlayer = java.util.UUID.randomUUID();
        assertFalse(PocketReposeManager.hasCooldown(testPlayer), "Player should initially have no cooldown");

        PocketReposeManager.setCooldown(testPlayer);
        assertTrue(PocketReposeManager.hasCooldown(testPlayer), "Player should have cooldown right after being set");

        // Simulate expired cooldown
        PocketReposeManager.COOLDOWN_MAP.put(testPlayer, System.currentTimeMillis() - 3000L);
        assertFalse(PocketReposeManager.hasCooldown(testPlayer), "Cooldown should expire after 2500ms");
    }

    @Test
    void testSafePocketSpawnCoordinates() {
        // Center at sector 0 is (0, 64, 0)
        BlockPos center = PocketReposeManager.getSectorCenter(0);
        // Player must spawn safely away from the portal (which is at center)
        BlockPos safeFloor = center.add(0, 1, 2);
        assertEquals(0, safeFloor.getX());
        assertEquals(65, safeFloor.getY());
        assertEquals(2, safeFloor.getZ());
    }

    @Test
    void testAllDyedSuitcaseRecipesIntegrity() {
        String[] colors = {
            "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
            "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
        };

        for (String color : colors) {
            InputStream stream = getClass().getResourceAsStream("/data/pocket_repose/recipe/" + color + "_suitcase.json");
            assertNotNull(stream, "Recipe for " + color + "_suitcase.json must exist");
            JsonObject json = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
            assertEquals("minecraft:crafting_shapeless", json.get("type").getAsString());
            JsonObject result = json.getAsJsonObject("result");
            assertEquals("pocket_repose:" + color + "_suitcase", result.get("id").getAsString());
        }
    }

    @Test
    void testSecretBarrelRecipeIntegrity() {
        InputStream stream = getClass().getResourceAsStream("/data/pocket_repose/recipe/secret_barrel.json");
        assertNotNull(stream, "Recipe for secret_barrel.json must exist");
        JsonObject json = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
        assertEquals("minecraft:crafting_shapeless", json.get("type").getAsString());
        JsonObject result = json.getAsJsonObject("result");
        assertEquals("pocket_repose:secret_barrel", result.get("id").getAsString());
    }

    @Test
    void testSuitcasesTagIntegrity() {
        InputStream stream = getClass().getResourceAsStream("/data/pocket_repose/tags/item/suitcases.json");
        assertNotNull(stream, "Tag suitcases.json must exist on classpath");
        JsonObject json = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
        assertTrue(json.has("values"));
        com.google.gson.JsonArray values = json.getAsJsonArray("values");
        assertTrue(values.contains(new com.google.gson.JsonPrimitive("pocket_repose:suitcase")));
        assertTrue(values.contains(new com.google.gson.JsonPrimitive("pocket_repose:white_suitcase")));
        assertTrue(values.contains(new com.google.gson.JsonPrimitive("pocket_repose:black_suitcase")));
        assertTrue(values.contains(new com.google.gson.JsonPrimitive("pocket_repose:secret_barrel")));
        assertEquals(18, values.size(), "Tag should contain all 18 suitcase variants");
    }

    @Test
    void testVoidFallEscapeThreshold() {
        assertEquals(20.0, PocketReposeManager.VOID_FALL_ESCAPE_Y, "Void fall escape threshold should be at Y=20.0");
        // Island generated at Y=64, so falling below Y=20 is safely in the void
        assertTrue(PocketReposeManager.VOID_FALL_ESCAPE_Y < 64.0, "Threshold must be below the island floor");
    }
}
