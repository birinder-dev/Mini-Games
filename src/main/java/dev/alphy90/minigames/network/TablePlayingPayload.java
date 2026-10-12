package dev.alphy90.minigames.network;

import dev.alphy90.minigames.MiniGames;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;


public record TablePlayingPayload(boolean isTablePlaying) implements CustomPayload {
    public static final Id<TablePlayingPayload> ID = new Id<>(MiniGames.id("table_playing"));
    public static final PacketCodec<RegistryByteBuf, TablePlayingPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL,
            TablePlayingPayload::isTablePlaying,
            TablePlayingPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId(){
        return ID;
    }
}