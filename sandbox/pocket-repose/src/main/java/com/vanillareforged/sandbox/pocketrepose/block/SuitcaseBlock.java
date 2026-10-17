package com.vanillareforged.sandbox.pocketrepose.block;

import com.mojang.serialization.MapCodec;
import com.vanillareforged.sandbox.pocketrepose.block.entity.SuitcaseBlockEntity;
import com.vanillareforged.sandbox.pocketrepose.item.KeystoneItem;
import com.vanillareforged.sandbox.pocketrepose.item.ModItems;
import com.vanillareforged.sandbox.pocketrepose.world.PocketReposeManager;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SuitcaseBlock extends BlockWithEntity {

    public static final MapCodec<SuitcaseBlock> CODEC = createCodec(SuitcaseBlock::new);
    public static final BooleanProperty OPEN = BooleanProperty.of("open");
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE_NS = Block.createCuboidShape(0.0, 0.0, 2.0, 16.0, 4.0, 14.0);
    private static final VoxelShape SHAPE_EW = Block.createCuboidShape(2.0, 0.0, 0.0, 14.0, 4.0, 16.0);

    public SuitcaseBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
            .with(OPEN, false)
            .with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(OPEN, FACING);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()
            .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
            .with(OPEN, false);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction dir = state.get(FACING);
        return (dir == Direction.NORTH || dir == Direction.SOUTH) ? SHAPE_NS : SHAPE_EW;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SuitcaseBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient()) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof SuitcaseBlockEntity suitcase) {
                NbtComponent customData = itemStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
                if (!customData.isEmpty()) {
                    NbtCompound nbt = customData.copyNbt();
                    if (nbt.contains("BoundKeystone")) {
                        suitcase.bindKeystone(nbt.getString("BoundKeystone").orElse(""));
                    }
                    if (nbt.contains("Locked")) {
                        suitcase.setLocked(nbt.getBoolean("Locked").orElse(false));
                    }
                    if (nbt.contains("EnteredPlayers")) {
                        String playersStr = nbt.getString("EnteredPlayers").orElse("");
                        for (String token : playersStr.split(",")) {
                            try {
                                suitcase.addEnteredPlayer(UUID.fromString(token.trim()));
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
                if (!suitcase.getBoundKeystoneName().isEmpty()) {
                    PocketReposeManager.registerSuitcase(suitcase.getBoundKeystoneName(), world.getRegistryKey(), pos);
                }
            }
        }
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient() && !player.isCreative()) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof SuitcaseBlockEntity suitcase) {
                ItemStack dropStack = createSuitcaseStack(suitcase, this.asItem());
                ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, dropStack);
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
            }
        }
        return super.onBreak(world, pos, state, player);
    }

    public static ItemStack createSuitcaseStack(SuitcaseBlockEntity suitcase) {
        return createSuitcaseStack(suitcase, ModItems.SUITCASE);
    }

    public static ItemStack createSuitcaseStack(SuitcaseBlockEntity suitcase, net.minecraft.item.ItemConvertible item) {
        ItemStack stack = new ItemStack(item);
        String keystone = suitcase.getBoundKeystoneName();
        boolean locked = suitcase.isLocked();
        List<UUID> enteredPlayers = suitcase.getEnteredPlayers();

        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> {
            if (!keystone.isEmpty()) {
                nbt.putString("BoundKeystone", keystone);
            }
            nbt.putBoolean("Locked", locked);
            if (!enteredPlayers.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < enteredPlayers.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(enteredPlayers.get(i).toString());
                }
                nbt.putString("EnteredPlayers", sb.toString());
            }
        });

        List<Text> lore = new ArrayList<>();
        if (!enteredPlayers.isEmpty()) {
            lore.add(Text.literal("§c⚠ Contains " + enteredPlayers.size() + " Traveler(s)!").formatted(Formatting.RED));
        }
        if (!keystone.isEmpty()) {
            lore.add(Text.literal("§7Bound to: §e" + keystone.replace("_", " ")).formatted(Formatting.GRAY));
            lore.add(Text.literal(locked ? "§cLocked" : "§aUnlocked").formatted(Formatting.GRAY));
        } else {
            lore.add(Text.literal("§7Unbound").formatted(Formatting.GRAY));
        }
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        return stack;
    }

    protected net.minecraft.sound.SoundEvent getOpenSound() {
        return SoundEvents.BLOCK_CHEST_LOCKED;
    }

    protected net.minecraft.sound.SoundEvent getCloseSound() {
        return SoundEvents.BLOCK_BAMBOO_WOOD_TRAPDOOR_CLOSE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof SuitcaseBlockEntity suitcase)) {
            return ActionResult.PASS;
        }

        ItemStack held = player.getStackInHand(player.getActiveHand());

        // 1. Keystone Interactions
        if (held.getItem() instanceof KeystoneItem) {
            String keyName = KeystoneItem.getSanitizedKeystoneName(held);
            if (keyName.isEmpty()) {
                player.sendMessage(Text.literal("§cRename the key in an anvil first to bind it!"), true);
                world.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 0.5f, 2.0f);
                return ActionResult.FAIL;
            }

            String currentBound = suitcase.getBoundKeystoneName();

            // Toggle Lock if already bound to this key
            if (!currentBound.isEmpty() && currentBound.equals(keyName)) {
                boolean newLock = !suitcase.isLocked();
                suitcase.setLocked(newLock);
                world.playSound(null, pos, newLock ? SoundEvents.BLOCK_IRON_DOOR_CLOSE : SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 0.5f, 1.8f);
                player.sendMessage(Text.literal(newLock ? "§cSuitcase Locked" : "§aSuitcase Unlocked"), true);
                return ActionResult.SUCCESS;
            }

            // Prevent binding if locked
            if (suitcase.isLocked()) {
                world.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 0.5f, 2.0f);
                player.sendMessage(Text.literal("§cSuitcase is locked!"), true);
                return ActionResult.FAIL;
            }

            // Bind suitcase to this key
            suitcase.bindKeystone(keyName);
            PocketReposeManager.registerSuitcase(keyName, world.getRegistryKey(), pos);
            world.playSound(null, pos, SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.BLOCKS, 1.5f, 1.0f);
            player.sendMessage(Text.literal("§aBound suitcase to dimension: §e" + keyName), true);
            return ActionResult.SUCCESS;
        }

        // 2. Open / Close Suitcase
        if (suitcase.isLocked()) {
            world.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.BLOCKS, 0.5f, 2.0f);
            player.sendMessage(Text.literal("§cSuitcase is locked!"), true);
            return ActionResult.FAIL;
        }

        if (suitcase.getBoundKeystoneName().isEmpty()) {
            world.playSound(null, pos, SoundEvents.BLOCK_CHAIN_PLACE, SoundCategory.BLOCKS, 0.5f, 1.8f);
            player.sendMessage(Text.literal("§eRight-click with a named Traveler's Key to bind this suitcase."), true);
            return ActionResult.FAIL;
        }

        boolean isOpen = state.get(OPEN);
        world.setBlockState(pos, state.with(OPEN, !isOpen));
        if (!isOpen) {
            world.playSound(null, pos, getOpenSound(), SoundCategory.BLOCKS, 0.4f, 1.8f);
            world.playSound(null, pos, SoundEvents.BLOCK_LADDER_STEP, SoundCategory.BLOCKS, 0.3f, 1.0f);
        } else {
            world.playSound(null, pos, getCloseSound(), SoundCategory.BLOCKS, 0.4f, 1.0f);
            world.playSound(null, pos, SoundEvents.BLOCK_LADDER_BREAK, SoundCategory.BLOCKS, 0.3f, 1.0f);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, net.minecraft.entity.EntityCollisionHandler handler, boolean booleanArg) {
        if (!world.isClient() && entity instanceof ServerPlayerEntity player) {
            if (PocketReposeManager.hasCooldown(player.getUuid())) {
                return;
            }
            // Must be open and player must sneak to enter
            if (state.get(OPEN) && player.isSneaking()) {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof SuitcaseBlockEntity suitcase && !suitcase.getBoundKeystoneName().isEmpty() && !suitcase.isLocked()) {
                    PocketReposeManager.enterPocketDimension(player, pos, suitcase);
                }
            }
        }
    }
}
