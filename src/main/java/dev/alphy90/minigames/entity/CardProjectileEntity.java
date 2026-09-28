package dev.alphy90.minigames.entity;

import com.google.common.net.HostAndPort;
import dev.alphy90.minigames.MiniGames;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class CardProjectileEntity extends ArrowEntity {

    public CardProjectileEntity(EntityType<? extends CardProjectileEntity> type, World world){
        super(type, world);
        this.pickupType = PickupPermission.ALLOWED;
    }

    @Override
    public void tick(){
        super.tick();
    }

    public CardProjectileEntity(World world, LivingEntity owner, float power){
        super(MiniGames.CARD_PROJECTILE, world);
        this.setOwner(owner);
        this.setPosition(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
        this.pickupType = PickupPermission.ALLOWED;

        //charge .7 -> 2.5
        float speed = 0.7F + (power * 1.8F);
        this.setVelocity(owner, owner.getPitch(), owner.getYaw(), 0.0F, speed, 0.5F);
    }

    public boolean isInGround(){
        return this.inGround;
    }

    @Override
    public ItemStack asItemStack(){
        return new ItemStack(MiniGames.TAVERN_CARD);
    }

    @Override
    protected SoundEvent getHitSound(){
        return SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR;
    }

    @Override // for other entity, for now bounce off em without damage
    protected void onEntityHit(EntityHitResult entityHitResult){
        this.setVelocity(this.getVelocity().multiply(-0.2));
    }

    @Override
    public void onPlayerCollision(PlayerEntity player){

    }

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand){
        if(hand == Hand.MAIN_HAND && player.getStackInHand(hand).isEmpty() && this.inGround){
            if(!this.getWorld().isClient()){
                player.getInventory().offerOrDrop(this.asItemStack());
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.8F, 1.2F);
                this.discard();
            }
            return ActionResult.success(this.getWorld().isClient());
        }
        return super.interact(player, hand);
    }

    public static final int DESPAWN_TICKS = 6000;
    private int despawnTimer = 0;

    @Override
    protected void age(){
        this.despawnTimer++;
        if(this.despawnTimer >= DESPAWN_TICKS){
            this.discard();
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt){
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("DespawnTimer", this.despawnTimer);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt){
        super.readCustomDataFromNbt(nbt);
        this.despawnTimer = nbt.getInt("DespawnTimer");
    }
}