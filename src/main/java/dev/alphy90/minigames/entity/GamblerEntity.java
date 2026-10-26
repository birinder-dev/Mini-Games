package dev.alphy90.minigames.entity;

import dev.alphy90.minigames.entity.ai.GamblerSeekStoolGoal;

import net.minecraft.block.Block;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;

public class GamblerEntity extends PathAwareEntity {
    public GamblerEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    private static final TrackedData<Integer> LEFT_CARDS = DataTracker.registerData(GamblerEntity.class, TrackedDataHandlerRegistry.INTEGER);

    @Override
    protected void initDataTracker(DataTracker.Builder builder){
        super.initDataTracker(builder);
        builder.add(LEFT_CARDS, 5);
    }

    public int getLeftCards(){
        return this.dataTracker.get(LEFT_CARDS);
    }

    public void setLeftCards(int count){
        this.dataTracker.set(LEFT_CARDS, Math.max(0, count));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt){
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("LeftCards", this.getLeftCards());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt){
        super.readCustomDataFromNbt(nbt);
        if(nbt.contains("LeftCards")){
            this.setLeftCards(nbt.getInt("LeftCards"));
        }
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
        if(this.hasVehicle()){
            if(this.getWorld().isClient()){
                return ActionResult.SUCCESS;
            }
            if(player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer){
                //while sneaking
                if(player.isSneaking()){
                    this.dismountVehicle();
                    return ActionResult.SUCCESS;
                }

                BlockPos stoolPos = this.getVehicle() != null ? this.getVehicle().getBlockPos() : this.getBlockPos();
                for(net.minecraft.util.math.Direction dir : Direction.Type.HORIZONTAL){
                    BlockPos tablePos = stoolPos.offset(dir);
                    if(this.getWorld().getBlockEntity(tablePos) instanceof dev.alphy90.minigames.block.entity.GamblingTableBlockEntity tableBe){
                        if(tableBe.areGameConditionsMet((net.minecraft.server.world.ServerWorld)this.getWorld(), serverPlayer) && !tableBe.isGameActive()){
                            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(serverPlayer, new dev.alphy90.minigames.network.OpenTableScreenPayload(tablePos));
                            return ActionResult.SUCCESS;
                        }
                    }
                }
            }
            return ActionResult.CONSUME;
        }
        return super.interactMob(player, hand);
    }

    @Override
    public net.minecraft.util.math.Vec3d getVehicleAttachmentPos(net.minecraft.entity.Entity vehicle) {
        if (vehicle instanceof SeatEntity) {
            return new net.minecraft.util.math.Vec3d(0.0, 0.6, 0.0);
        }
        return super.getVehicleAttachmentPos(vehicle);
    }
}