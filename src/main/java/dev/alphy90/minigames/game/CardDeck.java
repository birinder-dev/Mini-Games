package dev.alphy90.minigames.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CardDeck {
    public static final Random RNG = new Random();

    public static int rollWeightedValue(CardSuit suit){
        int[] weights = new int[10];
        int totalWeight = 0;

        for(int i =0; i < 10; i++){
            int value = i + 1;
            if(suit == CardSuit.SWORD){
                weights[i] = 11 - value;
            } else {
                weights[i] = value;
            }
            totalWeight += weights[i];
        }

        int roll = RNG.nextInt(totalWeight);
        int accumulated = 0;
        for(int i = 0; i < 10; i++){
            accumulated += weights[i];
            if(roll < accumulated){
                return i + 1;
            }
        }
        return 5;
    }

    public static TavernCard randomCard(){
        CardSuit[] suits = CardSuit.values();
        CardSuit suit = suits[RNG.nextInt(suits.length)];
        int value = rollWeightedValue(suit);
        return new TavernCard(suit, value);
    }

    public static List<TavernCard> dealHand(int count){
        List<TavernCard> hand = new ArrayList<>();
        for(int i = 0; i < count; i++){
            hand.add(randomCard());
        }
        return hand;
    }
}