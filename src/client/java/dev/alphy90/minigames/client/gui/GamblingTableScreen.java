package dev.alphy90.minigames.client.gui;

import dev.alphy90.minigames.MiniGames;

import dev.alphy90.minigames.game.CardSuit;
import dev.alphy90.minigames.game.TavernCard;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.RotationAxis;

import javax.smartcardio.Card;

public class GamblingTableScreen extends Screen{
    private static final Identifier BG_TEXTURE = Identifier.of(MiniGames.MOD_ID, "textures/gui/bg_table_texture_main.png");
    private static final Identifier EMERALD_TEXTURE = Identifier.of(MiniGames.MOD_ID, "textures/gui/emerald_block.png");
    private static final Identifier CARD_BACK_TEXTURE = Identifier.of(MiniGames.MOD_ID, "textures/item/tavern_card.png");
    private static final Identifier CARD_FRONT_TEXTURE = Identifier.of(MiniGames.MOD_ID, "textures/item/tavern_card_front.png");
    private static final String[] TAB_NAMES = {"Game", "Rules", "Advanced"};
    private static final String[] DIFFICULTIES = {"Medium", "Easy", "Hard"};

    private int difficultyIndex = 0;
    private ButtonWidget difficultyButton;

    private final BlockPos tablePos;
    private int selectedSection = 0;

    private int bgWidth = 320;
    private int bgHeight = 220;
    private int left;
    private int top;

    //bet meter state
    private int currentBet = 10;
    private int minBet = 2;
    private int maxBet = 30;
    private boolean isDraggingMeter = false;

    //cards value
    private int cardCount = 5;
    private final int minCards = 1;
    private final int maxCards = 9;
    private double scroll = 0.0;
    private ButtonWidget backCardButton;
    private ButtonWidget nextCardButton;
    private ButtonWidget playButton;

    //tabs
    private int tabWidth = 92;
    private int tabHeight = 26;
    private int tabSpacing = 6;
    private int tabStartX;
    private int tabY;

    public GamblingTableScreen(BlockPos tablePos){
        super(Text.literal("Gambling Table"));
        this.tablePos = tablePos;
    }

    @Override
    protected void init(){
        super.init();
        this.left = (this.width - this.bgWidth) / 2;
        this.top = (this.height - this.bgHeight) / 2;

        int totalTabsWidth = (3 * this.tabWidth) + (2 * this.tabSpacing) ;
        this.tabStartX = this.left + (this.bgWidth - totalTabsWidth) / 2;
        this.tabY = this.top + 14;

        int contentX = this.left + 16;
        int contentY = tabY + this.tabHeight + 10;
        int contentWidth = this.bgWidth - 32;
        int contentHeight = (this.top + this.bgHeight - 16) - contentY;

        int btnWidth = 100;
        int btnHeight = 20;
        int rowY = contentY + 16;
        int btnX = contentX + contentWidth - 16 - btnWidth;

        this.difficultyButton = ButtonWidget.builder(
                Text.literal(DIFFICULTIES[this.difficultyIndex]),
                btn -> {
                    this.difficultyIndex = (this.difficultyIndex + 1) % DIFFICULTIES.length;
                    btn.setMessage(Text.literal(DIFFICULTIES[this.difficultyIndex]));
                }
        )
                .dimensions(btnX, rowY, btnWidth, btnHeight)
                .build();

        this.addDrawableChild(this.difficultyButton);

        // card area
        int rightStartX = contentX + (int)(contentWidth * 0.40f);
        int rightWidth = contentWidth - (int)(contentWidth * 0.40f);
        int rightCenterX = rightStartX + (rightWidth / 2);
        int playButtonY = contentY + contentHeight - 38;
        int playButtonWidth = 96;
        int arrowY = playButtonY - 26;

        this.backCardButton = ButtonWidget.builder(
                Text.literal("<"),
                btn -> {
                    if(this.cardCount > this.minCards){
                        this.cardCount--;
                        updateWidgetVisibility();
                    }
                }
        ).dimensions(rightCenterX - 48, arrowY, 20, 20).build();

        this.nextCardButton = ButtonWidget.builder(
                Text.literal(">"),
                btn -> {
                    if(this.cardCount < this.maxCards) {
                        this.cardCount++;
                        updateWidgetVisibility();
                    }
                }
        ).dimensions(rightCenterX + 28, arrowY, 20, 20).build();

        this.playButton = ButtonWidget.builder(
                Text.literal("Start Play!"),
                btn -> {
                    if(this.client != null && this.client.player != null){
                        this.client.player.sendMessage(Text.literal("Playing with " + this.cardCount + " cards and " + this.currentBet + " emeralds"), true);
                    }
                }
        ).dimensions(rightCenterX - (playButtonWidth / 2), playButtonY, playButtonWidth, 20).build();
        this.addDrawableChild(this.backCardButton);
        this.addDrawableChild(this.nextCardButton);
        this.addDrawableChild(this.playButton);

        updateWidgetVisibility();
    }

