package com.vanillareforged.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import net.minecraft.sound.SoundEvents;

@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentScreenHandlerMixin extends ScreenHandler {

    @Shadow @Final private Inventory inventory;
    @Shadow @Final public int[] enchantmentPower;
    @Shadow @Final private Random random;
    @Shadow @Final private Property seed;

    protected EnchantmentScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Unique
    private boolean isGoldReagent() {
        ItemStack reagent = this.inventory.getStack(1);
        return !reagent.isEmpty() && reagent.isOf(Items.GOLD_INGOT);
    }

    /**
     * 1. Allow gold ingots in the lapis slot (slot index 1) and play an enchanting chime when inserted.
     */
    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At("TAIL"))
    private void allowGoldInReagentSlot(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, CallbackInfo ci) {
        if (this.slots.size() > 1) {
            Slot originalSlot = this.slots.get(1);
            Slot reagentSlot = new Slot(this.inventory, 1, originalSlot.x, originalSlot.y) {
                @Override
                public boolean canInsert(ItemStack stack) {
                    return stack.isOf(Items.LAPIS_LAZULI) || stack.isOf(Items.GOLD_INGOT);
                }

                @Override
                public void setStack(ItemStack stack) {
                    boolean wasGold = isGoldReagent();
                    super.setStack(stack);
                    if (!wasGold && !stack.isEmpty() && stack.isOf(Items.GOLD_INGOT)) {
                        playerInventory.player.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.4f);
                    }
                }
            };
            reagentSlot.id = originalSlot.id;
            this.slots.set(1, reagentSlot);
        }
    }

    /**
     * 2. Allow quick-moving (shift-clicking) gold ingots into the reagent slot (slot 1).
     */
    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    private void quickMoveGoldReagent(PlayerEntity player, int slotIndex, CallbackInfoReturnable<ItemStack> cir) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return;
        }

        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasStack()) {
            ItemStack stack = slot.getStack();
            if (stack.isOf(Items.GOLD_INGOT) && slotIndex != 0 && slotIndex != 1) {
                if (!this.insertItem(stack, 1, 2, true)) {
                    cir.setReturnValue(ItemStack.EMPTY);
                    return;
                }
                if (stack.isEmpty()) {
                    slot.setStack(ItemStack.EMPTY);
                } else {
                    slot.markDirty();
                }
                cir.setReturnValue(stack);
            }
        }
    }

    /**
     * 3. Dual resource enchanting:
     * When Gold Ingot is in slot 1: ONLY suggest and grant new custom enchantments (vanilla_reforged:*).
     * When Lapis Lazuli (or empty) is in slot 1: ONLY suggest and grant vanilla enchantments (minecraft:*).
     */
    @Inject(method = "generateEnchantments", at = @At("HEAD"), cancellable = true)
    private void onGenerateEnchantments(
            DynamicRegistryManager dynamicRegistryManager,
            ItemStack stack,
            int slot,
            int level,
            CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        boolean gold = this.isGoldReagent();
        this.random.setSeed((long) (this.seed.get() + slot));

        Registry<Enchantment> registry = dynamicRegistryManager.getOrThrow(RegistryKeys.ENCHANTMENT);
        Optional<RegistryEntryList.Named<Enchantment>> optional = registry.getOptional(EnchantmentTags.IN_ENCHANTING_TABLE);
        if (optional.isEmpty()) {
            cir.setReturnValue(List.of());
            return;
        }

        Stream<RegistryEntry<Enchantment>> filtered = optional.get().stream().filter(entry -> {
            String ns = entry.getKey().map(key -> key.getValue().getNamespace()).orElse("minecraft");
            return gold ? ns.equals("vanilla_reforged") : ns.equals("minecraft");
        });

        List<EnchantmentLevelEntry> list = EnchantmentHelper.generateEnchantments(this.random, stack, level, filtered);
        if (stack.isOf(Items.BOOK) && list.size() > 1) {
            list.remove(this.random.nextInt(list.size()));
            while (list.size() > 1) {
                list.remove(list.size() - 1);
            }
        }
        cir.setReturnValue(list);
    }
}
