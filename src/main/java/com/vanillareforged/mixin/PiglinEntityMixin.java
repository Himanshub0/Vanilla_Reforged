package com.vanillareforged.mixin;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PiglinEntity.class)
public abstract class PiglinEntityMixin {

    @Shadow
    private SimpleInventory inventory;

    /**
     * Mid-Barter Piglin Drop:
     * When a Piglin dies while holding a gold ingot (in main hand or off hand mid-barter / inspecting)
     * or carrying gold ingots in inventory, convert those ingots into 2-4 gold nuggets instead of dropping raw ingots.
     * Guarantees piglins never drop gold ingots directly even if they were holding or bartering one.
     */
    @Inject(method = "dropEquipment", at = @At("HEAD"))
    private void convertGoldIngotsToNuggets(ServerWorld world, DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        PiglinEntity piglin = (PiglinEntity) (Object) this;
        Random random = world != null ? world.getRandom() : piglin.getRandom();

        // Check main hand and off hand for held gold ingots (mid-barter / inspecting)
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}) {
            ItemStack stack = piglin.getEquippedStack(slot);
            if (stack.isOf(Items.GOLD_INGOT)) {
                int nuggets = 0;
                for (int i = 0; i < stack.getCount(); i++) {
                    nuggets += 2 + random.nextInt(3); // 2-4 nuggets per ingot
                }
                piglin.equipStack(slot, new ItemStack(Items.GOLD_NUGGET, nuggets));
                piglin.setEquipmentDropChance(slot, 2.0f);
            }
        }

        // Convert any gold ingots in Piglin inventory as well
        if (this.inventory != null) {
            for (int i = 0; i < this.inventory.size(); i++) {
                ItemStack stack = this.inventory.getStack(i);
                if (stack.isOf(Items.GOLD_INGOT)) {
                    int nuggets = 0;
                    for (int j = 0; j < stack.getCount(); j++) {
                        nuggets += 2 + random.nextInt(3); // 2-4 nuggets per ingot
                    }
                    this.inventory.setStack(i, new ItemStack(Items.GOLD_NUGGET, Math.min(64, nuggets)));
                }
            }
        }
    }
}
