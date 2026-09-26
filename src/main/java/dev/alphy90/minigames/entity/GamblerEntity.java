package dev.alphy90.minigames.entity;

import dev.alphy90.minigames.entity.ai.GamblerSeekStoolGoal;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;

import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;

import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class GamblerEntity extends PathAwareEntity {
    public GamblerEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createGamblerAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new GamblerSeekStoolGoal(this));
        this.goalSelector.add(2, new WanderAroundFarGoal(this, 0.6));
        this.goalSelector.add(3, new LookAroundGoal(this));
    }

    /*@Override
    public void tick(){
        super.tick();;
        // player comes near, this unsits itself
        if(!this.getWorld().isClient() && this.hasVehicle()){
            PlayerEntity player = this.getWorld().getClosestPlayer(this, 2.5);
            if(player != null && !player.isSpectator() && !player.isCreative()){
                this.dismountVehicle();
            }
        }

     */

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand){
        // right click to make em unsit
        if(this.hasVehicle()){
            if(!this.getWorld().isClient()){
                this.dismountVehicle();
            }
            return ActionResult.success(this.getWorld().isClient());
        }
        return super.interactMob(player, hand);
    }

}


