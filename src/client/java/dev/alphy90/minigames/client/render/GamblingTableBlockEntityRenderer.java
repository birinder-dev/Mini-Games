package dev.alphy90.minigames.client.render;

import dev.alphy90.minigames.MiniGames;
import dev.alphy90.minigames.block.GamblingTableBlock;
import dev.alphy90.minigames.block.entity.GamblingTableBlockEntity;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.apache.logging.log4j.core.appender.rolling.action.IfAll;
import org.joml.Matrix4f;

public class GamblingTableBlockEntityRenderer implements BlockEntityRenderer<GamblingTableBlockEntity>{
    private static final Identifier CARD_TEXTURE = Identifier.of(MiniGames.MOD_ID, "textures/item/tavern_card.png");

    public GamblingTableBlockEntityRenderer(BlockEntityRendererFactory.Context ctx){}

    @Override
    public void render(GamblingTableBlockEntity be, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay){
        if(!be.isHasCards()) return;

        Direction facing = be.getCachedState().get(GamblingTableBlock.FACING);

        matrices.push();
        matrices.translate(0.5, 0.0, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-facing.asRotation()));
        matrices.translate(-0.5, 0.0, -0.5);

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(CARD_TEXTURE));

        float cardW = 0.22F;
        float cardH = 0.32F;

        //DECK PILE
        int pileHeight = 8;
        for (int i = 0; i < pileHeight; i++){
            matrices.push();
            float stackY = 1.002F + (i * 0.003F);
            float slightJitter = ((i % 3) - 1) * 1.5F;
            matrices.translate(0.28F, stackY, 0.50F);

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(slightJitter));
            renderFlatCard(matrices, buffer, light, cardW, cardH, false);
            matrices.pop();
        }
        matrices.pop();
    }

    private void renderFlatCard(MatrixStack matrices, VertexConsumer buffer, int light, float cardW, float cardH, boolean faceUp){
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));

        MatrixStack.Entry entry = matrices.peek();
        Matrix4f pos = entry.getPositionMatrix();
        float halfW = cardW / 2.0F;
        float halfH = cardH / 2.0F;

        if(faceUp){
            buffer.vertex(pos, -halfW, -halfH, 0.0F).color(255, 255, 255, 255).texture(0.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 1.0F, 0.0F);
            buffer.vertex(pos, halfW, -halfH, 0.0F).color(255, 255, 255, 255).texture(1.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 1.0F, 0.0F);
            buffer.vertex(pos, halfW, halfH, 0.0F).color(255, 255, 255, 255).texture(1.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 1.0F, 0.0F);
            buffer.vertex(pos, -halfW, halfH, 0.0F).color(255, 255, 255, 255).texture(0.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 1.0F, 0.0F);
        }else {
            //pile
            buffer.vertex(pos, halfW, -halfH, 0.0F).color(255, 255, 255, 255).texture(1.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 1.0F, 0.0F);
            buffer.vertex(pos, -halfW, -halfH, 0.0F).color(255, 255, 255, 255).texture(0.0F, 1.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 1.0F, 0.0F);
            buffer.vertex(pos, -halfW, -halfH, 0.0F).color(255, 255, 255, 255).texture(0.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 1.0F, 0.0F);
            buffer.vertex(pos, halfW, halfH, 0.0F).color(255, 255, 255, 255).texture(1.0F, 0.0F).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0.0F, 1.0F, 0.0F);
        }
        matrices.pop();
    }
}