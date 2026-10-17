package dev.alphy90.minigames.game;

public record TavernCard(CardSuit suit, int value) {
    public enum RoundResult { WIN, LOSS, TIE }

    public RoundResult compareWith(TavernCard opponent){
        if(this.value == opponent.value) return RoundResult.TIE;

        if(this.suit == CardSuit.SWORD || opponent.suit == CardSuit.SWORD){
            return this.value > opponent.value ? RoundResult.WIN : RoundResult.LOSS;
        }

        //both shield
        return this.value < opponent.value ? RoundResult.WIN : RoundResult.LOSS;
    }
}