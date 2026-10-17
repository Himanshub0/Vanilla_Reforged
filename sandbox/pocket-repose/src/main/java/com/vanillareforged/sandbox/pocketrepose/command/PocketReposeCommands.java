package com.vanillareforged.sandbox.pocketrepose.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.vanillareforged.sandbox.pocketrepose.item.ModItems;
import com.vanillareforged.sandbox.pocketrepose.world.PocketReposeManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

public class PocketReposeCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("pocket")
            .then(CommandManager.literal("kit")
                .executes(ctx -> giveTestingKit(ctx.getSource()))
            )
            .then(CommandManager.literal("tp")
                .then(CommandManager.argument("keystone", StringArgumentType.word())
                    .executes(ctx -> warpToPocket(ctx.getSource(), StringArgumentType.getString(ctx, "keystone")))
                )
            )
            .then(CommandManager.literal("exit")
                .executes(ctx -> exitPocket(ctx.getSource()))
            )
        );
    }

    private static int giveTestingKit(ServerCommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("Must be executed by a player."));
            return 0;
        }

        PlayerInventory inv = player.getInventory();
        inv.offerOrDrop(new ItemStack(ModItems.SUITCASE, 2));
        inv.offerOrDrop(new ItemStack(ModItems.KEYSTONE, 3));
        inv.offerOrDrop(new ItemStack(Items.ANVIL, 1));
        inv.offerOrDrop(new ItemStack(Items.NAME_TAG, 5));
        inv.offerOrDrop(new ItemStack(Items.EXPERIENCE_BOTTLE, 16));
        inv.offerOrDrop(new ItemStack(Items.LEATHER, 16));
        inv.offerOrDrop(new ItemStack(Items.GOLD_INGOT, 16));
        inv.offerOrDrop(new ItemStack(Items.ENDER_CHEST, 2));

        source.sendFeedback(() -> Text.literal("§a[Pocket Repose] Granted Pocket Repose Testing Kit!"), false);
        return 1;
    }

    private static int warpToPocket(ServerCommandSource source, String keystoneName) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("Must be executed by a player."));
            return 0;
        }

        ServerWorld pocketWorld = source.getServer().getWorld(PocketReposeManager.POCKET_WORLD_KEY);
        if (pocketWorld == null) {
            source.sendError(Text.literal("§cPocket dimension is not available."));
            return 0;
        }

        int sector = PocketReposeManager.getOrCreateSector(keystoneName);
        BlockPos center = PocketReposeManager.getSectorCenter(sector);

        Vec3d dest = new Vec3d(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5);
        TeleportTarget tp = new TeleportTarget(pocketWorld, dest, Vec3d.ZERO, 0f, 0f, TeleportTarget.NO_OP);
        player.teleportTo(tp);

        source.sendFeedback(() -> Text.literal("§d[Pocket Repose] Teleported to pocket dimension sector: §e" + keystoneName), false);
        return 1;
    }

    private static int exitPocket(ServerCommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("Must be executed by a player."));
            return 0;
        }

        PocketReposeManager.exitPocketDimension(player);
        return 1;
    }
}
