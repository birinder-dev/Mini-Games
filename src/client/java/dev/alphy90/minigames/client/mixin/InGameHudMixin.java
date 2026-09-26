package dev.alphy90.minigames.client.mixin;

import dev.alphy90.minigames.client.CardClientState;
import dev.alphy90.minigames.MiniGames;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "renderHotbarItem", at = @At("HEAD"), cancellable = true)
    private void hideHotbarItems(DrawContext context, int x, int y, RenderTickCounter tickCounter,
                                 PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci){
        if(CardClientState.isActive()) ci.cancel();
    }

    @Inject(method = "renderHeldItemTooltip", at = @At("HEAD"), cancellable = true)
    private void hideHeldItemTooltip(DrawContext context, CallbackInfo ci){
        if(CardClientState.isActive()) ci.cancel();
    }

    @Shadow @Final private MinecraftClient client;

    @Unique
    private static final ItemStack VIRTUAL_CARD_STACK = new ItemStack(MiniGames.TAVERN_CARD);

    @Inject(
            method = "renderHotbarItem(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void renderVirtualCardsInHotbar(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci){
        if(!CardClientState.isActive()){
            return;
        }

        int hotbarStartX = context.getScaledWindowWidth() / 2-90;
        int slotIndex = (x - hotbarStartX - 2) / 20;

        if(CardClientState.isCardSlot(slotIndex)){
            context.drawItem(player, VIRTUAL_CARD_STACK, x, y, seed);
            context.drawStackOverlay(this.client.textRenderer, VIRTUAL_CARD_STACK, x, y);
            ci.cancel();
        } else {
            // empty non card slots when its on
            ci.cancel();
        }
    }
}