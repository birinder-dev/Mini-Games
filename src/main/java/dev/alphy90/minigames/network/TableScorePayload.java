package dev.alphy90.minigames.network;

import dev.alphy90.minigames.MiniGames;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record TableScorePayload(int playerScore, int gamblerScore, int roundResult, int currentRound, int newCardSuit, int newCardValue) implements CustomPayload {
    public static final Id<TableScorePayload> ID = new Id<>(MiniGames.id("table_score"));
    public static final PacketCodec<RegistryByteBuf, TableScorePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, TableScorePayload::playerScore,
            PacketCodecs.VAR_INT, TableScorePayload::gamblerScore,
            PacketCodecs.VAR_INT, TableScorePayload::roundResult,
            PacketCodecs.VAR_INT, TableScorePayload::currentRound,
            PacketCodecs.VAR_INT, TableScorePayload::newCardSuit,
            PacketCodecs.VAR_INT, TableScorePayload::newCardValue,
            TableScorePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

