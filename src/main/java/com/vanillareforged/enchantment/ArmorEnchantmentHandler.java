package com.vanillareforged.enchantment;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LightBlock;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public final class ArmorEnchantmentHandler {

    private static final Identifier STRIDE_MODIFIER_ID = Identifier.of("vanilla_reforged", "enchantment.stride");
    private static final Identifier VITALITY_MODIFIER_ID = Identifier.of("vanilla_reforged", "enchantment.vitality");
    private static final Identifier GRAVIOLE_MODIFIER_ID = Identifier.of("vanilla_reforged", "enchantment.graviole");

    public static final long SCORCH_WALKER_DURATION_TICKS = 50L; // 2.5 seconds = 50 game ticks
    private static final Map<RegistryKey<World>, Map<BlockPos, Long>> TEMPORARY_MAGMA_BLOCKS = new ConcurrentHashMap<>();

    private static final Random RANDOM = new Random();

    public record ArmorEquippedState(
            ServerPlayerEntity player,
            ItemStack head,
            ItemStack chest,
            ItemStack legs,
            ItemStack feet,
            boolean isGliding,
            int scorchLevel
    ) {}
    private static final Map<UUID, ArmorEquippedState> PREVIOUS_ARMOR = new WeakHashMap<>();

    private static final Map<UUID, BlockPos> PLAYER_LIGHT_BLOCKS = new WeakHashMap<>();

    private ArmorEnchantmentHandler() {}

    public static void register() {
        // Clear equipment tracking on player disconnect and respawn
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            PREVIOUS_ARMOR.remove(handler.getPlayer().getUuid());
            BlockPos lightPos = PLAYER_LIGHT_BLOCKS.remove(handler.getPlayer().getUuid());
            removeLightBlock(handler.getPlayer().getEntityWorld(), lightPos);
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            PREVIOUS_ARMOR.remove(oldPlayer.getUuid());
            BlockPos lightPos = PLAYER_LIGHT_BLOCKS.remove(oldPlayer.getUuid());
            removeLightBlock(oldPlayer.getEntityWorld(), lightPos);
        });

        // Ticking player attributes on equipment change, environmental effects & temporary magma reverting
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickPlayerArmorEffects(player);
                handleLuminosity(player);
            }
            tickTemporaryMagma(server);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(ArmorEnchantmentHandler::cleanupAllTemporaryMagma);

        // ServerLivingEntityEvents.ALLOW_DAMAGE for damage cancellations or arrow retrieval
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayerEntity player)) {
                return true;
            }

            // Scorch Walker: Negate hot floor / magma block damage
            if (source.isOf(DamageTypes.HOT_FLOOR)) {
                int scorchWalkerLevel = EnchantmentHelper.getEquipmentLevel(ModEnchantments.SCORCH_WALKER, player, EquipmentSlot.FEET);
                if (scorchWalkerLevel > 0) {
                    return false; // Cancel damage completely
                }
            }

            // Retrieval: When hit by a projectile, chance to catch/add an arrow to inventory
            if (source.getSource() instanceof PersistentProjectileEntity arrow) {
                // Never steal player loyalty tridents
                if (arrow instanceof net.minecraft.entity.projectile.TridentEntity) {
                    return true;
                }
                int retrievalLevel = EnchantmentHelper.getEquipmentLevel(ModEnchantments.RETRIEVAL, player, EquipmentSlot.CHEST);
                if (retrievalLevel > 0) {
                    float chance = 0.20f * retrievalLevel; // 20% per level up to 80% at IV
                    if (RANDOM.nextFloat() < chance) {
                        if (arrow.pickupType == PersistentProjectileEntity.PickupPermission.ALLOWED) {
                            ItemStack arrowItem = arrow.getItemStack();
                            if (arrowItem.isEmpty()) {
                                arrowItem = new ItemStack(Items.ARROW);
                            }
                            player.getInventory().insertStack(arrowItem);
                        }
                        arrow.discard();
                        return false; // Prevent arrow damage when successfully retrieved
                    }
                }
            }

            return true;
        });
    }

    public static void clearPlayerState(UUID uuid) {
        PREVIOUS_ARMOR.remove(uuid);
    }

    public static void tickPlayerArmorEffects(ServerPlayerEntity player) {
        ServerWorld world = player.getEntityWorld();
        UUID uuid = player.getUuid();

        ItemStack head = player.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack legs = player.getEquippedStack(EquipmentSlot.LEGS);
        ItemStack feet = player.getEquippedStack(EquipmentSlot.FEET);
        boolean isGliding = player.isGliding();

        ArmorEquippedState prevState = PREVIOUS_ARMOR.get(uuid);

        int scorchLevel;
        if (prevState != null
                && prevState.player() == player
                && prevState.isGliding() == isGliding
                && ItemStack.areEqual(prevState.head(), head)
                && ItemStack.areEqual(prevState.chest(), chest)
                && ItemStack.areEqual(prevState.legs(), legs)
                && ItemStack.areEqual(prevState.feet(), feet)) {
            scorchLevel = prevState.scorchLevel();
        } else {
            // 1. Scorch Walker level from boots
            scorchLevel = EnchantmentHelper.getEquipmentLevel(ModEnchantments.SCORCH_WALKER, player, EquipmentSlot.FEET);

            // 2. Stride: Modify step height attribute (+0.5 for exactly 1 full block step-up) - strictly leggings only
            int strideLevel = EnchantmentHelper.getEquipmentLevel(ModEnchantments.STRIDE, player, EquipmentSlot.LEGS);
            EnchantmentHelper.updateAttributeModifier(
                    player.getAttributeInstance(EntityAttributes.STEP_HEIGHT),
                    STRIDE_MODIFIER_ID,
                    strideLevel > 0,
                    0.5,
                    EntityAttributeModifier.Operation.ADD_VALUE
            );

            // 3. Vitality: Modify max health (+2 HP per level = 1 heart per level) - strictly chestplate only
            int vitalityLevel = EnchantmentHelper.getEquipmentLevel(ModEnchantments.VITALITY, player, EquipmentSlot.CHEST);
            EnchantmentHelper.updateAttributeModifier(
                    player.getAttributeInstance(EntityAttributes.MAX_HEALTH),
                    VITALITY_MODIFIER_ID,
                    vitalityLevel > 0,
                    2.0 * vitalityLevel,
                    EntityAttributeModifier.Operation.ADD_VALUE
            );

            // 4. Graviole: Reduce gravity while gliding with elytra
            int gravioleLevel = EnchantmentHelper.getEquipmentLevel(ModEnchantments.GRAVIOLE, player, EquipmentSlot.CHEST);
            boolean applyGraviole = gravioleLevel > 0 && isGliding;
            EnchantmentHelper.updateAttributeModifier(
                    player.getAttributeInstance(EntityAttributes.GRAVITY),
                    GRAVIOLE_MODIFIER_ID,
                    applyGraviole,
                    -0.5 * (gravioleLevel / 3.0),
                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
            );

            PREVIOUS_ARMOR.put(uuid, new ArmorEquippedState(
                    player,
                    head.copy(),
                    chest.copy(),
                    legs.copy(),
                    feet.copy(),
                    isGliding,
                    scorchLevel
            ));
        }

        // Environmental effect: Scorch Walker (freeze nearby lava on ground)
        if (scorchLevel > 0 && player.isOnGround()) {
            freezeLavaNearby(world, player.getBlockPos(), scorchLevel);
        }
    }

    private static void freezeLavaNearby(ServerWorld world, BlockPos center, int level) {
        int radius = 2 + level;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        boolean convertedAny = false;
        long expireTime = world.getTime() + SCORCH_WALKER_DURATION_TICKS;
        Map<BlockPos, Long> worldMagma = TEMPORARY_MAGMA_BLOCKS.computeIfAbsent(world.getRegistryKey(), k -> new ConcurrentHashMap<>());

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radius * radius) {
                    mutable.set(center.getX() + x, center.getY() - 1, center.getZ() + z);
                    // Surface check: must be still lava source block with air above it (matching vanilla Frost Walker)
                    if (world.getBlockState(mutable).isOf(Blocks.LAVA)
                            && world.getFluidState(mutable).isStill()
                            && world.getBlockState(mutable.up()).isAir()) {
                        BlockPos immutable = mutable.toImmutable();
                        world.setBlockState(immutable, Blocks.MAGMA_BLOCK.getDefaultState());
                        worldMagma.putIfAbsent(immutable, expireTime);
                        world.spawnParticles(
                                ParticleTypes.SMOKE,
                                immutable.getX() + 0.5,
                                immutable.getY() + 1.05,
                                immutable.getZ() + 0.5,
                                2,
                                0.2, 0.1, 0.2, 0.02
                        );
                        convertedAny = true;
                    }
                }
            }
        }
        if (convertedAny) {
            world.playSound(null, center, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.35f, 1.8f);
        }
    }

    /**
     * Luminosity: Places a dynamic light block (level 5, 10, or 15) at the player's position
     * when wearing an enchanted chestplate, and cleans up when moving or taking off the chestplate.
     */
    private static void handleLuminosity(ServerPlayerEntity player) {
        int level = EnchantmentHelper.getEquipmentLevel(ModEnchantments.LUMINOSITY, player, EquipmentSlot.CHEST);
        UUID uuid = player.getUuid();
        BlockPos prevPos = PLAYER_LIGHT_BLOCKS.get(uuid);
        ServerWorld world = player.getEntityWorld();

        if (level <= 0) {
            if (prevPos != null) {
                removeLightBlock(world, prevPos);
                PLAYER_LIGHT_BLOCKS.remove(uuid);
            }
            return;
        }

        // Determine ideal position: player feet or head
        BlockPos candidatePos = player.getBlockPos();
        BlockState currentState = world.getBlockState(candidatePos);
        boolean candidateValid = currentState.isAir()
                || (currentState.isOf(Blocks.WATER) && world.getFluidState(candidatePos).isStill())
                || currentState.isOf(Blocks.LIGHT);

        if (!candidateValid) {
            BlockPos headPos = candidatePos.up();
            BlockState headState = world.getBlockState(headPos);
            if (headState.isAir() || (headState.isOf(Blocks.WATER) && world.getFluidState(headPos).isStill()) || headState.isOf(Blocks.LIGHT)) {
                candidatePos = headPos;
                currentState = headState;
                candidateValid = true;
            }
        }

        if (!candidateValid) {
            return;
        }

        if (prevPos != null && !prevPos.equals(candidatePos)) {
            removeLightBlock(world, prevPos);
            PLAYER_LIGHT_BLOCKS.remove(uuid);
        }

        boolean isWater = currentState.isOf(Blocks.WATER)
                || (currentState.isOf(Blocks.LIGHT) && currentState.contains(LightBlock.WATERLOGGED) && currentState.get(LightBlock.WATERLOGGED));
        int targetLightLevel = Math.min(15, Math.max(1, level * 5)); // Level 1 -> 5, Level 2 -> 10, Level 3 -> 15

        if (!currentState.isOf(Blocks.LIGHT)
                || currentState.get(LightBlock.LEVEL_15) != targetLightLevel
                || currentState.get(LightBlock.WATERLOGGED) != isWater) {
            world.setBlockState(candidatePos, Blocks.LIGHT.getDefaultState()
                    .with(LightBlock.LEVEL_15, targetLightLevel)
                    .with(LightBlock.WATERLOGGED, isWater), 3);
        }

        PLAYER_LIGHT_BLOCKS.put(uuid, candidatePos);
    }

    private static void removeLightBlock(ServerWorld world, BlockPos pos) {
        if (pos == null || world == null) {
            return;
        }
        BlockState state = world.getBlockState(pos);
        if (state.isOf(Blocks.LIGHT)) {
            boolean waterlogged = state.contains(LightBlock.WATERLOGGED) && state.get(LightBlock.WATERLOGGED);
            world.setBlockState(pos, waterlogged ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState(), 3);
        }
    }

    /**
     * Ticks temporary magma blocks created by Scorch Walker.
     * When 2.5 seconds (50 ticks) have elapsed, reverts the magma block back into a lava source block.
     */
    private static void tickTemporaryMagma(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) {
            Map<BlockPos, Long> worldMagma = TEMPORARY_MAGMA_BLOCKS.get(world.getRegistryKey());
            if (worldMagma == null || worldMagma.isEmpty()) {
                continue;
            }
            long currentTime = world.getTime();
            Iterator<Map.Entry<BlockPos, Long>> iterator = worldMagma.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<BlockPos, Long> entry = iterator.next();
                if (currentTime >= entry.getValue()) {
                    BlockPos pos = entry.getKey();
                    iterator.remove();

                    if (world.getChunkManager().isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
                        if (world.getBlockState(pos).isOf(Blocks.MAGMA_BLOCK)) {
                            world.setBlockState(pos, Blocks.LAVA.getDefaultState(), 3);
                            world.spawnParticles(
                                    ParticleTypes.LAVA,
                                    pos.getX() + 0.5,
                                    pos.getY() + 1.05,
                                    pos.getZ() + 0.5,
                                    3,
                                    0.2, 0.1, 0.2, 0.05
                            );
                            world.spawnParticles(
                                    ParticleTypes.SMOKE,
                                    pos.getX() + 0.5,
                                    pos.getY() + 1.05,
                                    pos.getZ() + 0.5,
                                    4,
                                    0.2, 0.2, 0.2, 0.02
                            );
                            world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.35f, 1.2f);
                        }
                    }
                }
            }
        }
    }

    /**
     * Reverts all active temporary magma blocks back to lava on server shutdown.
     */
    public static void cleanupAllTemporaryMagma(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) {
            Map<BlockPos, Long> worldMagma = TEMPORARY_MAGMA_BLOCKS.remove(world.getRegistryKey());
            if (worldMagma != null) {
                for (BlockPos pos : worldMagma.keySet()) {
                    if (world.getChunkManager().isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
                        if (world.getBlockState(pos).isOf(Blocks.MAGMA_BLOCK)) {
                            world.setBlockState(pos, Blocks.LAVA.getDefaultState(), 3);
                        }
                    }
                }
            }
        }
    }

    public static int getActiveTemporaryMagmaCount(RegistryKey<World> worldKey) {
        Map<BlockPos, Long> map = TEMPORARY_MAGMA_BLOCKS.get(worldKey);
        return map == null ? 0 : map.size();
    }

    public static void clearAllTemporaryMagma() {
        TEMPORARY_MAGMA_BLOCKS.clear();
    }
}
