package dev.alphy90.minigames.client.mixin;

import dev.alphy90.minigames.client.CardClientState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow public ClientPlayerEntity player;

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void lockAttackInCardMode(CallbackInfoReturnable<Boolean> cir){
        if (CardClientState.isActive()) cir.setReturnValue(false);
    }

    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void lockItemUseInCard(CallbackInfo ci){
        if (CardClientState.isActive()){
            if(this.player != null){
                //sound
                this.player.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0F, 1.2F);
                //card removed
                CardClientState.playSelectedCard(this.player.getInventory().selectedSlot);
            }
            ci.cancel();
        }
    }

    @Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
    private void lockBlockBreakingInCardMode(boolean breaking, CallbackInfo ci) {
        if (breaking && CardClientState.isActive()) ci.cancel();
    }
}

