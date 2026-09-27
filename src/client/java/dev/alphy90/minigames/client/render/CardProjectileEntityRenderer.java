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

public class CardProjectileEntityRenderer extends EntityRenderer<CardProjectileEntity>{
    private static final Identifier TEXTURE = MiniGames.id("textures/item/tavern_card.png");

    private static final float min = -0.5F;
    private static final float max = 0.5F;

    public CardProjectileEntityRenderer(EntityRendererFactory.Context ctx){
        super(ctx);
    }

    @Override
    public void render(CardProjectileEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light){
        matrices.push();

        // orient to flight trajectory
        float currentYaw = MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw());
        float currentPitch = MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch());

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(currentYaw - 90.0F));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(currentPitch));

        // corner penetration rotation
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(15.0F));

        matrices.scale(0.35F, 0.35F, 0.35F);

        MatrixStack.Entry entry = matrices.peek();
        Matrix4f posMatrix = entry.getPositionMatrix();
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE));

        //front
        drawQuad(consumer, posMatrix, entry, light, 1.0F, false);
        //back
        drawQuad(consumer, posMatrix, entry, light, -1.0F, true);

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private void drawQuad(VertexConsumer consumer, Matrix4f mat, MatrixStack.Entry entry, int light, float normalY, boolean reverse){
        if(!reverse){
            vertex(consumer, mat, entry, min,  min, 0.0F, 0.0F, normalY, light);
            vertex(consumer, mat, entry, max,  min, 1.0F, 0.0F, normalY, light);
            vertex(consumer, mat, entry, max,  max, 1.0F, 1.0F, normalY, light);
            vertex(consumer, mat, entry, min,  max, 0.0F, 1.0F, normalY, light);
        } else {
            vertex(consumer, mat, entry, min,  max, 0.0F, 1.0F,  normalY, light);
            vertex(consumer, mat, entry, max,  max, 1.0F, 1.0F,  normalY,  light);
            vertex(consumer, mat, entry, max,  min, 1.0F, 0.0F,  normalY,  light);
            vertex(consumer, mat, entry, min,  min, 0.0F, 0.0F,  normalY,  light);
        }
    }

    private void vertex(VertexConsumer consumer, Matrix4f mat, MatrixStack.Entry entry,float x, float z, float u, float v, float ny, int light){
        consumer.vertex(mat, x, 0.0F, z)
                .color(255,255, 255, 255)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(entry, 0.0F, ny, 0.0F);
    }

    @Override
    public Identifier getTexture(CardProjectileEntity entity){
        return TEXTURE;
    }
}