package dev.alphy90.minigames.client.mixin;

import dev.alphy90.minigames.MiniGames;
import dev.alphy90.minigames.client.CardClientState;
import dev.alphy90.minigames.client.PlayerCardSyncState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HeldItemFeatureRenderer.class)
public class HeldItemFeatureRendererMixin {
    @ModifyVariable(
            method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;Lnet/minecraft/util/Arm;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private ItemStack overrideHeldItemForCardState(ItemStack stack, LivingEntity entity, ItemStack originalStack, ModelTransformationMode mode, Arm arm){
        if(entity instanceof PlayerEntity player){
            boolean inCardState = (player == MinecraftClient.getInstance().player) ? CardClientState.isActive() : PlayerCardSyncState.isPlayerInCardState(player);

            if(inCardState){
                //render Card
                if(arm == player.getMainArm()){
                    return MiniGames.TAVERN_CARD.getDefaultStack();
                } else {
                    //suppress offhand items too
                    return ItemStack.EMPTY;
                }
            }
        }
        return stack;
    }
}