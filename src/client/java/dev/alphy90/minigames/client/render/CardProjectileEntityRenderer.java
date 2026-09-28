package dev.alphy90.minigames.client.render;

import dev.alphy90.minigames.MiniGames;
import dev.alphy90.minigames.entity.CardProjectileEntity;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.Identifier;

import org.joml.Matrix4f;

public class CardProjectileEntityRenderer extends EntityRenderer<CardProjectileEntity> {
    private static final Identifier TEXTURE = MiniGames.id("textures/item/tavern_card.png");

    private static final float min = -0.5F;
    private static final float max = 0.5F;

    public CardProjectileEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(CardProjectileEntity entity, float yaw, float tickDelta, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        // orient to flight trajectory
        float currentYaw = MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw());
        float currentPitch = MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch());

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(currentYaw - 90.0F));

        boolean isFlying = !entity.isInGround();

        if (isFlying) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(currentPitch));
            float spinAngle = (entity.age + tickDelta) * 45.0F;

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spinAngle));
        } else {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(currentPitch));

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(15.0F));

            matrices.translate(0.0, 0.0, -0.22);
        }

        float scale = isFlying ? 0.45F : 0.65F;
        matrices.scale(scale, scale, scale);

        MatrixStack.Entry entry = matrices.peek();
        Matrix4f posMatrix = entry.getPositionMatrix();
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE));

        int blockLight = Math.max(10, light & 0xFFFF);
        int skyLight = (light >> 16) & 0xFFFF;
        int cardLight = (skyLight << 16) | blockLight;

        // front
        drawQuad(consumer, posMatrix, entry, cardLight, 1.0F, false);
        // back
        drawQuad(consumer, posMatrix, entry, cardLight, -1.0F, true);

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private void drawQuad(VertexConsumer consumer, Matrix4f mat, MatrixStack.Entry entry, int light, float normalY,
            boolean reverse) {
        float y = reverse ? -0.001F : 0.001F;
        if (!reverse) {
            vertex(consumer, mat, entry, min, y, min, 0.0F, 0.0F, normalY, light);
            vertex(consumer, mat, entry, max, y, min, 1.0F, 0.0F, normalY, light);
            vertex(consumer, mat, entry, max, y, max, 1.0F, 1.0F, normalY, light);
            vertex(consumer, mat, entry, min, y, max, 0.0F, 1.0F, normalY, light);
        } else {
            vertex(consumer, mat, entry, min, y, max, 0.0F, 1.0F, normalY, light);
            vertex(consumer, mat, entry, max, y, max, 1.0F, 1.0F, normalY, light);
            vertex(consumer, mat, entry, max, y, min, 1.0F, 0.0F, normalY, light);
            vertex(consumer, mat, entry, min, y, min, 0.0F, 0.0F, normalY, light);
        }
    }

    private void vertex(VertexConsumer consumer, Matrix4f mat, MatrixStack.Entry entry, float x, float y, float z, float u,
            float v, float ny, int light) {
        consumer.vertex(mat, x, y, z)
                .color(255, 255, 255, 255)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(entry, 0.0F, ny, 0.0F);
    }

    @Override
    public Identifier getTexture(CardProjectileEntity entity) {
        return TEXTURE;
    }
}