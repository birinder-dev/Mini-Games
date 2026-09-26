package dev.alphy90.minigames.network;

import dev.alphy90.minigames.MiniGames;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public record PlaceCardPayload(BlockPos pos, Direction side) implements CustomPayload {
    public static final Id<PlaceCardPayload> ID = new Id<>(MiniGames.id("place_card"));
    public static final PacketCodec<RegistryByteBuf, PlaceCardPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, PlaceCardPayload::pos,
            Direction.PACKET_CODEC, PlaceCardPayload::side,
            PlaceCardPayload::new
    );

    @Override
    public Id<?extends CustomPayload> getId(){
        return ID;
    }
}