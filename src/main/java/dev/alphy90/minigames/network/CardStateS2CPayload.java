package dev.alphy90.minigames.network;

import dev.alphy90.minigames.MiniGames;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record CardStateS2CPayload(int entityId, boolean active, int count, boolean charging) implements CustomPayload {
    public static final Id<CardStateS2CPayload> ID = new Id<>(MiniGames.id("card_state_s2c"));
    public static final PacketCodec<RegistryByteBuf, CardStateS2CPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER,
            CardStateS2CPayload::entityId,
            PacketCodecs.BOOL,
            CardStateS2CPayload::active,
            PacketCodecs.INTEGER,
            CardStateS2CPayload::count,
            PacketCodecs.BOOL,
            CardStateS2CPayload::charging,
            CardStateS2CPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId(){
        return ID;
    }
}