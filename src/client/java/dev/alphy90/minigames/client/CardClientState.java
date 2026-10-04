package dev.alphy90.minigames.client;

import dev.alphy90.minigames.config.ModConfig;

public class CardClientState {
    private static boolean active = false;
    private static int cardCount = 1;

    private static int holdTicks = 0;
    private static boolean throwCharging = false;

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
        int max = ModConfig.get().maxCardsInHand;
        cardCount = Math.max(0, Math.min(max, count));
        if(cardCount == 0){
            active = false;
            resetHold();
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

    public static void removeOneCard(){
        setCardCount(cardCount - 1);
        if(cardCount <= 0){
            active = false;
        }
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

    //hold &charge logic

    public static int getHoldTicks(){
        return holdTicks;
    }

    public static void incrementHold(){
        holdTicks++;
        //1sec to initiate
        if(holdTicks >= 20){
            throwCharging = true;
        }
    }

    public static boolean isThrowCharging(){
        return throwCharging;
    }

    public static float getChargeProgress(){
        if(holdTicks < 20) return 0.0F;
        return Math.min(1.0F, (holdTicks - 20) / 40.0F);
    }

    public static float getThrowPower(){
        if(holdTicks < 20) return 0.0F;

        //next 3sec, 0.15->1.0
        int chargeTicks = Math.min(60, holdTicks - 20);
        return (float) (0.15 + (0.85 * (chargeTicks / 60.0F)));
    }

    public static void resetHold(){
        holdTicks = 0;
        throwCharging = false;
    }

    public static final int TOTAL_FLICK_TICKS = 4;
    private static int flickTicks = 0;
    private static float pendingPower = 0.0F;
    private static boolean pendingThrow = false;

    public static void startFlick(float power){
        flickTicks = TOTAL_FLICK_TICKS;
        pendingPower = power;
        pendingThrow = true;
        resetHold();
    }

    public static boolean isFlicking(){
        return flickTicks > 0;
    }

    public static int getFlickTicks(){
        return flickTicks;
    }

    public static float getPendingPower(){
        return pendingPower;
    }

    public static boolean hasPendingThrow(){
        return pendingThrow;
    }

    public static void tickFlick(){
        if(flickTicks > 0){
            flickTicks--;
        }
    }

    public static void completeThrow(){
        pendingThrow = false;
        pendingPower = 0.0F;
        flickTicks = 0;
    }

    // centred hotbar slots
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

    public static int originSlot = -1;

    public static int getOriginSlot(){
        return originSlot;
    }

    public static void setOriginSlot(int slot){
        originSlot = slot;
    }
}

