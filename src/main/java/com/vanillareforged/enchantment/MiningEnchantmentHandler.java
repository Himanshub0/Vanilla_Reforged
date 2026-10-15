package com.vanillareforged.enchantment;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Handles Mining & Harvesting Enchantments:
 * - Pyrolysis: 100% auto-smelt on ores (Iron, Gold, Copper, Ancient Debris, Sand, Clay, Stone, etc.)
 *              plus dynamic furnace recipe fallback, with furnace crackle and flame particles!
 * - Gluttony: Telekinesis effect that inserts mined block drops directly into the player's inventory
 *             with pickup sound and instant client sync; pulls XP orbs at Level 3;
 *             and continuously vacuums nearby items for tools/weapons held.
 * - Crab's Touch: Extends block interaction reach distance (+1 block per level).
 */
public final class MiningEnchantmentHandler {

    private static final Identifier CRABS_TOUCH_MODIFIER_ID = Identifier.of("vanilla_reforged", "enchantment.crabs_touch");

    public record MiningEquippedState(ServerPlayerEntity player, ItemStack stack, int crabsLevel) {}
    private static final Map<UUID, MiningEquippedState> PREVIOUS_EQUIPMENT = new WeakHashMap<>();

    private MiningEnchantmentHandler() {}

    public static void register() {
        // Clear equipment tracking on player disconnect and respawn
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            PREVIOUS_EQUIPMENT.remove(handler.getPlayer().getUuid());
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            PREVIOUS_EQUIPMENT.remove(oldPlayer.getUuid());
        });

        // Crab's Touch reach attributes & Gluttony continuous vacuum on tick
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                updateCrabsTouchAttribute(player);
                handleGluttonyContinuousVacuum(player);
            }
        });

        // Pyrolysis & Gluttony handling on block break
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) {
                return;
            }

            ServerWorld serverWorld = (ServerWorld) world;
            ItemStack heldStack = player.getMainHandStack();

            // Skip containers with block entities (chests, shulker boxes, barrels) to prevent inventory clutter
            if (state.hasBlockEntity() || blockEntity != null) {
                return;
            }

            int pyrolysisLevel = EnchantmentHelper.getLevel(ModEnchantments.PYROLYSIS, heldStack, player);
            // Pyrolysis is strictly restricted to Pickaxes
            if (!heldStack.isIn(ItemTags.PICKAXES)) {
                pyrolysisLevel = 0;
            }
            int gluttonyLevel = EnchantmentHelper.getLevel(ModEnchantments.GLUTTONY, heldStack, player);

            if (pyrolysisLevel <= 0 && gluttonyLevel <= 0) {
                return;
            }

            // Find item entities dropped at this block position immediately after break (generous search box)
            Box searchBox = new Box(pos).expand(3.5);
            List<ItemEntity> droppedItems = serverWorld.getEntitiesByClass(ItemEntity.class, searchBox, itemEntity -> !itemEntity.isRemoved());

            boolean playedSmeltFx = false;

            for (ItemEntity itemEntity : droppedItems) {
                ItemStack stack = itemEntity.getStack();

                // Pyrolysis: 100% auto-smelt mined ores and blocks
                if (pyrolysisLevel > 0) {
                    ItemStack smelted = getSmeltedResult(stack, serverWorld);
                    if (!smelted.isOf(stack.getItem()) || smelted.getCount() != stack.getCount()) {
                        itemEntity.setStack(smelted);
                        stack = smelted;

                        if (!playedSmeltFx) {
                            playedSmeltFx = true;
                            serverWorld.playSound(null, pos, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 0.8f, 1.0f);
                            serverWorld.spawnParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.25, 0.25, 0.25, 0.02);
                        }
                    }
                }

                // Gluttony: Directly insert into player inventory
                if (gluttonyLevel > 0) {
                    insertItemDirectly(serverPlayer, itemEntity);
                }
            }

            // Gluttony III: Absorb experience orbs directly into the player
            if (gluttonyLevel >= 3) {
                List<ExperienceOrbEntity> xpOrbs = serverWorld.getEntitiesByClass(ExperienceOrbEntity.class, new Box(pos).expand(6.0), orb -> !orb.isRemoved());
                for (ExperienceOrbEntity orb : xpOrbs) {
                    orb.setPosition(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ());
                    orb.onPlayerCollision(serverPlayer);
                }
            }
        });
    }

    /**
     * Resolves the 100% smelted version of mined items (ores, stones, sand, clay, cactus, logs, etc.)
     * with dynamic furnace smelting and blasting recipe lookup.
     */
    public static ItemStack getSmeltedResult(ItemStack stack, ServerWorld world) {
        if (stack.isEmpty()) {
            return stack;
        }

        // 1. Direct fast-path for all standard ores & minerals (preserves stack count from Fortune)
        if (stack.isOf(Items.RAW_IRON) || stack.isOf(Items.IRON_ORE) || stack.isOf(Items.DEEPSLATE_IRON_ORE)) {
            return new ItemStack(Items.IRON_INGOT, stack.getCount());
        }
        if (stack.isOf(Items.RAW_GOLD) || stack.isOf(Items.GOLD_ORE) || stack.isOf(Items.DEEPSLATE_GOLD_ORE) || stack.isOf(Items.NETHER_GOLD_ORE)) {
            return new ItemStack(Items.GOLD_INGOT, stack.getCount());
        }
        if (stack.isOf(Items.RAW_COPPER) || stack.isOf(Items.COPPER_ORE) || stack.isOf(Items.DEEPSLATE_COPPER_ORE)) {
            return new ItemStack(Items.COPPER_INGOT, stack.getCount());
        }
        if (stack.isOf(Items.ANCIENT_DEBRIS)) {
            return new ItemStack(Items.NETHERITE_SCRAP, stack.getCount());
        }
        if (stack.isOf(Items.SAND) || stack.isOf(Items.RED_SAND)) {
            return new ItemStack(Items.GLASS, stack.getCount());
        }
        if (stack.isOf(Items.COBBLESTONE) || stack.isOf(Items.COBBLED_DEEPSLATE)) {
            return new ItemStack(stack.isOf(Items.COBBLESTONE) ? Items.STONE : Items.DEEPSLATE, stack.getCount());
        }
        if (stack.isOf(Items.STONE)) {
            return new ItemStack(Items.SMOOTH_STONE, stack.getCount());
        }
        if (stack.isOf(Items.CLAY_BALL)) {
            return new ItemStack(Items.BRICK, stack.getCount());
        }
        if (stack.isOf(Items.CLAY)) {
            return new ItemStack(Items.TERRACOTTA, stack.getCount());
        }
        if (stack.isOf(Items.CACTUS)) {
            return new ItemStack(Items.GREEN_DYE, stack.getCount());
        }
        if (stack.isOf(Items.NETHERRACK)) {
            return new ItemStack(Items.NETHER_BRICK, stack.getCount());
        }
        if (stack.isOf(Items.POTATO)) {
            return new ItemStack(Items.BAKED_POTATO, stack.getCount());
        }
        if (stack.isIn(ItemTags.LOGS)) {
            return new ItemStack(Items.CHARCOAL, stack.getCount());
        }

        // 2. Dynamic smelting recipe lookup fallback
        SingleStackRecipeInput recipeInput = new SingleStackRecipeInput(stack);
        Optional<RecipeEntry<SmeltingRecipe>> smeltingRecipe = world.getRecipeManager()
                .getFirstMatch(RecipeType.SMELTING, recipeInput, world);

        if (smeltingRecipe.isPresent()) {
            ItemStack result = smeltingRecipe.get().value().craft(recipeInput, world.getRegistryManager());
            if (!result.isEmpty()) {
                result.setCount(result.getCount() * stack.getCount());
                return result;
            }
        }
        return stack;
    }

    /**
     * Directly inserts an ItemEntity's contents into the player's inventory,
     * plays pickup sound, and forces an immediate client screen sync.
     * Any overflow safely teleports to the player's feet with 0 pickup delay.
     */
    public static void insertItemDirectly(ServerPlayerEntity player, ItemEntity itemEntity) {
        if (itemEntity == null || itemEntity.isRemoved()) {
            return;
        }

        ItemStack stack = itemEntity.getStack();
        if (stack.isEmpty()) {
            itemEntity.discard();
            return;
        }

        ItemStack toInsert = stack.copy();
        int initialCount = toInsert.getCount();

        if (player.getInventory().insertStack(toInsert)) {
            int inserted = initialCount - toInsert.getCount();
            if (inserted > 0) {
                player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 0.25f, (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2f + 1.0f);
                player.playerScreenHandler.sendContentUpdates();
            }

            if (toInsert.isEmpty()) {
                itemEntity.discard();
                return;
            }
        }

        // If inventory was full or partially full, teleport remainder to player's feet with 0 pickup delay
        itemEntity.setStack(toInsert);
        itemEntity.setPosition(player.getX(), player.getY() + 0.2, player.getZ());
        itemEntity.setVelocity(Vec3d.ZERO);
        itemEntity.setPickupDelay(0);
    }

    /**
     * Continuous Gluttony vacuum: Sucks nearby dropped items and experience orbs
     * directly towards the player holding a Gluttony tool/weapon.
     */
    private static void handleGluttonyContinuousVacuum(ServerPlayerEntity player) {
        ItemStack held = player.getMainHandStack();
        int gluttonyLevel = EnchantmentHelper.getLevel(ModEnchantments.GLUTTONY, held, player);
        if (gluttonyLevel <= 0) {
            return;
        }

        double radius = gluttonyLevel == 1 ? 3.0 : (gluttonyLevel == 2 ? 8.0 : 10.0);
        ServerWorld world = player.getEntityWorld();
        Box box = player.getBoundingBox().expand(radius);

        List<ItemEntity> nearbyItems = world.getEntitiesByClass(ItemEntity.class, box, item -> !item.isRemoved() && !item.cannotPickup());
        for (ItemEntity item : nearbyItems) {
            insertItemDirectly(player, item);
        }

        if (gluttonyLevel >= 3) {
            List<ExperienceOrbEntity> nearbyOrbs = world.getEntitiesByClass(ExperienceOrbEntity.class, player.getBoundingBox().expand(6.0), orb -> !orb.isRemoved());
            for (ExperienceOrbEntity orb : nearbyOrbs) {
                orb.setPosition(player.getX(), player.getY(), player.getZ());
                orb.onPlayerCollision(player);
            }
        }
    }

    public static void clearPlayerState(UUID uuid) {
        PREVIOUS_EQUIPMENT.remove(uuid);
    }

    public static void updateCrabsTouchAttribute(ServerPlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        UUID uuid = player.getUuid();
        MiningEquippedState prevState = PREVIOUS_EQUIPMENT.get(uuid);

        if (prevState != null && prevState.player() == player && ItemStack.areEqual(prevState.stack(), mainHand)) {
            return;
        }

        int crabsLevel = EnchantmentHelper.getLevel(ModEnchantments.CRABS_TOUCH, mainHand, player);

        // BLOCK_INTERACTION_RANGE modifies block reach in 1.21+
        EnchantmentHelper.updateAttributeModifier(
                player.getAttributeInstance(EntityAttributes.BLOCK_INTERACTION_RANGE),
                CRABS_TOUCH_MODIFIER_ID,
                crabsLevel > 0,
                1.0 * crabsLevel,
                EntityAttributeModifier.Operation.ADD_VALUE
        );

        PREVIOUS_EQUIPMENT.put(uuid, new MiningEquippedState(player, mainHand.copy(), crabsLevel));
    }
}
