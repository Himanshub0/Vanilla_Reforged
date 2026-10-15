package com.vanillareforged.enchantment;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.WeakHashMap;

public final class CombatEnchantmentHandler {

    private static final Identifier SWIFT_STRIKE_MODIFIER_ID = Identifier.of("vanilla_reforged", "enchantment.swift_strike");
    private static final Identifier OUTREACH_MODIFIER_ID = Identifier.of("vanilla_reforged", "enchantment.outreach");

    private static final Random RANDOM = new Random();

    public record CombatEquippedState(ServerPlayerEntity player, ItemStack stack, int swiftLevel, int reachLevel) {}
    private static final Map<UUID, CombatEquippedState> PREVIOUS_EQUIPMENT = new WeakHashMap<>();

    private CombatEnchantmentHandler() {}

    public static void register() {
        // AttackEntityCallback: Handles Ice Aspect, Websnare, Curses on hit
        AttackEntityCallback.EVENT.register((player, world, hand, target, hitResult) -> {
            if (world.isClient() || hand != Hand.MAIN_HAND || !(target instanceof LivingEntity livingTarget) || !(world instanceof ServerWorld serverWorld)) {
                return ActionResult.PASS;
            }

            ItemStack mainHandStack = player.getMainHandStack();

            // 1. Ice Aspect
            int iceAspectLevel = EnchantmentHelper.getLevel(ModEnchantments.ICE_ASPECT, mainHandStack, player);
            if (iceAspectLevel > 0) {
                float chance = iceAspectLevel == 1 ? 0.10f : 0.20f;
                if (RANDOM.nextFloat() < chance) {
                    // Freeze for 7 seconds (140 ticks): apply extreme slowness & freeze ticks with glass shatter SFX
                    livingTarget.setFrozenTicks(140);
                    livingTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 140, 9, false, true, true));
                    livingTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 140, 9, false, true, true));
                    serverWorld.playSound(null, livingTarget.getBlockPos(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 0.8f, 1.4f);
                    serverWorld.spawnParticles(ParticleTypes.SNOWFLAKE, livingTarget.getX(), livingTarget.getY() + 1.0, livingTarget.getZ(), 18, 0.4, 0.5, 0.4, 0.05);
                }
            }

            // 2. Websnare
            int webLevel = EnchantmentHelper.getLevel(ModEnchantments.WEBSNARE, mainHandStack, player);
            if (webLevel > 0) {
                float chance = webLevel == 1 ? 0.15f : 0.25f;
                if (RANDOM.nextFloat() < chance) {
                    BlockPos targetPos = target.getBlockPos();
                    if (world.getBlockState(targetPos).isAir()) {
                        world.setBlockState(targetPos, Blocks.COBWEB.getDefaultState());
                    }
                }
            }

            // 3. Curse of Displacement
            int displacementLevel = EnchantmentHelper.getLevel(ModEnchantments.CURSE_OF_DISPLACEMENT, mainHandStack, player);
            if (displacementLevel > 0) {
                if (RANDOM.nextFloat() < 0.20f) {
                    // Teleport enemy 4-6 blocks behind attacker (with solid ground/air collision check)
                    Vec3d attackerPos = player.getEntityPos();
                    Vec3d lookVec = player.getRotationVec(1.0f).normalize();
                    double distance = 4.0 + RANDOM.nextDouble() * 2.0; // 4 to 6 blocks
                    Vec3d teleportPos = attackerPos.subtract(lookVec.multiply(distance));

                    BlockPos targetPos = BlockPos.ofFloored(teleportPos);
                    if (world.getBlockState(targetPos).isAir() && world.getBlockState(targetPos.up()).isAir()) {
                        serverWorld.playSound(null, livingTarget.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.9f, 1.0f);
                        serverWorld.spawnParticles(ParticleTypes.PORTAL, livingTarget.getX(), livingTarget.getY() + 1.0, livingTarget.getZ(), 20, 0.4, 0.5, 0.4, 0.1);
                        livingTarget.refreshPositionAfterTeleport(teleportPos.x, teleportPos.y, teleportPos.z);
                        serverWorld.playSound(null, targetPos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.9f, 1.0f);
                        serverWorld.spawnParticles(ParticleTypes.PORTAL, teleportPos.x, teleportPos.y + 1.0, teleportPos.z, 20, 0.4, 0.5, 0.4, 0.1);
                    }
                }
            }

