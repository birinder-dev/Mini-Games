package dev.alphy90.minigames.client.mixin;

import dev.alphy90.minigames.client.CardClientState;
import dev.alphy90.minigames.client.PlayerCardSyncState;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BipedEntityModel.class)
public class BipedEntityModelMixin<T extends LivingEntity> {
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void applyCardPosing(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci){
        if(livingEntity instanceof PlayerEntity player){
            boolean isLocal = (player == MinecraftClient.getInstance().player);
            boolean inCardState = isLocal ? CardClientState.isActive() : PlayerCardSyncState.isPlayerInCardState(player);

            if(inCardState){
                boolean isCharging = isLocal ? CardClientState.isThrowCharging() : PlayerCardSyncState.isPlayerCharging(player);

                if(isCharging){
                    this.rightArm.pitch = -1.55F;
                    this.rightArm.yaw = -0.35F;
                    this.rightArm.roll = 0.20F;
                } else {
                    this.rightArm.pitch = -0.75F;
                    this.rightArm.yaw = -0.28F;
                    this.rightArm.roll = 0.20F;

                    this.leftArm.pitch = -0.75F;
                    this.leftArm.yaw = 0.28F;
                    this.leftArm.roll = -0.05F;
                }
            }
        }
    }
}