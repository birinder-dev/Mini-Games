package dev.alphy90.minigames.client.mixin;

import dev.alphy90.minigames.MiniGames;
import dev.alphy90.minigames.client.CardClientState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Shadow
    protected abstract void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Arm arm);

    @Unique
    private static final Identifier CARD_TEXTURE = Identifier.of(MiniGames.MOD_ID, "textures/item/tavern_card.png");

    @Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At("HEAD"), cancellable = true)
    private void renderHandheldCards(float tickDelta, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, ClientPlayerEntity player, int light, CallbackInfo ci) {
        if (CardClientState.isActive()) {
            render3DCardHand(matrices, vertexConsumers, light, tickDelta, player);
            ci.cancel();
        }
    }

    @Unique
    private void render3DCardHand(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta, ClientPlayerEntity player) {
        int count = CardClientState.getCardCount();
        if (count <= 0) return;

        float pitch = player.getPitch(tickDelta);
        float pitchFactor = MathHelper.clamp(pitch / 90.0F, -1.0F, 1.0F);

        boolean isThrowing = CardClientState.isThrowCharging() || CardClientState.isFlicking();
        if(!player.isInvisible()){
            if(isThrowing){
                float charge = CardClientState.getChargeProgress();
                matrices.push();
                matrices.translate(0.04F, -0.24F + pitchFactor * -0.06F, -0.48F - (0.08F * charge));

                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(58.0F - (15.0F * charge)));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(78.0F));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-10.0F));

                matrices.scale(0.85F, 0.85F, 0.85F);
                this.renderArm(matrices, vertexConsumers, light, Arm.RIGHT);
                matrices.pop();
            }else {
                matrices.push();
                matrices.translate(0.0F, -0.32F + pitchFactor * -0.06F, -0.46F);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(64.0F));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
                matrices.scale(0.85F, 0.85F, 0.85F);
                this.renderArm(matrices, vertexConsumers, light,  Arm.RIGHT);
                this.renderArm(matrices, vertexConsumers, light, Arm.LEFT);
                matrices.pop();
            }
        }

        matrices.push();
        // Base position: negative X rotation pulls the LOWER edge toward your chest
        matrices.translate(0.0F, -0.36F + pitchFactor * -0.05F, -0.42F);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-20.0F)); // <-- Changed to negative to bring bottom toward you!

        float cardW = 0.33F;
        float cardH = 0.35F;
        float halfW = cardW / 2.0F;

        float anglestep;
        float cardSpacing;

        if (count == 1) {
            anglestep = 0.0F;
            cardSpacing = 0.0F;
        } else if (count == 2) {
            anglestep = 9.0F;
            cardSpacing = 0.095F;
        } else if (count <= 5) {
            anglestep = 7.0F;
            cardSpacing = 0.075F;
        } else {
            anglestep = Math.max(3.8F, 28.0F / count);
            cardSpacing = Math.max(0.040F, 0.070F - (count - 5) * 0.004F);
        }

        int selectedSlot = player.getInventory().selectedSlot;
        int blockLight = Math.max(12, light & 0xFFFF);
        int skyLight = (light >> 16) & 0xFFFF;
        int cardLight = (skyLight << 16) | blockLight;

        for (int i = 0; i < count; i++) {

            int selectedCardIndex = CardClientState.getCardIndexForSlot(selectedSlot, count);
            boolean isSelected = (i == selectedCardIndex);

            if(isThrowing && !isSelected){
                continue;
            }

            // t = 0 for the center card; negative for left, positive for right
            float t = (float) (i - (count - 1) / 2.0F);
            float cardX = t * cardSpacing;
            float angleZ = -t * anglestep;
            // Parabolic curve: center card stays highest, outer wings dip slightly
            float arcY = -(t * t) * 0.005F;
            // POSITIVE: gives dominance to the right card (higher i sits in front)
            float arcZ = i * 0.012F;
            float cardPivotY = arcY;

            if(isSelected && isThrowing){

                cardX = 0.12F;
                cardPivotY = 0.01F;
                arcZ = -0.04F;

                if(CardClientState.isThrowCharging()){
                    float charge = CardClientState.getChargeProgress();
                    cardPivotY -= 0.04F * charge;
                    arcZ -= 0.08F * charge;

                    float shake = (float)Math.sin((player.age + tickDelta) * 2.5F) * (0.0012F + 0.0022F * charge);
                    cardX += shake;
                    cardPivotY += shake * 0.5F;
                }

                if(CardClientState.isFlicking()){
                    float flickProgress = 1.0F - ((float) CardClientState.getFlickTicks() / (float) CardClientState.TOTAL_FLICK_TICKS);
                    cardX -= 0.10F * flickProgress;
                    cardPivotY += 0.08F * flickProgress;
                    arcZ += 0.35F * flickProgress;
                }
            } else if (isSelected) {
                cardPivotY += 0.045F;
                arcZ = (count * 0.012F) + 0.030F;
                
            }
            matrices.push();
            matrices.translate(cardX, cardPivotY, arcZ);

            if(isSelected && isThrowing){
                if(CardClientState.isThrowCharging()){
                    float charge = CardClientState.getChargeProgress();
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-15.0F));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-25.0F * charge));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(45.0F));

                    matrices.translate(-halfW * 0.85F, -0.06F, 0.0F);
                } else if (CardClientState.isFlicking()) {
                    float flickProgress = 1.0F - ((float) CardClientState.getFlickTicks() / (float)CardClientState.TOTAL_FLICK_TICKS);

                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(42.0F * flickProgress));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-25.0F * flickProgress));
                    matrices.translate(-halfW * 0.85F, -0.06F, 0.0F);
                }
            } else {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angleZ));
            }

            renderCardQuad(matrices, vertexConsumers, cardLight, cardW, cardH, isSelected);
            matrices.pop();
        }

        matrices.pop();

        if (vertexConsumers instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw();
        }
    }

    @Unique
    private void renderCardQuad(MatrixStack matrices, VertexConsumerProvider vertexConsumer, int light, float cardW, float cardH, boolean isSelected) {
        VertexConsumer buffer = vertexConsumer.getBuffer(RenderLayer.getEntityCutoutNoCull(CARD_TEXTURE));
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f pos = entry.getPositionMatrix();
        float halfW = cardW / 2.0F;

        int r = isSelected ? 255 : 240;
        int g = isSelected ? 245 : 240;
        int b = isSelected ? 210 : 240;

        // Front face (facing camera)
        buffer.vertex(pos, -halfW, 0.0F, 0.0F).color(r, g, b, 255).texture(0.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, 1.0F);
        buffer.vertex(pos, halfW, 0.0F, 0.0F).color(r, g, b, 255).texture(1.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, 1.0F);
        buffer.vertex(pos, halfW, cardH, 0.0F).color(r, g, b, 255).texture(1.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, 1.0F);
        buffer.vertex(pos, -halfW, cardH, 0.0F).color(r, g, b, 255).texture(0.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, 1.0F);

        // Back face
        buffer.vertex(pos, halfW, 0.0F, -0.001F).color(r, g, b, 255).texture(1.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, -1.0F);
        buffer.vertex(pos, -halfW, 0.0F, -0.001F).color(r, g, b, 255).texture(0.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, -1.0F);
        buffer.vertex(pos, -halfW, cardH, -0.001F).color(r, g, b, 255).texture(0.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, -1.0F);
        buffer.vertex(pos, halfW, cardH, -0.001F).color(r, g, b, 255).texture(1.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 0.0F, -1.0F);
    }
}