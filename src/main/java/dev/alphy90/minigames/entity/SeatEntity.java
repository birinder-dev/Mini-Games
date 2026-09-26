package dev.alphy90.minigames.entity;

import dev.alphy90.minigames.MiniGames;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SeatEntity extends Entity{

    public SeatEntity(EntityType<?> type, World world){
        super(type, world);
        this.noClip = true;
    }

    public SeatEntity(World world, BlockPos pos, double yOffset){
        this(MiniGames.SEAT_ENTITY, world);
        this.setPosition(pos.getX() + 0.5, pos.getY() + yOffset, pos.getZ() + 0.5);
    }

    @Override
    public void tick(){
        super.tick();
        if(!this.getWorld().isClient()){
            if(this.getPassengerList().isEmpty()){
                this.discard();
            }
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {}

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {}

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {}
}

