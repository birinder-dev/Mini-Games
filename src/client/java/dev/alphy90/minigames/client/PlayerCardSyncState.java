package dev.alphy90.minigames.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerCardSyncState {
    public record State(boolean active, int count, boolean charging) {
    }

    private static final Map<Integer, State> PLAYER_STATES = new ConcurrentHashMap<>();

    public static void update(int entityId, boolean active, int count, boolean charging) {
        if (!active) {
            PLAYER_STATES.remove(entityId);
        } else {
            PLAYER_STATES.put(entityId, new State(active, count, charging));
        }
    }

    private static final Map<Integer, PlayerAnim> PLAYER_ANIMS = new ConcurrentHashMap<>();

    public static class PlayerAnim {
        public float chargeProgress = 0.0F;
        public float prevChargeProgress = 0.0F;
        public float activeProgress = 0.0F;
        public float prevActiveProgress = 0.0F;
        public int flickTicks = 0;
        public boolean wasCharging = false;

        public void tick(boolean isActive, boolean isCharging){
            prevChargeProgress = chargeProgress;
            prevActiveProgress = activeProgress;

            if(isActive){
                activeProgress = Math.min(1.0F, activeProgress + 0.20F);
            } else {
                activeProgress = Math.max(0.0F, activeProgress - 0.20F);
            }

            if(wasCharging && !isCharging && isActive){
                flickTicks = 4;
            }
            wasCharging = isCharging;

            if(flickTicks > 0){
                flickTicks--;
            }

            if(isCharging){
                chargeProgress = Math.min(1.0F, chargeProgress + 0.16F);
            } else {
                chargeProgress = Math.max(0.0F, chargeProgress - 0.25F);
            }
        }

        public float getInterpolatedCharge(float tickDelta){
            float val = MathHelper.lerp(tickDelta, prevChargeProgress, chargeProgress);
            val = MathHelper.clamp(val, 0.0F, 1.0F);
            float inv = 1.0F - val;
            return 1.0F - (inv * inv * inv);
        }

        public float getInterpolatedActive(float tickDelta){
            float val = MathHelper.lerp(tickDelta, prevActiveProgress, activeProgress);
            return MathHelper.clamp(val, 0.0F, 1.0F);
        }

        public float getFlickProgress(){
            return flickTicks > 0 ? (float) flickTicks / 4.0F : 0.0F;
        }
    }

    public static PlayerAnim getAnim(int entityId){
        return PLAYER_ANIMS.computeIfAbsent(entityId, id -> new PlayerAnim());
    }

    public static void tick(MinecraftClient client){
        if(client.world == null){
            PLAYER_ANIMS.clear();
            return;
        }
        if(client.player != null){
            getAnim(client.player.getId()).tick(CardClientState.isActive(), CardClientState.isThrowCharging());
        }
        PLAYER_STATES.forEach((id, state) -> {
            if(client.player == null || id != client.player.getId()){
                getAnim(id).tick(state.active(), state.charging());
            }
        });
        PLAYER_ANIMS.keySet().removeIf(id -> {
            if(client.player != null && id == client.player.getId()) return false;
            return !PLAYER_STATES.containsKey(id);
        });
    }

    public static boolean isPlayerInCardState(PlayerEntity player) {
        if (player == null)
            return false;
        State s = PLAYER_STATES.get(player.getId());
        return s != null && s.active();
    }

    public static boolean isPlayerCharging(PlayerEntity player) {
        if (player == null)
            return false;
        State s = PLAYER_STATES.get(player.getId());
        return s != null && s.charging();
    }

    public static int getCardCount(PlayerEntity player){
        if(player == null)
            return 1;
        State s = PLAYER_STATES.get(player.getId());
        return s != null ? s.count() : 1;
    }
}