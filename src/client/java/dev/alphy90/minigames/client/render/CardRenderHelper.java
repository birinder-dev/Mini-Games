package dev.alphy90.minigames.client.render;

import dev.alphy90.minigames.game.CardSuit;
import dev.alphy90.minigames.game.TavernCard;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public class CardRenderHelper {
    public static void renderCardFrontDetails(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
            float cardW, float cardH, float centerY, TavernCard card) {
        if (card == null)
            return;

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        float halfW = cardW / 2.0F;
        float halfH = cardH / 2.0F;

        String number = String.valueOf(card.value());
        String symbol = card.suit().getSymbol();
        int symbolColor = (card.suit() == CardSuit.SWORD) ? 0xFFD32F2F : 0xFF2A6F97;
        int numberColor = 0xFF1C1C1C;

        int numW = textRenderer.getWidth(number);
        int symW = textRenderer.getWidth(symbol);

        // Corner numbers: positioned relative to visible card canvas inside texture
        // margins
        float numScale = 0.0036F;
        float cornerX = halfW * 0.44F;
        float cornerY = halfH * 0.64F;

        // Top-left corner
        matrices.push();
        matrices.translate(-cornerX, centerY + cornerY, 0.002F);
        matrices.scale(numScale, -numScale, numScale);
        textRenderer.draw(number, -numW / 2.0F, -textRenderer.fontHeight / 2.0F, numberColor, false,
                matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET, 0,
                light);
        matrices.pop();

        // Bottom-right corner (inverted)
        matrices.push();
        matrices.translate(cornerX, centerY - cornerY, 0.002F);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
        matrices.scale(numScale, -numScale, numScale);
        textRenderer.draw(number, -numW / 2.0F, -textRenderer.fontHeight / 2.0F, numberColor, false,
                matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET, 0,
                light);
        matrices.pop();

        // Middle pips
        int val = Math.max(1, Math.min(10, card.value()));

        if (val == 1) {
            matrices.push();
            matrices.translate(0.0F, centerY, 0.002F);
            float bigScale = 0.0070F;
            matrices.scale(bigScale, -bigScale, bigScale);
            textRenderer.draw(symbol, -symW / 2.0F, -textRenderer.fontHeight / 2.0F, symbolColor, false,
                    matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET, 0,
                    light);
            matrices.pop();
        } else {
            float pipScale = (val <= 3) ? 0.0038F : (val <= 6 ? 0.0030F : 0.0024F);
            float spreadX = halfW * 0.35F;
            float spreadY = halfH * 0.50F;
            float[][] coords = getPipCoords(val);

            for (float[] pt : coords) {
                float px = pt[0] * spreadX;
                float py = centerY + pt[1] * spreadY;

                matrices.push();
                matrices.translate(px, py, 0.002F);
                matrices.scale(pipScale, -pipScale, pipScale);
                textRenderer.draw(symbol, -symW / 2.0F, -textRenderer.fontHeight / 2.0F, symbolColor, false,
                        matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET,
                        0, light);
                matrices.pop();
            }
        }
    }

    public static void renderFrontCardForGui(net.minecraft.client.gui.DrawContext context, TextRenderer textRenderer, net.minecraft.util.Identifier frontTexture, int x, int y, int w, int h, TavernCard card){
        if(card == null) return;

        context.drawTexture(frontTexture, x, y, w, h, 0.0f, 0.0f, 32, 32, 32, 32);
        String num = String.valueOf(card.value());
        String symbol = card.suit().getSymbol();
        int symW = textRenderer.getWidth(symbol);
        int symbolColor = (card.suit() == CardSuit.SWORD) ? 0xFFD32F2F : 0xFF2A6F97;
        int numberColor = 0xFF1C1C1C;

        MatrixStack matrices = context.getMatrices();
        float numScale = 0.60f;
        matrices.push();
        matrices.translate(x + 5.2f, y + 3.8f, 0);
        matrices.scale(numScale, numScale, 1.0f);
        context.drawText(textRenderer, num, 0, 0, numberColor, false);
        matrices.pop();
        matrices.push();
        matrices.translate(x + w - 5.2f, y + h - 3.8f, 0);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
        matrices.scale(numScale, numScale, 1.0f);
        context.drawText(textRenderer, num, 0, 0, numberColor, false);
        matrices.pop();

        float cx = x + (w / 2.0f);
        float cy = y + (h / 2.0f);
        int val = Math.max(1, Math.min(10, card.value()));

        if(val == 1){
            matrices.push();
            matrices.translate(cx, cy, 0);
            matrices.scale(0.75f, 0.75f, 1.0f);
            context.drawText(textRenderer, symbol, -symW / 2, -4, symbolColor, false);
            matrices.pop();
        } else {
            float pipScale = (val <= 4) ? 0.52f : 0.44f;
            float spreadX = (w / 2.0f) * 0.42f;
            float spreadY = (h / 2.0f) * 0.52f;
            float[][] coords = getPipCoords(val);

            for(float[] pt : coords){
                float px = cx + (pt[0] * spreadX);
                float py = cy - (pt[1] * spreadY);

                matrices.push();
                matrices.translate(px, py, 0);
                matrices.scale(pipScale, pipScale, 1.0f);
                context.drawText(textRenderer, symbol, -symW / 2, -4, symbolColor, false);
                matrices.pop();
            }
        }
    }

    private static float[][] getPipCoords(int val) {
        return switch (val) {
            case 2 -> new float[][] { { 0.0F, 0.65F }, { 0.0F, -0.65F } };
            case 3 -> new float[][] { { 0.0F, 0.70F }, { 0.0F, 0.0F }, { 0.0F, -0.70F } };
            case 4 -> new float[][] { { -1.0F, 0.70F }, { 1.0F, 0.70F }, { -1.0F, -0.70F }, { 1.0F, -0.70F } };
            case 5 -> new float[][] { { -1.0F, 0.70F }, { 1.0F, 0.70F }, { 0.0F, 0.0F }, { -1.0F, -0.70F },
                    { 1.0F, -0.70F } };
            case 6 -> new float[][] { { -1.0F, 0.75F }, { 1.0F, 0.75F }, { -1.0F, 0.0F }, { 1.0F, 0.0F },
                    { -1.0F, -0.75F }, { 1.0F, -0.75F } };
            case 7 -> new float[][] { { -1.0F, 0.75F }, { 1.0F, 0.75F }, { 0.0F, 0.38F }, { -1.0F, 0.0F },
                    { 1.0F, 0.0F }, { -1.0F, -0.75F }, { 1.0F, -0.75F } };
            case 8 -> new float[][] { { -1.0F, 0.75F }, { 1.0F, 0.75F }, { 0.0F, 0.38F }, { -1.0F, 0.0F },
                    { 1.0F, 0.0F }, { 0.0F, -0.38F }, { -1.0F, -0.75F }, { 1.0F, -0.75F } };
            case 9 -> new float[][] { { -1.0F, 0.80F }, { 1.0F, 0.80F }, { -1.0F, 0.27F }, { 1.0F, 0.27F },
                    { 0.0F, 0.0F }, { -1.0F, -0.27F }, { 1.0F, -0.27F }, { -1.0F, -0.80F }, { 1.0F, -0.80F } };
            case 10 ->
                new float[][] { { -1.0F, 0.82F }, { 1.0F, 0.82F }, { 0.0F, 0.48F }, { -1.0F, 0.27F }, { 1.0F, 0.27F },
                        { -1.0F, -0.27F }, { 1.0F, -0.27F }, { 0.0F, -0.48F }, { -1.0F, -0.82F }, { 1.0F, -0.82F } };
            default -> new float[][] { { 0.0F, 0.0F } };
        };
    }
}