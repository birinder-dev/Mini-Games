package dev.alphy90.minigames.mixin;

import dev.alphy90.minigames.MiniGames;
import dev.alphy90.minigames.config.ModConfig;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin{

    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    private void setCustomCardStackSize(CallbackInfoReturnable<Integer> cir){
        if(((ItemStack) (Object)this).isOf(MiniGames.TAVERN_CARD)){
            cir.setReturnValue(Math.min(9, Math.max(1, ModConfig.get().maxCardsInHand)));
        }
    }
}