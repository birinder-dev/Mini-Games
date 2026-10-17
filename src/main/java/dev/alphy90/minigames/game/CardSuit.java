package dev.alphy90.minigames.game;

import dev.alphy90.minigames.MiniGames;

import net.minecraft.util.Identifier;

public enum CardSuit{
    SWORD("Sword", "⚔", 1, "Highest number wins", Identifier.of(MiniGames.MOD_ID, "textures/item/tavern_card_sword.png")),
    SHIELD("Shield", "\uD83D\uDEE1", 2, "Lowest number wins", Identifier.of(MiniGames.MOD_ID, "textures/item/tavern_card_shield.png"));

    private final String displayName;
    private final String symbol;
    private final int priority;
    private final String ruleDescription;
    private final Identifier frontTexture;

    CardSuit(String displayName, String symbol, int priority, String ruleDescription, Identifier frontTexture){
        this.displayName = displayName;
        this.symbol = symbol;
        this.priority = priority;
        this.ruleDescription = ruleDescription;
        this.frontTexture = frontTexture;
    }

    public String getDisplayName() { return displayName; }
    public String getSymbol() { return symbol; }
    public int getPriority() { return priority; }
    public String getRuleDescription() { return ruleDescription; }
    public Identifier getFrontTexture() { return frontTexture; }
}