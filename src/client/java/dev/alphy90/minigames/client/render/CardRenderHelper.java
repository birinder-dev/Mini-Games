package dev.alphy90.minigames.client.render;

import dev.alphy90.minigames.game.CardSuit;
import dev.alphy90.minigames.game.TavernCard;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public class CardRenderHelper {
    public static void renderCardFrontDetails(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float cardW, float cardH, float centerY, TavernCard card){
        if(card == null) return;

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        float halfW = cardW / 2.0F;
        float halfH = cardH / 2.0F;

        String number = String.valueOf(card.value());
        String symbol = card.suit().getSymbol();
        int symbolColor = (card.suit() == CardSuit.SWORD) ? 0xFFD32F2F : 0xFF2A6F97;
        int numberColor = 0xFF1C1C1C;

        int numW = textRenderer.getWidth(number);
        int symW = textRenderer.getWidth(symbol);

        //corner numbers
        float numScale = 0.0055F;
        matrices.push();
        matrices.translate(-halfW + 0.040F, centerY + halfH - 0.042F, 0.002F);
        matrices.scale(numScale, -numScale, numScale);
        textRenderer.draw(number, -numW / 2.0F, -textRenderer.fontHeight / 2.0F, numberColor, false,
                matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET, 0, light);
        matrices.pop();

        //bottom corner
        matrices.push();
        matrices.translate(halfW - 0.040F, centerY - halfH + 0.042F, 0.002F);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
        matrices.scale(numScale, -numScale, numScale);
        textRenderer.draw(number, -numW / 2.0F, -textRenderer.fontHeight / 2.0F, numberColor, false,
                matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET, 0, light);
        matrices.pop();

        //pips
        int val = Math.max(1, Math.min(10, card.value()));

        if(val == 1){
            matrices.push();
            matrices.translate(0.0F, centerY, 0.002F);
            float bigScale = 0.0085F;
            matrices.scale(bigScale, -bigScale, bigScale);
            textRenderer.draw(symbol, -symW / 2.0F, -textRenderer.fontHeight / 2.0F, symbolColor, false,
                    matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET, 0, light);
            matrices.pop();
        } else {
            float pipScale = (val <= 3) ? 0.0042F : (val <= 6 ? 0.0035F : 0.0028F);
            float spreadX = halfW * 0.44F;
            float spreadY = halfH * 0.54F;
            float[][] coords = getPipCoords(val);

            for(float[] pt : coords){
                float px = pt[0] * spreadX;
                float py = centerY + pt[1] * spreadY;

                matrices.push();
                matrices.translate(px, py, 0.002F);
                matrices.scale(pipScale, -pipScale, pipScale);
                textRenderer.draw(symbol, -symW / 2.0F, -textRenderer.fontHeight / 2.0F, symbolColor, false,
                        matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET, 0, light);
                matrices.pop();
                }
        }
    }

    private static float[][] getPipCoords(int val){
        return switch (val){
            case 2 -> new float[][]{{0.0F, 0.65F}, {0.0F, -0.65F}};
            case 3 -> new float[][]{{0.0F, 0.70F}, {0.0F, 0.0F}, {0.0F, -0.70F}};
            case 4 -> new float[][]{{-1.0F, 0.70F}, {1.0F, 0.70F}, {-1.0F, -0.70F}, {1.0F, -0.70F}};
            case 5 -> new float[][]{{-1.0F, 0.70F}, {1.0F, 0.70F}, {0.0F, 0.0F}, {-1.0F, -0.70F}, {1.0F, -0.70F}};
            case 6 -> new float[][]{{-1.0F, 0.75F}, {1.0F, 0.75F}, {-1.0F, 0.0F}, {1.0F, 0.0F}, {-1.0F, -0.75F}, {1.0F, -0.75F}};
            case 7 -> new float[][]{{-1.0F, 0.75F}, {1.0F, 0.75F}, {0.0F, 0.38F}, {-1.0F, 0.0F}, {1.0F, 0.0F}, {-1.0F, -0.75F}, {1.0F, -0.75F}};
            case 8 -> new float[][]{{-1.0F, 0.75F}, {1.0F, 0.75F}, {0.0F, 0.38F}, {-1.0F, 0.0F}, {1.0F, 0.0F}, {0.0F, -0.38F}, {-1.0F, -0.75F}, {1.0F, -0.75F}};
            case 9 -> new float[][]{{-1.0F, 0.80F}, {1.0F, 0.80F}, {-1.0F, 0.27F}, {1.0F, 0.27F}, {0.0F, 0.0F}, {-1.0F, -0.27F}, {1.0F, -0.27F}, {-1.0F, -0.80F}, {1.0F, -0.80F}};
            case 10 -> new float[][]{{-1.0F, 0.82F}, {1.0F, 0.82F}, {0.0F, 0.48F}, {-1.0F, 0.27F}, {1.0F, 0.27F}, {-1.0F, -0.27F}, {1.0F, -0.27F}, {0.0F, -0.48F}, {-1.0F, -0.82F}, {1.0F, -0.82F}};
            default -> new float[][]{{0.0F, 0.0F}};
        };
    }
}