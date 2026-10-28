package dev.alphy90.minigames.network;

import dev.alphy90.minigames.MiniGames;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record StartTableGamePayload(BlockPos tablePos, int betAmount, int cardCount, int difficulty) implements CustomPayload{
    public static final Id<StartTableGamePayload> ID = new Id<>(MiniGames.id("start_table_game"));
    public static final PacketCodec<RegistryByteBuf, StartTableGamePayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, StartTableGamePayload::tablePos,
            PacketCodecs.VAR_INT, StartTableGamePayload::betAmount,
            PacketCodecs.VAR_INT, StartTableGamePayload::cardCount,
            PacketCodecs.VAR_INT, StartTableGamePayload::difficulty,
            StartTableGamePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId(){
        return ID;
    }
}