package dev.alphy90.minigames.block;

import dev.alphy90.minigames.entity.GamblerEntity;
import dev.alphy90.minigames.entity.SeatEntity;
import dev.alphy90.minigames.network.TablePlayingPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class GamblingStoolBlock extends Block{
    private static final VoxelShape SHAPE = Block.createCuboidShape(3.0,0.0,3.0,13.0,10.0,13.0);

    public GamblingStoolBlock(Settings settings){
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
        return SHAPE;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit){
        if(!world.isClient()){
            Box stoolBox = new Box(pos).expand(0.2);

            for(SeatEntity seat : world.getEntitiesByClass(SeatEntity.class, stoolBox, SeatEntity::hasPassengers)){
                if(seat.getFirstPassenger() instanceof GamblerEntity gambler){
                    gambler.dismountVehicle();
                    return ActionResult.SUCCESS;
                }
            }
            boolean occupied = !world.getEntitiesByClass(SeatEntity.class, stoolBox, SeatEntity::hasPassengers).isEmpty();
            if(occupied){
                return ActionResult.CONSUME;
            }

            //sit player down
            SeatEntity seat = new SeatEntity(world, pos, 0.45);
            world.spawnEntity(seat);
            player.startRiding(seat, true);

            //check position
            checkTableAndOpponent(world, pos, (ServerPlayerEntity) player);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved){
        if(!state.isOf(newState.getBlock())){
            Box stoolBox = new Box(pos).expand(0.2);
            for(SeatEntity seat : world.getEntitiesByClass(SeatEntity.class, stoolBox, net.minecraft.entity.Entity::isAlive)){
                seat.removeAllPassengers();
                seat.discard();
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    private void checkTableAndOpponent(World world, BlockPos stoolPos, ServerPlayerEntity player){
        BlockPos adjacentTablePos = null;
        Direction tableDirection = null;

        for(Direction dir : Direction.Type.HORIZONTAL){
            BlockPos checkPos = stoolPos.offset(dir);
            if(world.getBlockState(checkPos).getBlock() instanceof GamblingTableBlock){
                adjacentTablePos = checkPos;
                tableDirection = dir;
                break;
            }
        }

        //stool is cardinal
        if(adjacentTablePos != null){
            BlockPos oppositeStoolPos = adjacentTablePos.offset(tableDirection);
            boolean gamblerDirectlyOpposite = isGamblerSeatedAt(world, oppositeStoolPos);

            if(gamblerDirectlyOpposite){
                player.sendMessage(Text.literal("You sit across from the Gambler. Ready to play").formatted(Formatting.GREEN), true);
                ServerPlayNetworking.send(player, new TablePlayingPayload(true));
                return;
            }

            boolean gamblerSeatedElsewhereAtTable = false;
            for(Direction dir : Direction.Type.HORIZONTAL){
                BlockPos otherStool = adjacentTablePos.offset(dir);

                if(!otherStool.equals(stoolPos) && isGamblerSeatedAt(world, otherStool)){
                    gamblerSeatedElsewhereAtTable = true;
                    break;
                }
            }

            if(gamblerSeatedElsewhereAtTable){
                player.sendMessage(Text.literal("You must sit on the stool directly in front of the Gambler across the table to play!").formatted(Formatting.RED), true);
                ServerPlayNetworking.send(player, new TablePlayingPayload(false));
                return;
            }

            //gambler not at table
            player.sendMessage(Text.literal("Waiting for the Gambler to sit across the table...").formatted(Formatting.YELLOW), true);
            ServerPlayNetworking.send(player, new TablePlayingPayload(false));
            return;
        }

        //player is sitting diagnolly
        BlockPos diagonalTablePos = findNearbyTable(world, stoolPos);
        if(diagonalTablePos != null && hasAnyGamblerAtTable(world, diagonalTablePos)){
            player.sendMessage(Text.literal("You must sit on the stool directly in front of the Gambler across the table!").formatted(Formatting.RED), true);
        }
        ServerPlayNetworking.send(player, new TablePlayingPayload(false));
    }

    private boolean isGamblerSeatedAt(World world, BlockPos pos){
        if(world.getBlockState(pos).getBlock() instanceof GamblingStoolBlock){
            Box box = new Box(pos).expand(0.2);
            for(SeatEntity seat : world.getEntitiesByClass(SeatEntity.class, box, SeatEntity::hasPassengers)){
                if(seat.getFirstPassenger() instanceof GamblerEntity){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasAnyGamblerAtTable(World world, BlockPos tablePos){
        for(Direction dir : Direction.Type.HORIZONTAL){
            if(isGamblerSeatedAt(world, tablePos.offset(dir))) return true;
        }
        return false;
    }

    private BlockPos findNearbyTable(World world, BlockPos origin){
        for(int dx = -2; dx <= 2; dx++){
            for(int dz = -2; dz <= 2; dz++){
                BlockPos check = origin.add(dx, 0, dz);
                if(world.getBlockState(check).getBlock() instanceof GamblingTableBlock){
                    return check;
                }
            }
        }
        return null;
    }
}