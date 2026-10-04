package dev.alphy90.minigames.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerCardSyncState {
    public record State(boolean active, int count, boolean charging){}
    private static final Map<Integer, State> PLAYER_STATES = new ConcurrentHashMap<>();
    public static void update(int entityId, boolean active, int count, boolean charging){
        if(!active){
            PLAYER_STATES.remove(entityId);
        } else {
            PLAYER_STATES.put(entityId, new State(active, count, charging));
        }
    }

    public static boolean isPlayerInCardState(PlayerEntity player){
        if(player == null) return false;
        State s = PLAYER_STATES.get(player.getId());
        return s != null && s.active();
    }

    public static boolean isPlayerCharging(PlayerEntity player){
        if(player == null) return false;
        State s = PLAYER_STATES.get(player.getId());
        return s != null && s.charging();
    }
}