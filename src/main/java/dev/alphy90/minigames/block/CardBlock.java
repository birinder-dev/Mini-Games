package dev.alphy90.minigames.block;

import dev.alphy90.minigames.MiniGames;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;

public class CardBlock extends Block{
    public static final DirectionProperty FACING = Properties.FACING;

    private static final VoxelShape SHAPE_UP = Block.createCuboidShape(3.0, 0.0, 1.0, 13.0, 0.8, 15.0);
    private static final VoxelShape SHAPE_DOWN = Block.createCuboidShape(3.0, 15.2, 1.0, 13.0, 16.0, 15.0);
    private static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(3.0, 1.0, 15.2, 13.0, 15.0, 16.0);
    private static final VoxelShape SHAPE_SOUTH = Block.createCuboidShape(3.0, 1.0, 0.0, 13.0, 15.0, 0.8);
    private static final VoxelShape SHAPE_WEST = Block.createCuboidShape(15.2, 1.0, 3.0, 16.0, 15.0, 13.0);
    private static final VoxelShape SHAPE_EAST = Block.createCuboidShape(0.0, 1.0, 3.0, 0.8, 15.0, 13.0);

    public CardBlock(Settings settings){
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.UP));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
        builder.add(FACING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
        return switch (state.get(FACING)){
            case UP -> SHAPE_UP;
            case DOWN -> SHAPE_DOWN;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
        };
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos){
     Direction facing = state.get(FACING);
     BlockPos supportPos = pos.offset(facing.getOpposite());
     BlockState supportState = world.getBlockState(supportPos);
     return !supportState.isAir() && !supportState.getCollisionShape(world, supportPos).isEmpty();
    }

    //breaking like a flower
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos){
        if(direction == state.get(FACING).getOpposite() && !state.canPlaceAt(world, pos)){
            if(world instanceof World actualWorld && !actualWorld.isClient()){
                dropStack(actualWorld, pos, new ItemStack(MiniGames.TAVERN_CARD));
            }
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    // punching w left click
    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player){
        if(!world.isClient() && !player.isCreative() && !player.isSpectator()){
            dropStack(world, pos, new ItemStack(MiniGames.TAVERN_CARD));
        }
        return super.onBreak(world, pos, state, player);
    }

    //right click to pick it up
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit){
        if(player.getMainHandStack().isEmpty()){
            if(!world.isClient()){
                player.getInventory().offerOrDrop(new ItemStack(MiniGames.TAVERN_CARD));
                world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.7F, 1.2F);
                world.removeBlock(pos, false);
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}