package dev.alphy90.minigames.client;

public class CardClientState {
    private static boolean active = false;
    private static int cardCount = 1;

    public static boolean isActive(){
        return active;
    }

    public static void setActive(boolean isActive){
        active = isActive;
    }

    public static int getCardCount(){
        return cardCount;
    }

    public static void setCardCount(int count){
        cardCount = Math.max(1, Math.min(9, count));
        if(cardCount == 0){
            active = false;
        }
    }

    public static boolean toggle(int requestedCount){
        if(active && (requestedCount <= 0 || requestedCount == cardCount)){
            active = false;
        }
        else{
            active = true;
            if(requestedCount>0) setCardCount(requestedCount);
        }
        return active;
    }

    public static int getStartSlotIndex(int count){
        return 4 - (count/2);
    }

    public static int getCardIndexForSlot(int slot, int count){
        int start = getStartSlotIndex(count);
        int cardIndex = slot - start;
        return (cardIndex >= 0 && cardIndex < count)? cardIndex : -1;
    }

    public static int getSlotForCardIndex(int cardIndex, int count){
        return getStartSlotIndex(count) + cardIndex;
    }

    public static boolean isCardSlot(int slot){
        return active && getCardIndexForSlot(slot, cardCount) != -1;
    }

    public static void playSelectedCard(int selectedSlot){
        int cardIndex = getCardIndexForSlot(selectedSlot, cardCount);

        if(cardIndex != -1){
            setCardCount(cardCount - 1 );

            if(cardCount <= 0 || !active){
                active = false;
            }
        }
    }
}