    private void updateWidgetVisibility(){
        boolean settingTab = (this.selectedSection == 2);
        if(this.difficultyButton != null){
            this.difficultyButton.visible = settingTab;
            this.difficultyButton.active = settingTab;
        }

        boolean gameTab = (this.selectedSection == 0);
        if(this.backCardButton != null){
            this.backCardButton.visible = gameTab;
            this.backCardButton.active = gameTab && (this.cardCount > this.minCards);
        }
        if(this.nextCardButton != null){
            this.nextCardButton.visible = gameTab;
            this.nextCardButton.active = gameTab && (this.cardCount < this.maxCards);
        }
        if(this.playButton != null){
            this.playButton.visible = gameTab;
            this.playButton.active = gameTab;
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta){
        // Empty so super.render() at the bottom doesn't run blur/darken over our custom GUI
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta){
        // Blurs and darkens only the 3D world behind the table window
        super.renderBackground(context, mouseX, mouseY, delta);

        // Texture original dimensions
        int texW = 1448;
        int texH = 1086;

        // Main bg texture scaled to 320x220
        context.drawTexture(BG_TEXTURE, this.left, this.top, this.bgWidth, this.bgHeight, 0.0F, 0.0F, texW, texH, texW, texH);

        // 3 tabs on top
        for(int i = 0; i < 3; i++){
            int tx = this.tabStartX + i * (this.tabWidth + this.tabSpacing);
            boolean isSelected = (this.selectedSection == i);
            boolean isHovered = mouseX >= tx && mouseX <= tx + this.tabWidth && mouseY >= this.tabY && mouseY <= this.tabY + this.tabHeight;

            // Draw tab background using texture scaled to tab size
            context.drawTexture(BG_TEXTURE, tx, this.tabY, this.tabWidth, this.tabHeight, 0.0F, 0.0F, texW, texH, texW, texH);

            if(isSelected){
                // Active tab has same natural light + gold highlight border
                context.drawBorder(tx, this.tabY, this.tabWidth, this.tabHeight, 0xFFE5A823);
            } else {
                // Inactive tabs are darker shadowed (lighter on hover)
                int shadowColor = isHovered ? 0x66000000 : 0x99000000;
                context.fill(tx, this.tabY, tx + this.tabWidth, this.tabY + this.tabHeight, shadowColor);
                context.drawBorder(tx, this.tabY, this.tabWidth, this.tabHeight, 0x44000000);
            }

            // Tab label
            String label = TAB_NAMES[i];
            int textColor = isSelected ? 0xFFFFFFFF : 0xFFAAAAAA;
            context.drawCenteredTextWithShadow(this.textRenderer, label, tx + (this.tabWidth / 2), this.tabY + (this.tabHeight - 8) / 2, textColor);
        }

        // Main content area placeholder for next step
        int contentX = this.left + 16;
        int contentY = this.tabY + this.tabHeight + 10;
        int contentWidth = this.bgWidth - 32;
        int contentHeight = (this.top + this.bgHeight - 16) - contentY;

        //tiled advanced section
        if(this.selectedSection == 2){
            context.enableScissor(contentX, contentY, contentX + contentWidth, contentY + contentHeight);
            int tileSize = 16;
            for(int x = contentX; x < contentX + contentWidth; x += tileSize){
                for(int y = contentY; y < contentY + contentHeight; y += tileSize){
                    context.drawTexture(EMERALD_TEXTURE, x, y, tileSize, tileSize, 0.0F, 0.0F, 470, 470, 470, 470);
                }
            }
            context.disableScissor();

            //transparent overlay
            context.fill(contentX, contentY, contentX + contentWidth, contentY + contentHeight, 0xB0141414);
            context.drawBorder(contentX, contentY, contentWidth, contentHeight, 0x44000000);

            //label
            int rowY = contentY + 16;
            context.drawTextWithShadow(this.textRenderer, Text.literal("Difficulty"), contentX + 16, rowY + 6, 0xFFFFFFFF);
        } else if(this.selectedSection == 0){
            //section 0
            //left meter
            int leftWidth = (int)(contentWidth * 0.40f);
            int leftCenterX = contentX + (leftWidth / 2);
            int barX = leftCenterX - 6;
            int barWidth = 12;
            int barTopY = contentY + 14;
            int barHeight = 84;
            int barBottomY = barTopY + barHeight;

            //top cap
            context.fill(barX - 8, barTopY, barX + 8 + barWidth, barTopY + 2, 0xFFE5B834);
            context.fill(barX - 8, barTopY, barX + 8 + barWidth, barTopY + 1, 0xFFFFF19E);
            context.drawTextWithShadow(this.textRenderer, Text.literal("pay"), barX - 28, barTopY - 3, 0xFFCCCCCC);
            context.drawTextWithShadow(this.textRenderer, Text.literal(String.valueOf(this.maxBet)), barX + barWidth + 10, barTopY - 3, 0xFFE5B834);

            //bottom cap
            context.fill(barX - 8, barBottomY - 2, barX + barWidth + 8, barBottomY, 0xFFE5B834);
            context.fill(barX - 8, barBottomY - 1, barX + barWidth + 8, barBottomY, 0xFF8A6510);
            context.drawTextWithShadow(this.textRenderer, Text.literal("get"), barX - 28, barBottomY - 6, 0xFFCCCCCC);
            context.drawTextWithShadow(this.textRenderer, Text.literal(String.valueOf(this.minBet)), barX + barWidth + 10, barBottomY - 6, 0xFFE5B834);

            //vertical golden outer rails
            context.fill(barX, barTopY, barX + 1, barBottomY, 0xFFE5B834);
            context.fill(barX + barWidth - 1, barTopY, barX + barWidth, barBottomY, 0xFFE5B834);

            //background track
            context.fill(barX + 1, barTopY + 2, barX + barWidth - 1, barBottomY - 2, 0xFF2A2D32);
            context.fill(barX + 2, barTopY + 2, barX + 3, barBottomY - 2, 0xFF3D4249);

            //filled emerald section
            float fraction = (float)(this.currentBet - this.minBet) / (this.maxBet - this.minBet);
            int fillH =  Math.round((barHeight - 4) * fraction);
            int fillY = (barBottomY - 2) - fillH;

            if(fillH > 0){
                //emerald
                context.fill(barX + 1, fillY, barX + barWidth - 1, barBottomY - 2, 0xFF00C853);
                //inner shine line
                context.fill(barX + 2, fillY, barX + 3, barBottomY - 2, 0xFF69F0AE);
                //reflection line
                context.fill(barX + 1, fillY, barX + barWidth - 1, fillY + 1, 0xFFB9F6CA);
            }

            //bich aali reference dandi
            int tick20Y = barTopY + (int)(barHeight * 0.35f);
            context.fill(barX - 4, tick20Y, barX + barWidth + 4, tick20Y + 1, 0x88AAAAAA);
            context.drawTextWithShadow(this.textRenderer, Text.literal("20"), barX + barWidth + 8, tick20Y - 4, 0xFFAAAAAA);

            int tick10Y = barTopY + (int)(barHeight * 0.68f);
            context.fill(barX - 4, tick10Y, barX + barWidth + 4, tick10Y + 1, 0x88AAAAAA);
            context.drawTextWithShadow(this.textRenderer, Text.literal("10"), barX - 18, tick10Y - 4, 0xFFAAAAAA);

            //current bet
            context.fill(barX - 5, fillY, barX, fillY + 1, 0xFFFFE57F);
            context.drawTextWithShadow(this.textRenderer, Text.literal(String.valueOf(this.currentBet)), barX - 22, fillY - 4, 0xFFFFE57F);

            //golden coin w emerald
            int coinCenterY = barBottomY + 18;
            drawCoin(context, leftCenterX, coinCenterY, 12);
            context.drawItem(new ItemStack(Items.EMERALD), leftCenterX - 8, coinCenterY - 8);

            int rightStartX = contentX + (int)(contentWidth * 0.40f);
            int rightWidth = contentWidth - (int)(contentWidth * 0.40f);
            int rightCenterX = rightStartX + (rightWidth / 2);
            int cardW = 28;
            int cardH = 40;
            int cardsCenterY = contentY + 36;

            float maxAngle = (this.cardCount <= 5) ? (this.cardCount - 1)*6.0f : 24.0f;
            float angleStep = (this.cardCount > 1) ? (maxAngle * 2.0f) / (this.cardCount - 1) : 0.0F;
            float stepX = (this.cardCount > 1) ? Math.min(14.0f, 120.0f / (this.cardCount - 1)) : 0.0f;
            MatrixStack matrices = context.getMatrices();
            for(int i = 0; i < this.cardCount; i++){
                float offset = i - (this.cardCount - 1) / 2.0f;
                float angle = -maxAngle + i * angleStep;
                int cardX = rightCenterX + (int)(offset * stepX);
                int cardY = cardsCenterY + (int)(offset * offset * 0.8f);
                matrices.push();
                matrices.translate(cardX, cardY, 0);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle));
                matrices.translate(-cardW / 2.0f, -cardH / 2.0f, 0);

                context.drawTexture(CARD_BACK_TEXTURE, 0, 0, cardW, cardH, 0.0F, 0.0F, 32, 32, 32, 32);
                matrices.pop();
            }
            int playButtonY = contentY + contentHeight - 38;
            int stepperY = playButtonY - 26;
            String cardText = this.cardCount + (this.cardCount == 1 ? " Card" : " Cards");
            context.drawCenteredTextWithShadow(this.textRenderer, cardText, rightCenterX, stepperY + 6, 0xFFFFFFFF);

            //cost
            int emeraldTextY = playButtonY + 23;
            context.drawItem(new ItemStack(Items.EMERALD), rightCenterX - 36, emeraldTextY - 3);
            context.drawTextWithShadow(this.textRenderer, Text.literal(this.currentBet + " Emeralds"), rightCenterX - 18, emeraldTextY + 1, 0xFF55FF55);

        } else if (this.selectedSection == 1) {
            int totalContentHeight = 160;
            int maxScroll = Math.max(0, totalContentHeight - contentHeight);
            context.enableScissor(contentX, contentY, contentX + contentWidth, contentY + contentHeight);
            int startY = contentY + 10 - (int)this.scroll;
            int curY = startY;

            context.drawText(this.textRenderer, "Every card suit got a special condition to win the round.", contentX + 10, curY, 0xFFCCCCCC, false);
            curY+=11;
            context.drawText(this.textRenderer, "Which condition is selected is decided by a Priority Order:", contentX + 10, curY, 0xFF999999, false);
            curY += 15;

            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(contentX + 10, curY, 0);
            matrices.scale(1.15f, 1.15f, 1.0f);
            context.drawTextWithShadow(this.textRenderer, Text.literal("1. Priority Order:").formatted(Formatting.BOLD), 0, 0, 0xFFFFD700);
            matrices.pop();
            curY += 16;

            context.drawTextWithShadow(this.textRenderer, Text.literal("Swords ⚔  >  Shields \uD83D\uDEE1").formatted(Formatting.GOLD), contentX + 18, curY, 0xFFAAAAAA);
            curY += 13;
            context.drawText(this.textRenderer, "• If any Sword is played, the Sword condition takes priority.", contentX + 10, curY, 0xFFAAAAAA, false);
            curY += 11;
            context.drawText(this.textRenderer, "• Shield rule only applies when BOTH played cards are Shield.", contentX + 10, curY, 0xFFAAAAAA, false);
            curY += 16;

            int cardW = 24;
            int cardH = 34;

            //left-different suit
            int leftCenterX = contentX + 70;
            drawFrontCard(context, leftCenterX - 26, curY, cardW, cardH, CardSuit.SWORD, 7);
            drawFrontCard(context, leftCenterX + 2, curY, cardW, cardH, CardSuit.SHIELD, 4);
            context.drawCenteredTextWithShadow(this.textRenderer, "⚔ Prioritised", leftCenterX, curY + cardH + 4, 0xFFFF6666);

            //right-same
            int rightCenterX = contentX + 210;
            drawFrontCard(context, rightCenterX - 26, curY, cardW, cardH, CardSuit.SHIELD, 6);
            drawFrontCard(context, rightCenterX + 2, curY, cardW, cardH, CardSuit.SHIELD, 2);
            context.drawCenteredTextWithShadow(this.textRenderer, "\uD83D\uDEE1 Prioritised", rightCenterX, curY + cardH + 4, 0xFF55AAFF);
            curY += cardH + 20;
            context.disableScissor();

            if(maxScroll > 0){
                int scrollbarX = contentX + contentWidth - 4;
                int trackH = contentHeight - 4;
                int thumbH = Math.max(16, (int)((float) contentHeight / totalContentHeight * trackH));
                int thumbY = contentY + 2 + (int) ((this.scroll / maxScroll) * (trackH - thumbH));

                context.fill(scrollbarX, contentY + 2, scrollbarX + 2, contentY + 2 + trackH, 0x44000000);
                context.fill(scrollbarX, thumbY, scrollbarX + 2, thumbY + thumbH, 0xAAFFFFFF);
            }
        } else {
            context.fill(contentX, contentY, contentX + contentWidth, contentY + contentHeight, 0x33000000);
            context.drawBorder(contentX, contentY, contentWidth, contentHeight, 0x22000000);
        }


