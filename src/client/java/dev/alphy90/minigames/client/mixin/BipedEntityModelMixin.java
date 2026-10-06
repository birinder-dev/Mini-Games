package dev.alphy90.minigames.client.mixin;

import dev.alphy90.minigames.client.CardClientState;
import dev.alphy90.minigames.client.PlayerCardSyncState;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public class BipedEntityModelMixin<T extends LivingEntity> {
    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void applyCardPosing(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if(livingEntity instanceof PlayerEntity player){
            PlayerCardSyncState.PlayerAnim anim = PlayerCardSyncState.getAnim(player.getId());
            if(anim == null) return;

            float tickDelta = h - (float) livingEntity.age;
            float active = anim.getInterpolatedActive(tickDelta);
            if(active <= 0.001F) return;

            float charge = anim.getInterpolatedCharge(tickDelta);
            float flick = anim.getFlickProgress();


            //holding pose
            float idleRightPitch = -0.75F;
            float idleRightYaw = -0.28F;
            float idleRightRoll = 0.05F;

            float idleLeftPitch = -0.75F;
            float idleLeftYaw = 0.28F;
            float idleLeftRoll = -0.05F;

            //right arm
            float chargeRightPitch = -1.75F;
            float chargeRightYaw = -0.48F;
            float chargeRightRoll = 0.40F;

            //left arm
            float chargeLeftPitch = -1.20F;
            float chargeLeftYaw = 0.32F;
            float chargeLeftRoll = -0.15F;

            //subtle tension
            if(charge > 0.8F){
                float tension = (float) Math.sin(h * 2.5F) * 0.015F * charge;
                chargeRightPitch += tension;
                chargeRightYaw += tension * 0.5F;
            }

            float targetRightPitch = MathHelper.lerp(charge, idleRightPitch, chargeRightPitch);
            float targetRightYaw = MathHelper.lerp(charge, idleRightYaw, chargeRightYaw);
            float targetRightRoll = MathHelper.lerp(charge, idleRightRoll, chargeRightRoll);

            float targetLeftPitch = MathHelper.lerp(charge, idleLeftPitch, chargeLeftPitch);
            float targetLeftYaw = MathHelper.lerp(charge, idleLeftYaw, chargeLeftYaw);
            float targetLeftRoll = MathHelper.lerp(charge, idleLeftRoll, chargeLeftRoll);

            if(charge > 0.0F){
                targetRightPitch += this.head.pitch * charge;
                targetLeftPitch += this.head.pitch * charge;
            }

            //follow through flick snap
            if(flick > 0.0F){
                targetRightPitch = MathHelper.lerp(flick, -0.35F, targetRightPitch);
                targetRightYaw = MathHelper.lerp(flick, 0.40F, targetRightYaw);
                targetRightRoll = MathHelper.lerp(flick, -0.20F, targetRightRoll);
            }

            //apply arm rotation
            this.rightArm.pitch = MathHelper.lerp(active, this.rightArm.pitch, targetRightPitch);
            this.rightArm.yaw = MathHelper.lerp(active, this.rightArm.yaw, targetRightYaw);
            this.rightArm.roll = MathHelper.lerp(active, this.rightArm.roll, targetRightRoll);

            this.leftArm.pitch = MathHelper.lerp(active, this.leftArm.pitch, targetLeftPitch);
            this.leftArm.yaw = MathHelper.lerp(active, this.leftArm.yaw, targetLeftYaw);
            this.leftArm.roll = MathHelper.lerp(active, this.leftArm.roll, targetLeftRoll);
        }
    }
}