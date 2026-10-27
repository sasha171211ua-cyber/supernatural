package com.supernatural.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

/** Простий декоративний блок з власною формою (надгробки, урна). */
public class DecorBlock extends Block {
    public static final MapCodec<DecorBlock> CODEC = createCodec(DecorBlock::new);
    private final VoxelShape shape;

    public DecorBlock(Settings settings) {
        this(settings, VoxelShapes.fullCube());
    }

    public DecorBlock(Settings settings, VoxelShape shape) {
        super(settings);
        this.shape = shape;
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return shape;
    }
}
