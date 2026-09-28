package dev.alphy90.minigames.entity.ai;

import dev.alphy90.minigames.entity.GamblerEntity;
import dev.alphy90.minigames.entity.SeatEntity;
import dev.alphy90.minigames.block.GamblingStoolBlock;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.EnumSet;

public class GamblerSeekStoolGoal extends Goal {
    private final GamblerEntity gambler;
    private BlockPos targetStoolPos = null;
    private int cooldown = 0;

    public GamblerSeekStoolGoal(GamblerEntity gambler) {
        this.gambler = gambler;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart(){
        if(this.gambler.hasVehicle()) return false;
        if (this.cooldown-- > 0) return false;
        this.cooldown = 20;

        this.targetStoolPos = findNearestStool();
        return this.targetStoolPos != null;
    }

    @Override
    public boolean shouldContinue(){
        return !this.gambler.hasVehicle() && targetStoolPos != null && !isSeatOccupied(targetStoolPos);
    }

    @Override
    public void start() {
        if (targetStoolPos != null) {
            this.gambler.getNavigation().startMovingTo(
                    targetStoolPos.getX() + 0.5,
                    targetStoolPos.getY(),
                    targetStoolPos.getZ() + 0.5,
                    1.0
                    );
        }
    }

    @Override
    public void tick() {
        if(targetStoolPos == null) return;

        if(isSeatOccupied(targetStoolPos)) {
            this.targetStoolPos = null;
            return;
        }

        this.gambler.getLookControl().lookAt(
                targetStoolPos.getX() + 0.5,
                targetStoolPos.getY() + 0.5,
                targetStoolPos.getZ() + 0.5
        );

        //sitting down finally
        if(this.gambler.getBlockPos().isWithinDistance(targetStoolPos, 1.8)){
            World world = this.gambler.getWorld();
            if(!world.isClient()){
                SeatEntity seat = new SeatEntity(world, targetStoolPos, 0.45);
                world.spawnEntity(seat);
                this.gambler.startRiding(seat, true);
            }
            this.targetStoolPos = null;
        }
    }

    //position of nearest stool available
    private BlockPos findNearestStool(){
        World world = this.gambler.getWorld();
        BlockPos origin = this.gambler.getBlockPos();
        int radius = 5;
        BlockPos nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for(BlockPos pos : BlockPos.iterate(origin.add(-radius, -2, -radius), origin.add(radius, 2, radius))){
            if(world.getBlockState(pos).getBlock() instanceof GamblingStoolBlock && !isSeatOccupied(pos)){
                double dist = origin.getSquaredDistance(pos);
                if(dist < nearestDistSq){
                    nearestDistSq = dist;
                    nearest = pos.toImmutable();
                }
            }
        }
        return nearest;
    }

    private boolean isSeatOccupied(BlockPos pos){
        Box box = new Box(pos).expand(0.2);
        return !this.gambler.getWorld().getEntitiesByClass(SeatEntity.class, box, SeatEntity::hasPassengers).isEmpty();
    }
}