package dev.alphy90.minigames.entity;

import dev.alphy90.minigames.MiniGames;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class CardProjectileEntity extends ArrowEntity {

    public CardProjectileEntity(EntityType<? extends CardProjectileEntity> type, World world){
        super(type, world);
        this.pickupType = PickupPermission.ALLOWED;
    }

    public CardProjectileEntity(World world, LivingEntity owner, float power){
        super(MiniGames.CARD_PROJECTILE, world);
        this.setOwner(owner);
        this.setPosition(owner.getX(), owner.getY() - 0.1, owner.getZ());
        this.pickupType = PickupPermission.ALLOWED;

        //charge .7 -> 2.5
        float speed = 0.7F + (power * 1.8F);
        this.setVelocity(owner, owner.getPitch(), owner.getYaw(), 0.0F, speed, 0.5F);
    }

    @Override
    public ItemStack asItemStack(){
        return new ItemStack(MiniGames.TAVERN_CARD);
    }

    @Override
    protected SoundEvent getHitSound(){
        return SoundEvents.ENTITY_ARROW_HIT;
    }

    @Override // for other entity, for now bounce off em without damage
    protected void onEntityHit(EntityHitResult entityHitResult){
        this.setVelocity(this.getVelocity().multiply(-0.2));
    }
}