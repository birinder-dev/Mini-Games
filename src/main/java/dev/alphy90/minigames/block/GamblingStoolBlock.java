package dev.alphy90.minigames.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class GamblingStoolBlock extends Block{
    private static final VoxelShape SHAPE = Block.createCuboidShape(3.0,0.0,3.0,13.0,10.0,13.0);

    public GamblingStoolBlock(Settings settings){
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
        return SHAPE;
    }
}