package dev.alphy90.minigames.network;

import dev.alphy90.minigames.MiniGames;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record CardStateC2SPayload(boolean active, int count, boolean charging) implements CustomPayload{
    public static final Id<CardStateC2SPayload> ID = new Id<>(MiniGames.id("card_state_c2s"));
    public static final PacketCodec<RegistryByteBuf, CardStateC2SPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL,
            CardStateC2SPayload::active,
            PacketCodecs.INTEGER,
            CardStateC2SPayload::count,
            PacketCodecs.BOOL,
            CardStateC2SPayload::charging,
            CardStateC2SPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId(){
        return ID;
    }
}