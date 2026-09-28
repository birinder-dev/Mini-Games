package dev.alphy90.minigames.client.mixin;

import dev.alphy90.minigames.client.CardClientState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import dev.alphy90.minigames.entity.CardProjectileEntity;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.smartcardio.Card;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow public ClientPlayerEntity player;
    @Shadow @Nullable public Entity targetedEntity;
    @Shadow @Nullable public HitResult crosshairTarget;

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void lockAttackInCardMode(CallbackInfoReturnable<Boolean> cir){
        if (CardClientState.isActive()) {
            cir.setReturnValue(false);
            return;
        }

        if(this.crosshairTarget instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof CardProjectileEntity){
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void lockItemUseInCard(CallbackInfo ci){
        if (CardClientState.isActive()){
            ci.cancel();
        }
    }

    @Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
    private void lockBlockBreakingInCardMode(boolean breaking, CallbackInfo ci) {
        if (breaking && CardClientState.isActive()) ci.cancel();
    }
}

