package dev.alphy90.minigames.client.mixin;

import dev.alphy90.minigames.client.CardClientState;
import dev.alphy90.minigames.MiniGames;
import dev.alphy90.minigames.entity.SeatEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

import org.apache.commons.compress.harmony.pack200.NewAttributeBands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow
    private int overlayRemaining;

    //disable vanilla sitting message while sitting
    @Inject(method = "setOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void suppressSeatDismountMessage(Text message, boolean tinted, CallbackInfo ci){
        MinecraftClient client = MinecraftClient.getInstance();
        if(client.player != null && client.player.getVehicle() instanceof SeatEntity){
            if(message.getContent() instanceof TranslatableTextContent translatable && "mount.onboard".equals(translatable.getKey())){
                ci.cancel();
            }
        }
    }

    //extend stool msg to stay for 4 sec
    @Inject(method = "setOverlayMessage", at = @At("TAIL"))
    private void extendSeatOverlayDuration(Text message, boolean tinted, CallbackInfo ci){
        MinecraftClient client = MinecraftClient.getInstance();
        if(client.player != null && client.player.getVehicle() instanceof SeatEntity){
            this.overlayRemaining = 80;
        }
    }

    @Inject(method = "renderHeldItemTooltip", at = @At("HEAD"), cancellable = true)
    private void hideHeldItemTooltip(DrawContext context, CallbackInfo ci){
        if(CardClientState.isActive() || CardClientState.isTablePlaying()) ci.cancel();
    }


    @Unique
    private static final ItemStack VIRTUAL_CARD_STACK = new ItemStack(MiniGames.TAVERN_CARD);

    @Inject(
            method = "renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void renderVirtualCardsInHotbar(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci){
        if(CardClientState.isTablePlaying()){
            ci.cancel();
            return;
        }

        if(!CardClientState.isActive()){
            return;
        }

        int hotbarStartX = context.getScaledWindowWidth() / 2-90;
        int slotIndex = (x - hotbarStartX - 2) / 20;

        if(CardClientState.isCardSlot(slotIndex)){
            context.drawItem(player, VIRTUAL_CARD_STACK, x, y, seed);
            ci.cancel();
        } else {
            // empty non card slots when its on
            ci.cancel();
        }
    }
}