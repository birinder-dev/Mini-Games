package dev.alphy90.minigames.block.entity;

import dev.alphy90.minigames.MiniGames;
import dev.alphy90.minigames.block.GamblingStoolBlock;
import dev.alphy90.minigames.entity.GamblerEntity;
import dev.alphy90.minigames.entity.SeatEntity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtException;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GamblingTableBlockEntity extends BlockEntity{
    private boolean hasCards = false;
    private int checkCooldown = 0;

    public GamblingTableBlockEntity(BlockPos pos, BlockState state){
        super(MiniGames.GAMBLING_TABLE_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, GamblingTableBlockEntity be){
        if(world.isClient()) return;

        if(be.checkCooldown-- <= 0){
            be.checkCooldown = 10;
            boolean gamblerSeated = be.checkCardinalsForSeatedGambler(world, pos);
            if(gamblerSeated != be.hasCards){
                be.hasCards = gamblerSeated;
                be.markDirty();
                world.updateListeners(pos, state, state, 3);
            }
        }
    }

    private boolean checkCardinalsForSeatedGambler(World world, BlockPos tablePos){
        for(Direction dir : Direction.Type.HORIZONTAL){
            BlockPos stoolPos = tablePos.offset(dir);
            if(world.getBlockState(stoolPos).getBlock() instanceof GamblingStoolBlock){
                Box box = new Box(stoolPos).expand(0.2);
                for (SeatEntity seat : world.getEntitiesByClass(SeatEntity.class, box, SeatEntity::hasPassengers)){
                    if(seat.getFirstPassenger() instanceof GamblerEntity){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isHasCards(){
        return hasCards;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries){
        super.writeNbt(nbt, registries);
        nbt.putBoolean("HasCards", hasCards);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries){
        super.readNbt(nbt, registries);
        hasCards = nbt.getBoolean("HasCards");
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket(){
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries){
        return createNbt(registries);
    }
}