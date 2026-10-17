package com.vanillareforged.sandbox.pocketrepose.block;

import com.vanillareforged.sandbox.pocketrepose.world.PocketReposeManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Exit portal block placed inside each pocket dimension sector.
 * Stepping onto or interacting with this block teleports the player back to their Overworld suitcase!
 */
public class PocketPortalBlock extends Block {

    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);

    public PocketPortalBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, net.minecraft.entity.EntityCollisionHandler handler, boolean booleanArg) {
        if (!world.isClient() && entity instanceof ServerPlayerEntity player) {
            if (PocketReposeManager.hasCooldown(player.getUuid())) {
                return;
            }
            PocketReposeManager.exitPocketDimension(player);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient() && player instanceof ServerPlayerEntity serverPlayer) {
            if (PocketReposeManager.hasCooldown(serverPlayer.getUuid())) {
                return ActionResult.PASS;
            }
            PocketReposeManager.exitPocketDimension(serverPlayer);
            return ActionResult.SUCCESS;
        }
        return ActionResult.SUCCESS;
    }
}
