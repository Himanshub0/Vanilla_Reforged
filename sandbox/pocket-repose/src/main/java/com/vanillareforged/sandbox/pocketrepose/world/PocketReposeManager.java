package com.vanillareforged.sandbox.pocketrepose.world;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.vanillareforged.sandbox.pocketrepose.PocketReposeMod;
import com.vanillareforged.sandbox.pocketrepose.block.ModBlocks;
import com.vanillareforged.sandbox.pocketrepose.block.SuitcaseBlock;
import com.vanillareforged.sandbox.pocketrepose.block.entity.SuitcaseBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the pocket dimensions, sector coordinate allocations, and player entrance/exit locations.
 */
public class PocketReposeManager {

    public static final Identifier POCKET_DIMENSION_ID = Identifier.of("pocket_repose", "pocket_dimension");
    public static final RegistryKey<World> POCKET_WORLD_KEY = RegistryKey.of(RegistryKeys.WORLD, POCKET_DIMENSION_ID);

    public static final long TELEPORT_COOLDOWN_MS = 2500L;
    public static final Map<UUID, Long> COOLDOWN_MAP = new ConcurrentHashMap<>();

    public static void setCooldown(UUID uuid) {
        COOLDOWN_MAP.put(uuid, System.currentTimeMillis());
    }

