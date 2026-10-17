package dev.alphy90.minigames.network;

import dev.alphy90.minigames.MiniGames;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record PlayTableCardPayLoad(BlockPos tablePos, int suitOrdinal, int value) implements CustomPayload {
    public static final Id<PlayTableCardPayLoad> ID = new Id<>(MiniGames.id("play_table_card"));
    public static final PacketCodec<RegistryByteBuf, PlayTableCardPayLoad> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC,
            PlayTableCardPayLoad::tablePos,
            PacketCodecs.INTEGER,
            PlayTableCardPayLoad::suitOrdinal,
            PacketCodecs.INTEGER,
            PlayTableCardPayLoad::value,
            PlayTableCardPayLoad::new
    );

    @Override
    public Id<? extends CustomPayload> getId(){
        return ID;
    }
}