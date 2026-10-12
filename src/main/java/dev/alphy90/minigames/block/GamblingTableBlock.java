package dev.alphy90.minigames.block;

import com.mojang.serialization.MapCodec;

import dev.alphy90.minigames.MiniGames;
import dev.alphy90.minigames.block.entity.GamblingTableBlockEntity;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;


public class GamblingTableBlock extends BlockWithEntity{
    public static final MapCodec<GamblingTableBlock> CODEC = createCodec(GamblingTableBlock::new);
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    //1*1 table
    private static final VoxelShape TABLETOP = Block.createCuboidShape(0.0, 13.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape LEG_NW = Block.createCuboidShape(1.0, 0.0, 1.0, 3.5, 13.0, 3.5);
    private static final VoxelShape LEG_NE = Block.createCuboidShape(12.5, 0.0, 1.0, 15.0, 13.0, 3.5);
    private static final VoxelShape LEG_SW = Block.createCuboidShape(1.0, 0.0, 12.5, 3.5, 13.0, 15.0);
    private static final VoxelShape LEG_SE = Block.createCuboidShape(12.5, 0.0, 12.5, 15.0, 13.0, 15.0);
    private static final VoxelShape SHAPE = VoxelShapes.union(TABLETOP, LEG_NW, LEG_NE, LEG_SW, LEG_SE);

    public GamblingTableBlock(Settings settings){
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec(){
        return CODEC;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
        return SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state){
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx){
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
        return new GamblingTableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
        return validateTicker(type, MiniGames.GAMBLING_TABLE_BLOCK_ENTITY, GamblingTableBlockEntity::tick);
    }
}