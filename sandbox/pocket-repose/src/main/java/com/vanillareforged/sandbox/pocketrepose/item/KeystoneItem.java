package com.vanillareforged.sandbox.pocketrepose.item;

import com.vanillareforged.sandbox.pocketrepose.world.PocketReposeManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.List;

public class KeystoneItem extends Item {

    public KeystoneItem(Settings settings) {
        super(settings);
    }

    public static String sanitizeName(String raw) {
        return PocketReposeManager.sanitizeKeystoneName(raw);
    }

    public static String getSanitizedKeystoneName(ItemStack stack) {
        if (!stack.contains(DataComponentTypes.CUSTOM_NAME)) {
            return "";
        }
        Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (customName == null) {
            return "";
        }
        return PocketReposeManager.sanitizeKeystoneName(customName.getString());
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        String keyName = getSanitizedKeystoneName(stack);
        if (keyName.isEmpty()) {
            user.sendMessage(Text.literal("§cRename the Traveler's Key in an anvil first to bind it!"), true);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_CHAIN_PLACE, SoundCategory.PLAYERS, 0.5f, 1.8f);
            return ActionResult.FAIL;
        }

        // 1. If inside the pocket dimension: acts as emergency rescue key back to suitcase!
        if (PocketReposeManager.isPocketDimension(world)) {
            if (user instanceof ServerPlayerEntity serverPlayer) {
                user.sendMessage(Text.literal("§aRescuing you back to your suitcase..."), true);
                PocketReposeManager.exitPocketDimension(serverPlayer);
                return ActionResult.SUCCESS;
            }
        }

        // 2. In overworld: bind and prepare the key
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
            Text.literal("§7Bound Pocket: §e" + keyName.replace("_", " ")),
            Text.literal("§8Use on a Traveler's Suitcase to bind/lock.")
        )));

        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_AMETHYST_CLUSTER_FALL, SoundCategory.PLAYERS, 1.2f, 1.5f);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.4f, 2.0f);
        user.sendMessage(Text.literal("§aTraveler's Key bound to pocket: §e" + keyName), true);
        return ActionResult.SUCCESS;
    }
}