    public static boolean hasCooldown(UUID uuid) {
        return (System.currentTimeMillis() - COOLDOWN_MAP.getOrDefault(uuid, 0L)) < TELEPORT_COOLDOWN_MS;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Registry caches:
    // Keystone name -> Sector index
    private static final Map<String, Integer> SECTOR_MAP = new ConcurrentHashMap<>();
    // Keystone name -> Suitcase position
    private static final Map<String, SuitcaseData> SUITCASE_MAP = new ConcurrentHashMap<>();
    // Player UUID -> Exit data
    private static final Map<String, PlayerExitData> PLAYER_EXIT_MAP = new ConcurrentHashMap<>();

    private static int nextSectorIndex = 0;

    public static class SuitcaseData {
        public String worldKey;
        public int x, y, z;

        public SuitcaseData() {}

        public SuitcaseData(String worldKey, BlockPos pos) {
            this.worldKey = worldKey;
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
        }

        public BlockPos toBlockPos() {
            return new BlockPos(x, y, z);
        }
    }

    public static class PlayerExitData {
        public String worldKey;
        public double x, y, z;
        public float yaw, pitch;
        public String keystoneName;

        public PlayerExitData() {}

        public PlayerExitData(String worldKey, double x, double y, double z, float yaw, float pitch, String keystoneName) {
            this.worldKey = worldKey;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.keystoneName = keystoneName;
        }
    }

    public static boolean isPocketDimension(World world) {
        return world.getRegistryKey().equals(POCKET_WORLD_KEY);
    }

    public static String sanitizeKeystoneName(String raw) {
        if (raw == null) return "";
        String sanitized = raw.trim().toLowerCase().replaceAll("[^a-z0-9_]", "_");
        while (sanitized.contains("__")) {
            sanitized = sanitized.replace("__", "_");
        }
        while (sanitized.startsWith("_")) {
            sanitized = sanitized.substring(1);
        }
        while (sanitized.endsWith("_")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
        }
        return sanitized;
    }

    public static void registerSuitcase(String keystoneName, RegistryKey<World> worldKey, BlockPos pos) {
        if (keystoneName == null || keystoneName.isEmpty()) return;
        SUITCASE_MAP.put(keystoneName, new SuitcaseData(worldKey.getValue().toString(), pos));
    }

    public static int getOrCreateSector(String keystoneName) {
        return SECTOR_MAP.computeIfAbsent(keystoneName, k -> nextSectorIndex++);
    }

    public static BlockPos getSectorCenter(int sectorIndex) {
        return new BlockPos(sectorIndex * 1024, 64, 0);
    }

    /**
     * Teleports a player into their cozy pocket dimension sector.
     */
    public static void enterPocketDimension(ServerPlayerEntity player, BlockPos suitcasePos, SuitcaseBlockEntity suitcase) {
        MinecraftServer server = player.getEntityWorld().getServer();
        if (server == null) return;

        ServerWorld pocketWorld = server.getWorld(POCKET_WORLD_KEY);
        if (pocketWorld == null) {
            player.sendMessage(Text.literal("§c[Pocket Repose] Pocket dimension is not loaded!"), true);
            return;
        }

        String keystoneName = suitcase.getBoundKeystoneName();
        if (keystoneName.isEmpty()) return;

        // 1. Save player's Overworld return location
        RegistryKey<World> currentWorldKey = player.getEntityWorld().getRegistryKey();
        PLAYER_EXIT_MAP.put(player.getUuidAsString(), new PlayerExitData(
            currentWorldKey.getValue().toString(),
            suitcasePos.getX() + 0.5,
            suitcasePos.getY() + 0.5,
            suitcasePos.getZ() + 0.5,
            player.getYaw(),
            player.getPitch(),
            keystoneName
        ));

        // 2. Register suitcase position
        registerSuitcase(keystoneName, currentWorldKey, suitcasePos);
        suitcase.addEnteredPlayer(player.getUuid());

        // 3. Allocate / get sector
        boolean isNewSector = !SECTOR_MAP.containsKey(keystoneName);
        int sectorIndex = getOrCreateSector(keystoneName);
        BlockPos center = getSectorCenter(sectorIndex);

        // 4. Generate sector cozy structure if first time
        if (isNewSector) {
            generatePocketSector(server, pocketWorld, center);
        }

        // 5. Teleport player into pocket dimension at safe spawn point (offset from portal)
        setCooldown(player.getUuid());
        Vec3d dest = new Vec3d(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 2.5);
        player.stopRiding();
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0f;
        TeleportTarget tp = new TeleportTarget(pocketWorld, dest, Vec3d.ZERO, 0f, 0f, TeleportTarget.NO_OP);
        player.teleportTo(tp);

        // 6. Sound & feedback
        player.getEntityWorld().playSound(null, dest.x, dest.y, dest.z, SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.PLAYERS, 2.0f, 1.0f);
        player.sendMessage(Text.literal("§d[Pocket Repose] Welcome to your pocket dimension: §e" + keystoneName), true);

        saveState(server);
    }

    public static final double VOID_FALL_ESCAPE_Y = 20.0;

    /**
     * Ticks the pocket dimension world to detect players falling into the void.
     * When a player falls below Y=20 (well below the island at Y=64 and before taking void damage),
     * they are smoothly teleported back to their Overworld suitcase, allowing void-jumping as an escape method!
     */
    public static void tickPocketWorld(ServerWorld world) {
        if (!world.getRegistryKey().equals(POCKET_WORLD_KEY)) return;
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.getY() < VOID_FALL_ESCAPE_Y) {
                if (!hasCooldown(player.getUuid())) {
                    player.sendMessage(Text.literal("§e[Pocket Repose] You fell out of the pocket realm and landed safely by your suitcase!"), true);
                    exitPocketDimension(player);
                }
            }
        }
    }

    /**
     * Teleports a player back out of the pocket dimension to their Overworld suitcase.
     */
    public static void exitPocketDimension(ServerPlayerEntity player) {
        setCooldown(player.getUuid());
        MinecraftServer server = player.getEntityWorld().getServer();
        if (server == null) return;

        String uuid = player.getUuidAsString();
        PlayerExitData exitData = PLAYER_EXIT_MAP.get(uuid);

        ServerWorld targetWorld = server.getWorld(World.OVERWORLD);
        Vec3d exitPos = null;
        float yaw = player.getYaw();
        float pitch = player.getPitch();

        if (exitData != null) {
            Identifier worldId = Identifier.tryParse(exitData.worldKey);
            if (worldId != null) {
                ServerWorld savedWorld = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, worldId));
                if (savedWorld != null) {
                    targetWorld = savedWorld;
                }
            }

            SuitcaseData sd = SUITCASE_MAP.get(exitData.keystoneName);
            boolean foundSuitcase = false;

            // 1. Look up if the suitcase block still exists in the target world
            if (sd != null) {
                BlockPos scPos = sd.toBlockPos();
                BlockState state = targetWorld.getBlockState(scPos);
                if (state.getBlock() instanceof SuitcaseBlock) {
                    Direction dir = state.contains(SuitcaseBlock.FACING) ? state.get(SuitcaseBlock.FACING) : Direction.NORTH;
                    exitPos = new Vec3d(
                        scPos.getX() + 0.5 + dir.getOffsetX() * 0.8,
                        scPos.getY() + 0.1,
                        scPos.getZ() + 0.5 + dir.getOffsetZ() * 0.8
                    );
                    yaw = switch (dir) {
                        case SOUTH -> 0.0f;
                        case WEST -> 90.0f;
                        case NORTH -> 180.0f;
                        case EAST -> -90.0f;
                        default -> exitData.yaw;
                    };
                    if (targetWorld.getBlockEntity(scPos) instanceof SuitcaseBlockEntity sbe) {
                        sbe.removeEnteredPlayer(player.getUuid());
                    }
                    foundSuitcase = true;
                }
            }

            // 2. If the suitcase is NOT placed in the world, scan all online players on the server
            if (!foundSuitcase) {
                for (ServerPlayerEntity onlinePlayer : server.getPlayerManager().getPlayerList()) {
                    if (onlinePlayer.getUuid().equals(player.getUuid())) {
                        continue;
                    }
                    if (hasBoundSuitcaseInInventory(onlinePlayer, exitData.keystoneName)) {
                        targetWorld = (ServerWorld) onlinePlayer.getEntityWorld();
                        Direction pFacing = onlinePlayer.getHorizontalFacing();
                        exitPos = new Vec3d(
                            onlinePlayer.getX() - pFacing.getOffsetX() * 0.8,
                            onlinePlayer.getY() + 0.1,
                            onlinePlayer.getZ() - pFacing.getOffsetZ() * 0.8
                        );
                        yaw = onlinePlayer.getYaw();
                        pitch = onlinePlayer.getPitch();
                        removeEnteredPlayerFromInventory(onlinePlayer, player.getUuid(), exitData.keystoneName);
                        foundSuitcase = true;
                        break;
                    }
                }
            }

            // 3. Check if suitcase is lying as an ItemEntity nearby
            if (!foundSuitcase && sd != null) {
                BlockPos scPos = sd.toBlockPos();
                Box searchBox = new Box(scPos).expand(8.0);
                List<ItemEntity> items = targetWorld.getEntitiesByClass(ItemEntity.class, searchBox, item -> isMatchingSuitcaseStack(item.getStack(), exitData.keystoneName));
                if (!items.isEmpty()) {
                    ItemEntity drop = items.get(0);
                    exitPos = new Vec3d(drop.getX(), drop.getY() + 0.1, drop.getZ());
                    removeEnteredPlayerFromItemStack(drop.getStack(), player.getUuid());
                    foundSuitcase = true;
                }
            }

            // 4. Fallback to saved coordinates
            if (exitPos == null) {
                exitPos = new Vec3d(exitData.x, exitData.y, exitData.z);
                yaw = exitData.yaw;
                pitch = exitData.pitch;
            }
        } else {
            BlockPos spawn = targetWorld.getSpawnPoint().getPos();
            exitPos = new Vec3d(spawn.getX() + 0.5, spawn.getY() + 1.0, spawn.getZ() + 0.5);
        }

        player.stopRiding();
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0f;
        TeleportTarget tp = new TeleportTarget(targetWorld, exitPos, Vec3d.ZERO, yaw, pitch, TeleportTarget.NO_OP);
        player.teleportTo(tp);

        targetWorld.playSound(null, exitPos.x, exitPos.y, exitPos.z, SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.PLAYERS, 2.0f, 1.0f);
        targetWorld.playSound(null, exitPos.x, exitPos.y, exitPos.z, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.PLAYERS, 0.5f, 1.2f);
        player.sendMessage(Text.literal("§a[Pocket Repose] Exited back to your suitcase."), true);

        saveState(server);
    }

    public static boolean isMatchingSuitcaseStack(ItemStack stack, String keystoneName) {
        if (stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof BlockItem bi) || !(bi.getBlock() instanceof SuitcaseBlock)) {
            return false;
        }
        NbtComponent customData = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        if (customData.isEmpty()) return false;
        NbtCompound nbt = customData.copyNbt();
        if (nbt.contains("BoundKeystone")) {
            String bound = nbt.getString("BoundKeystone").orElse("");
            return bound.equals(keystoneName);
        }
        return false;
    }

    private static boolean hasBoundSuitcaseInInventory(ServerPlayerEntity player, String keystoneName) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack s = player.getInventory().getStack(i);
            if (isMatchingSuitcaseStack(s, keystoneName)) {
                return true;
            }
        }
        return false;
    }

    private static void removeEnteredPlayerFromInventory(ServerPlayerEntity carrier, UUID exitedUuid, String keystoneName) {
        for (int i = 0; i < carrier.getInventory().size(); i++) {
            ItemStack s = carrier.getInventory().getStack(i);
            if (isMatchingSuitcaseStack(s, keystoneName)) {
                removeEnteredPlayerFromItemStack(s, exitedUuid);
                carrier.getInventory().markDirty();
                return;
            }
        }
    }

    public static void removeEnteredPlayerFromItemStack(ItemStack stack, UUID exitedUuid) {
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> {
            if (nbt.contains("EnteredPlayers")) {
                String playersStr = nbt.getString("EnteredPlayers").orElse("");
                if (!playersStr.isEmpty()) {
                    List<String> list = new ArrayList<>(Arrays.asList(playersStr.split(",")));
                    list.remove(exitedUuid.toString());
                    if (list.isEmpty()) {
                        nbt.remove("EnteredPlayers");
                    } else {
                        nbt.putString("EnteredPlayers", String.join(",", list));
                    }
                }
            }
        });
    }

    /**
     * Vacuums a friendly mob into the pocket dimension.
     */
    public static boolean captureMob(LivingEntity mob, ServerPlayerEntity player, String keystoneName) {
        if (mob instanceof PlayerEntity || mob instanceof EnderDragonEntity || mob instanceof WitherEntity) {
            return false;
        }

        MinecraftServer server = player.getEntityWorld().getServer();
        if (server == null) return false;

        ServerWorld pocketWorld = server.getWorld(POCKET_WORLD_KEY);
        if (pocketWorld == null) return false;

        int sectorIndex = getOrCreateSector(keystoneName);
        BlockPos center = getSectorCenter(sectorIndex);

        if (!SECTOR_MAP.containsKey(keystoneName)) {
            generatePocketSector(server, pocketWorld, center);
        }

        Vec3d dest = new Vec3d(center.getX() + 1.5, center.getY() + 1.0, center.getZ() + 1.5);
        TeleportTarget tp = new TeleportTarget(pocketWorld, dest, Vec3d.ZERO, mob.getYaw(), mob.getPitch(), TeleportTarget.NO_OP);
        mob.teleportTo(tp);

        player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.PLAYERS, 2.0f, 1.2f);
        player.sendMessage(Text.literal("§d[Pocket Repose] Captured " + mob.getName().getString() + " into your suitcase pocket!"), true);
        return true;
    }

    /**
     * Generates the cozy structure (island or cabin) with exit portal at the sector center.
     */
    private static void generatePocketSector(MinecraftServer server, ServerWorld world, BlockPos center) {
        boolean placedStructure = false;
        try {
            Optional<StructureTemplate> templateOpt = server.getStructureTemplateManager()
                .getTemplate(Identifier.of("pocket_repose", "pocket_island_01"));

            if (templateOpt.isPresent()) {
                StructureTemplate template = templateOpt.get();
                BlockPos offsetPos = center.add(-8, -1, -8);
                template.place(world, offsetPos, offsetPos, new StructurePlacementData(), world.getRandom(), Block.NOTIFY_ALL);
                placedStructure = true;
                PocketReposeMod.LOGGER.info("Placed pocket_island_01 template at {}", center);
            }
        } catch (Exception e) {
            PocketReposeMod.LOGGER.warn("Could not place structure template pocket_island_01, generating procedural cozy room: {}", e.getMessage());
        }

        // Procedural cozy wooden cabin & guaranteed exit portal in center
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        // 7x7 Wooden cabin floor & walls if template was not used or to guarantee base
        if (!placedStructure) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    world.setBlockState(new BlockPos(cx + dx, cy, cz + dz), Blocks.SPRUCE_PLANKS.getDefaultState());
                    // Ceiling
                    world.setBlockState(new BlockPos(cx + dx, cy + 4, cz + dz), Blocks.SPRUCE_WOOD.getDefaultState());
                    // Clear air inside
                    for (int dy = 1; dy <= 3; dy++) {
                        world.setBlockState(new BlockPos(cx + dx, cy + dy, cz + dz), Blocks.AIR.getDefaultState());
                    }
                }
            }

            // Walls
            for (int dy = 1; dy <= 3; dy++) {
                for (int d = -3; d <= 3; d++) {
                    world.setBlockState(new BlockPos(cx + d, cy + dy, cz - 3), Blocks.DARK_OAK_PLANKS.getDefaultState());
                    world.setBlockState(new BlockPos(cx + d, cy + dy, cz + 3), Blocks.DARK_OAK_PLANKS.getDefaultState());
                    world.setBlockState(new BlockPos(cx - 3, cy + dy, cz + d), Blocks.DARK_OAK_PLANKS.getDefaultState());
                    world.setBlockState(new BlockPos(cx + 3, cy + dy, cz + d), Blocks.DARK_OAK_PLANKS.getDefaultState());
                }
            }

            // Cozy furniture
            world.setBlockState(new BlockPos(cx - 2, cy + 1, cz - 2), Blocks.RED_BED.getDefaultState());
            world.setBlockState(new BlockPos(cx + 2, cy + 1, cz - 2), Blocks.CRAFTING_TABLE.getDefaultState());
            world.setBlockState(new BlockPos(cx + 2, cy + 1, cz - 1), Blocks.CHEST.getDefaultState());
            world.setBlockState(new BlockPos(cx, cy + 3, cz), Blocks.LANTERN.getDefaultState());
        }

        // Place the Exit Portal Block at the center floor
        world.setBlockState(center, ModBlocks.PORTAL.getDefaultState());
        world.setBlockState(center.up(), Blocks.AIR.getDefaultState());
        world.setBlockState(center.up(2), Blocks.AIR.getDefaultState());
    }

    public static void loadState(MinecraftServer server) {
        Path path = server.getSavePath(WorldSavePath.ROOT).resolve("data/pocket_repose/registry.json");
        if (!Files.exists(path)) return;

        try (Reader reader = Files.newBufferedReader(path)) {
            Type type = new TypeToken<SavedDataHolder>() {}.getType();
            SavedDataHolder data = GSON.fromJson(reader, type);
            if (data != null) {
                if (data.sectors != null) SECTOR_MAP.putAll(data.sectors);
                if (data.suitcases != null) SUITCASE_MAP.putAll(data.suitcases);
                if (data.exits != null) PLAYER_EXIT_MAP.putAll(data.exits);
                nextSectorIndex = data.nextSectorIndex;
            }
        } catch (Exception e) {
            PocketReposeMod.LOGGER.error("Failed to load Pocket Repose registry", e);
        }
    }

    public static void saveState(MinecraftServer server) {
        Path path = server.getSavePath(WorldSavePath.ROOT).resolve("data/pocket_repose/registry.json");
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                SavedDataHolder data = new SavedDataHolder();
                data.sectors = new HashMap<>(SECTOR_MAP);
                data.suitcases = new HashMap<>(SUITCASE_MAP);
                data.exits = new HashMap<>(PLAYER_EXIT_MAP);
                data.nextSectorIndex = nextSectorIndex;
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            PocketReposeMod.LOGGER.error("Failed to save Pocket Repose registry", e);
        }
    }

    private static class SavedDataHolder {
        public Map<String, Integer> sectors;
        public Map<String, SuitcaseData> suitcases;
        public Map<String, PlayerExitData> exits;
        public int nextSectorIndex;
    }
}
