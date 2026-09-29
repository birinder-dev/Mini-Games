package dev.alphy90.minigames.client.mixin;

import dev.alphy90.minigames.client.CardClientState;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin{

    @Inject(method = "getFovMultiplier", at = @At("RETURN"), cancellable = true)
    private void applyCardThrowFoc(CallbackInfoReturnable<Float> cir){
        if(CardClientState.isActive() && CardClientState.isThrowCharging()){
            float progress = CardClientState.getChargeProgress();
            float fovZoom = 1.0F - (progress * 0.12F);
            cir.setReturnValue(cir.getReturnValue() * fovZoom);
        }
    }
}