        // Render widgets on top of backgrounds and overlays
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button){
        if(button == 0){
            for(int i = 0; i < 3; i++){
                int tx = this.tabStartX + i * (this.tabWidth + this.tabSpacing);
                if(mouseX >= tx && mouseX <= tx + this.tabWidth && mouseY >= this.tabY && mouseY <= tabY + this.tabHeight){
                    if(this.selectedSection != i){
                        this.selectedSection = i;
                        updateWidgetVisibility();
                        if(this.client != null){
                            this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        }
                    }
                    return true;
                }
            }
            //drag bet on meter
            if(this.selectedSection == 0){
                int contentX = this.left + 16;
                int contentY = this.tabY + this.tabHeight + 10;
                int contentWidth = this.bgWidth - 32;
                int leftWidth = (int)(contentWidth * 0.40f);
                int leftCenterX = contentX + (leftWidth / 2);
                int barX = leftCenterX - 6;
                int barWidth = 12;
                int barTopY = contentY + 14;
                int barHeight = 84;

                if(mouseX >= barX - 16 && mouseX <= barX + barWidth + 16 && mouseY >= barTopY && mouseY <= barTopY + barHeight){
                    this.isDraggingMeter = true;
                    updateBetFromMouse(mouseY, barTopY, barHeight);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY){
        if(this.isDraggingMeter && this.selectedSection == 0){
            int contentY = this.tabY + this.tabHeight + 10;
            int barTopY = contentY + 14;
            int barHeight = 84;
            updateBetFromMouse(mouseY, barTopY, barHeight);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button){
        if(button == 0){
            this.isDraggingMeter = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateBetFromMouse(double mouseY, int barTopY, int barHeight){
        float fraction = 1.0f - (float)((mouseY - barTopY) / barHeight);
        fraction = Math.max(0.0f, Math.min(1.0f, fraction));
        this.currentBet = this.minBet + Math.round(fraction * (this.maxBet - this.minBet));
    }

    private void drawCoin(DrawContext context, int centerX, int centerY, int radius){
        int r2 = radius * radius;
        int innerR2 = (radius - 2) * (radius - 2);
        for(int dy = -radius; dy <= radius; dy++){
            for(int dx = -radius; dx <= radius; dx++){
                int d2 = dx * dx + dy * dy;
                if(d2 <= r2){
                    if(d2 > innerR2){
                        int color = (dx + dy < 0) ? 0xFFFFE57F : 0xFF9E7206;
                        context.fill(centerX + dx, centerY + dy, centerX + dx + 1, centerY + dy + 1, color);
                    } else {
                        context.fill(centerX + dx,centerY + dy, centerX + dx + 1, centerY + dy + 1, 0xFF2A2213);
                    }
                }
            }
        }
    }

    private void drawFrontCard(DrawContext context, int x, int y, int w, int h, CardSuit suit, int value){
       dev.alphy90.minigames.client.render.CardRenderHelper.renderFrontCardForGui(context, this.textRenderer, CARD_FRONT_TEXTURE, x, y, w, h, new TavernCard(suit, value));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers){
        if(super.keyPressed(keyCode, scanCode, modifiers)){
            return true;
        }

        //also allow closing w 'E'
        if(this.client != null && this.client.options.inventoryKey.matchesKey(keyCode, scanCode)){
            this.close();
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldPause(){
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical){
        if(this.selectedSection == 1){
            int totaContentHeight = 160;
            int contentY = this.tabY + this.tabHeight + 10;
            int contentHeight = (this.top + this.bgHeight - 16) - contentY;
            int maxScroll = Math.max(0, totaContentHeight - contentHeight);
            this.scroll = Math.max(0.0, Math.min(maxScroll, this.scroll - vertical * 14.0));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }
}
