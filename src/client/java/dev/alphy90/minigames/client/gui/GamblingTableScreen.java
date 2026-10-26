package dev.alphy90.minigames.client.gui;

import dev.alphy90.minigames.MiniGames;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class GamblingTableScreen extends Screen{
    private static final Identifier BG_TEXTURE = Identifier.of(MiniGames.MOD_ID, "textures/gui/bg_table_texture_main.png");
    private static final Identifier EMERALD_TEXTURE = Identifier.of(MiniGames.MOD_ID, "textures/gui/emerald_block.png");
    private static final String[] TAB_NAMES = {"Section 1", "Section 2", "Advanced"};
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
        updateWidgetVisibility();
    }

    private void updateWidgetVisibility(){
        boolean isAdvanced = (this.selectedSection == 2);
        if(this.difficultyButton != null){
            this.difficultyButton.visible = isAdvanced;
            this.difficultyButton.active = isAdvanced;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta){
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
        }else {
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
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
}
