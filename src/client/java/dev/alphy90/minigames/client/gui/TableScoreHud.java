package dev.alphy90.minigames.client.gui;

import dev.alphy90.minigames.client.CardClientState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.List;

public class TableScoreHud {
    private static final ItemStack EMERALD_STACK = new ItemStack(Items.EMERALD);

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!CardClientState.isTablePlaying()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        int totalRounds = Math.max(1, CardClientState.getTotalRounds());
        List<Integer> results = CardClientState.getRoundResults();
        StringBuilder formBuilder = new StringBuilder("\u00A78(\u00A77Form: ");
        for (int i = 0; i < totalRounds; i++) {
            if (i > 0) {
                formBuilder.append(" ");
            }
            if (i < results.size()) {
                int res = results.get(i);
                if (res > 0) {
                    formBuilder.append("\u00A7a\u25CF");
                } else if (res < 0) {
                    formBuilder.append("\u00A7c\u25CF");
                } else {
                    formBuilder.append("\u00A7e\u25CF");
                }
            } else {
                formBuilder.append("\u00A77\u25CF");
            }
        }
        formBuilder.append("\u00A78)");
        String formString = formBuilder.toString();

        int formW = textRenderer.getWidth(formString);
        int boxW = Math.max(114, formW + 14);
        int boxH = 124;
        int boxX = 8;
        int boxY = (context.getScaledWindowHeight() - boxH) / 2;

        context.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0x900C0D10);
        context.fill(boxX, boxY, boxX + boxW, boxY + 2, 0xFFE5B834);
        context.drawBorder(boxX, boxY, boxW, boxH, 0x33000000);

        context.drawCenteredTextWithShadow(textRenderer, "\u00A76\u00A7lMINI-GAMES", boxX + (boxW / 2), boxY + 6, 0xFFFFFFFF);
        context.fill(boxX + 5, boxY + 18, boxX + boxW - 5, boxY + 19, 0x33FFFFFF);

        String round = "\u00A7eCurrent Round: \u00A7f" + CardClientState.getCurrentRound();
        context.drawCenteredTextWithShadow(textRenderer, round, boxX + (boxW / 2), boxY + 22, 0xFFFFFFFF);
        context.fill(boxX + 5, boxY + 34, boxX + boxW - 5, boxY + 35, 0x22FFFFFF);

        context.drawTextWithShadow(textRenderer, "\u00A77Player", boxX + 6, boxY + 39, 0xFFAAAAAA);
        String scoreHeader = "\u00A77Score";
        context.drawTextWithShadow(textRenderer, scoreHeader, boxX + boxW - 6 - textRenderer.getWidth(scoreHeader), boxY + 39, 0xFFAAAAAA);

        context.drawTextWithShadow(textRenderer, "\u00A7aYou", boxX + 6, boxY + 52, 0xFFFFFFFF);
        String playerScore = "\u00A7f" + CardClientState.getPlayerScore();
        context.drawTextWithShadow(textRenderer, playerScore, boxX + boxW - 6 - textRenderer.getWidth(playerScore), boxY + 52, 0xFFFFFFFF);

        context.drawTextWithShadow(textRenderer, formString, boxX + 6, boxY + 64, 0xFFFFFFFF);

        context.drawTextWithShadow(textRenderer, "\u00A7cGambler", boxX + 6, boxY + 76, 0xFFFFFFFF);
        String gamblerScore = "\u00A7f" + CardClientState.getGamblerScore();
        context.drawTextWithShadow(textRenderer, gamblerScore, boxX + boxW - 6 - textRenderer.getWidth(gamblerScore), boxY + 76, 0xFFFFFFFF);

        context.fill(boxX + 5, boxY + 89, boxX + boxW - 5, boxY + 90, 0x22FFFFFF);

        int scoreToWin = (totalRounds / 2) + 1;
        context.drawTextWithShadow(textRenderer, "\u00A7fScore to win: \u00A7e" + scoreToWin, boxX + 6, boxY + 94, 0xFFFFFFFF);

        String betMsg = "\u00A7fBet amount: \u00A7a" + CardClientState.getBetAmount();
        context.drawTextWithShadow(textRenderer, betMsg, boxX + 6, boxY + 108, 0xFFFFFFFF);
        int betW = textRenderer.getWidth(betMsg);

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(boxX + 8 + betW, boxY + 105, 0);
        matrices.scale(0.75f, 0.75f, 1.0f);
        context.drawItem(EMERALD_STACK, 0, 0);
        matrices.pop();
    }
}
