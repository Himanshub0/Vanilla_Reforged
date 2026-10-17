package com.vanillareforged.sandbox.pocketrepose.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class CustomSuitcaseBlock extends SuitcaseBlock {

    public static final MapCodec<CustomSuitcaseBlock> CODEC = createCodec(CustomSuitcaseBlock::new);
    private static final VoxelShape BARREL_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    public CustomSuitcaseBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<CustomSuitcaseBlock> getCodec() {
        return CODEC;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return BARREL_SHAPE;
    }

    @Override
    protected SoundEvent getOpenSound() {
        return SoundEvents.BLOCK_BARREL_OPEN;
    }

    @Override
    protected SoundEvent getCloseSound() {
        return SoundEvents.BLOCK_BARREL_CLOSE;
    }
}
