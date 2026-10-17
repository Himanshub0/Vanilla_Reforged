package com.vanillareforged.sandbox.pocketrepose;

import com.vanillareforged.sandbox.pocketrepose.block.ModBlocks;
import com.vanillareforged.sandbox.pocketrepose.block.entity.ModBlockEntities;
import com.vanillareforged.sandbox.pocketrepose.command.PocketReposeCommands;
import com.vanillareforged.sandbox.pocketrepose.item.ModItems;
import com.vanillareforged.sandbox.pocketrepose.world.PocketReposeManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PocketReposeMod implements ModInitializer {

    public static final String MOD_ID = "pocket_repose";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[Pocket Repose Sandbox] Initializing Traveler's Suitcase & Pocket Dimension system...");

        ModBlocks.registerBlocks();
        ModBlockEntities.registerBlockEntities();
        ModItems.registerItems();

        CommandRegistrationCallback.EVENT.register(PocketReposeCommands::register);

        ServerLifecycleEvents.SERVER_STARTED.register(PocketReposeManager::loadState);
        ServerLifecycleEvents.SERVER_STOPPING.register(PocketReposeManager::saveState);

        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_WORLD_TICK.register(PocketReposeManager::tickPocketWorld);

        registerMobCaptureCallback();

        LOGGER.info("[Pocket Repose Sandbox] Initialized successfully!");
    }

    private void registerMobCaptureCallback() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() || hand != Hand.MAIN_HAND) return ActionResult.PASS;

            ItemStack held = player.getStackInHand(hand);
            if (held.getItem() instanceof net.minecraft.item.BlockItem bi && bi.getBlock() instanceof com.vanillareforged.sandbox.pocketrepose.block.SuitcaseBlock) {
                NbtComponent customData = held.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
                if (!customData.isEmpty()) {
                    NbtCompound nbt = customData.copyNbt();
                    if (nbt.contains("BoundKeystone")) {
                        String keystone = nbt.getString("BoundKeystone").orElse("");
                        if (!keystone.isEmpty() && entity instanceof LivingEntity living && player instanceof ServerPlayerEntity serverPlayer) {
                            if (PocketReposeManager.captureMob(living, serverPlayer, keystone)) {
                                return ActionResult.SUCCESS;
                            }
                        }
                    }
                }
            }
            return ActionResult.PASS;
        });
    }
}
