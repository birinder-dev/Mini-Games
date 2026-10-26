package dev.alphy90.minigames.network;

import dev.alphy90.minigames.MiniGames;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record OpenTableScreenPayload(BlockPos tablePos) implements CustomPayload {
    public static final Id<OpenTableScreenPayload> ID = new Id<> (MiniGames.id("open_table_screen"));
    public static final PacketCodec<RegistryByteBuf, OpenTableScreenPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC,
            OpenTableScreenPayload::tablePos,
            OpenTableScreenPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId(){
        return ID;
    }
}