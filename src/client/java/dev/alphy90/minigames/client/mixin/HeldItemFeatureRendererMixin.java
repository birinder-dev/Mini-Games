package dev.alphy90.minigames.client.mixin;

import dev.alphy90.minigames.MiniGames;
import dev.alphy90.minigames.client.CardClientState;
import dev.alphy90.minigames.client.PlayerCardSyncState;

import dev.alphy90.minigames.entity.CardProjectileEntity;
import dev.alphy90.minigames.game.CardSuit;
import dev.alphy90.minigames.game.TavernCard;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.entity.feature.FeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(HeldItemFeatureRenderer.class)
public class HeldItemFeatureRendererMixin {

    @Unique
    private static final Identifier CARD_BACK_TEXTURE = Identifier.of(MiniGames.MOD_ID, "textures/item/tavern_card.png");

    @Unique
    private static final Identifier CARD_FRONT_TEXTURE = Identifier.of(MiniGames.MOD_ID, "textures/item/tavern_card_front.png");

    @Inject(
            method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;Lnet/minecraft/util/Arm;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void renderCustomCardHand(LivingEntity entity, ItemStack stack, ModelTransformationMode mode, Arm arm, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci){
        if(entity instanceof PlayerEntity player){
            boolean isLocal = (player == MinecraftClient.getInstance().player);
            boolean inCardState = isLocal ? (CardClientState.isActive() || CardClientState.isTablePlaying()) : PlayerCardSyncState.isPlayerInCardState(player);

            if(inCardState){
                if(arm == player.getMainArm()){
                    int count = isLocal ? CardClientState.getCardCount() : PlayerCardSyncState.getCardCount(player);
                    boolean isCharging = isLocal ? CardClientState.isThrowCharging() : PlayerCardSyncState.isPlayerCharging(player);

                    matrices.push();
                    if(((FeatureRenderer<?, ?>) (Object) this).getContextModel() instanceof ModelWithArms modelWithArms){
                        modelWithArms.setArmAngle(arm, matrices);
                    }
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));

                    boolean isLeft = (arm == Arm.LEFT);
                    matrices.translate((float) (isLeft ? -1 : 1) / 16.0F, 0.125F, -0.625F);

                    boolean isSeated = player.hasVehicle();
                    boolean showFront = isLocal || !isSeated;
                    renderThirdPersonCardHand(matrices, vertexConsumers, light, count, isCharging, isLocal, showFront);
                    matrices.pop();
                }
                ci.cancel();
            }
        }
    }

    @Unique
    private void renderThirdPersonCardHand(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int count, boolean isCharging, boolean isLocal, boolean showFront){
        matrices.push();
        matrices.translate(0.0F, 0.04F, 0.02F);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(15.0F));

        float cardW = 0.30F;
        float cardH = 0.40F;

        if(isCharging){
            // Positioned right in the player's raised hand (lowered into the palm/fingers)
            matrices.translate(0.04F, -0.14F, -0.02F);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-15.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(20.0F));
            TavernCard card = isLocal ? CardClientState.getCard(0) : null;
            renderCardQuad(matrices, vertexConsumers, light, cardW, cardH, card, showFront);
        } else {
            float cardSpacing = count <= 1 ? 0.0F : Math.min(0.05F, 0.22F / count);
            float anglestep = count <= 1 ? 0.0F : Math.min(7.0F, 28.0F / count);

            for(int i = 0; i < count; i++){
                float t = (float) (i - (count - 1) / 2.0F);
                matrices.push();
                matrices.translate(t * cardSpacing, -(t*t) * 0.003F, i * 0.003F);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-t * anglestep));
                TavernCard card = isLocal ? CardClientState.getCard(i) : null;
                renderCardQuad(matrices, vertexConsumers, light, cardW, cardH, card, showFront);
                matrices.pop();
            }
        }
        matrices.pop();
    }

    @Unique
    private void renderCardQuad(MatrixStack matrices, VertexConsumerProvider vertexConsumer, int light, float cardW, float cardH, TavernCard card, boolean showFront){
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f pos = entry.getPositionMatrix();
        float halfW = cardW / 2.0F;

        Identifier frontTex = (showFront && card != null) ? CARD_FRONT_TEXTURE : CARD_BACK_TEXTURE;
        VertexConsumer frontBuffer = vertexConsumer.getBuffer(RenderLayer.getEntityCutoutNoCull(frontTex));

        // Front face
        frontBuffer.vertex(pos, -halfW, 0.0F, 0.0F).color(255, 255, 255, 255).texture(0.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, 1.0F);
        frontBuffer.vertex(pos, halfW, 0.0F, 0.0F).color(255, 255, 255, 255).texture(1.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, 1.0F);
        frontBuffer.vertex(pos, halfW, cardH, 0.0F).color(255, 255, 255, 255).texture(1.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, 1.0F);
        frontBuffer.vertex(pos, -halfW, cardH, 0.0F).color(255, 255, 255, 255).texture(0.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, 1.0F);

        // Back face
        VertexConsumer backBuffer = vertexConsumer.getBuffer(RenderLayer.getEntityCutoutNoCull(CARD_BACK_TEXTURE));
        backBuffer.vertex(pos, halfW, 0.0F, -0.001F).color(255, 255, 255, 255).texture(1.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, -1.0F);
        backBuffer.vertex(pos, -halfW, 0.0F, -0.001F).color(255, 255, 255, 255).texture(0.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, -1.0F);
        backBuffer.vertex(pos, -halfW, cardH, -0.001F).color(255, 255, 255, 255).texture(0.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, -1.0F);
        backBuffer.vertex(pos, halfW, cardH, -0.001F).color(255, 255, 255, 255).texture(1.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, -1.0F);

        if(showFront && card != null){
            dev.alphy90.minigames.client.render.CardRenderHelper.renderCardFrontDetails(matrices, vertexConsumer, light, cardW, cardH, cardH / 2.0F, card);
        }
    }
}