            // 4. Curse of Double Edge
            int doubleEdgeLevel = EnchantmentHelper.getLevel(ModEnchantments.CURSE_OF_DOUBLE_EDGE, mainHandStack, player);
            if (doubleEdgeLevel > 0) {
                if (RANDOM.nextFloat() < 0.40f) {
                    // Damage attacker for 1-2 hearts (mitigated by player armor)
                    float damageAmount = 2.0f + (RANDOM.nextBoolean() ? 2.0f : 0.0f);
                    player.damage(serverWorld, serverWorld.getDamageSources().playerAttack(player), damageAmount);
                }
            }

            // 5. Curse of Clumsiness on attack: Combat fumble (drains attack recharge to 0% and plays disarm sound)
            int clumsinessLevel = EnchantmentHelper.getLevel(ModEnchantments.CURSE_OF_CLUMSINESS, mainHandStack, player);
            if (clumsinessLevel > 0) {
                if (RANDOM.nextFloat() < 0.15f) {
                    player.resetTicksSinceLastAttack();
                    if (!mainHandStack.isEmpty()) {
                        player.getItemCooldownManager().set(mainHandStack, 25);
                    }
                    player.playSound(net.minecraft.sound.SoundEvents.ITEM_SHIELD_BLOCK.value(), 1.0f, 0.6f);
                }
            }

            return ActionResult.PASS;
        });

        // Curse of Clumsiness on attack block / start break: Fumble weapon cooldown
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (world.isClient()) {
                return ActionResult.PASS;
            }
            ItemStack heldStack = player.getStackInHand(hand);
            int clumsinessLevel = EnchantmentHelper.getLevel(ModEnchantments.CURSE_OF_CLUMSINESS, heldStack, player);
            if (clumsinessLevel > 0) {
                if (RANDOM.nextFloat() < 0.15f) {
                    player.resetTicksSinceLastAttack();
                    if (!heldStack.isEmpty()) {
                        player.getItemCooldownManager().set(heldStack, 25);
                    }
                    player.playSound(net.minecraft.sound.SoundEvents.ITEM_SHIELD_BLOCK.value(), 0.8f, 0.6f);
                }
            }
            return ActionResult.PASS;
        });

        // Clear equipment tracking on player disconnect and respawn
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            PREVIOUS_EQUIPMENT.remove(handler.getPlayer().getUuid());
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            PREVIOUS_EQUIPMENT.remove(oldPlayer.getUuid());
        });

        // Attribute updates: Swift Strike & Outreach (only when equipped weapon or enchantment changes)
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                updateWeaponAttributes(player);
            }
        });
    }

    public static void clearPlayerState(UUID uuid) {
        PREVIOUS_EQUIPMENT.remove(uuid);
    }

    public static void updateWeaponAttributes(ServerPlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        UUID uuid = player.getUuid();
        CombatEquippedState prevState = PREVIOUS_EQUIPMENT.get(uuid);

        if (prevState != null && prevState.player() == player && ItemStack.areEqual(prevState.stack(), mainHand)) {
            return;
        }

        // Swift Strike: +0.3 attack speed per level
        int swiftLevel = EnchantmentHelper.getLevel(ModEnchantments.SWIFT_STRIKE, mainHand, player);
        EnchantmentHelper.updateAttributeModifier(
                player.getAttributeInstance(EntityAttributes.ATTACK_SPEED),
                SWIFT_STRIKE_MODIFIER_ID,
                swiftLevel > 0,
                0.3 * swiftLevel,
                EntityAttributeModifier.Operation.ADD_VALUE
        );

        // Outreach: +0.5 entity interaction range / attack reach per level
        int reachLevel = EnchantmentHelper.getLevel(ModEnchantments.OUTREACH, mainHand, player);
        EnchantmentHelper.updateAttributeModifier(
                player.getAttributeInstance(EntityAttributes.ENTITY_INTERACTION_RANGE),
                OUTREACH_MODIFIER_ID,
                reachLevel > 0,
                0.5 * reachLevel,
                EntityAttributeModifier.Operation.ADD_VALUE
        );

        PREVIOUS_EQUIPMENT.put(uuid, new CombatEquippedState(player, mainHand.copy(), swiftLevel, reachLevel));
    }
}
