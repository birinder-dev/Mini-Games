package dev.alphy90.minigames.network;

import dev.alphy90.minigames.MiniGames;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record ThrowCardPayLoad(float power) implements CustomPayload {
    public static final CustomPayload.Id<ThrowCardPayLoad> ID = new CustomPayload.Id<>(MiniGames.id("throw_card"));

    public static final PacketCodec<RegistryByteBuf, ThrowCardPayLoad> CODEC = PacketCodec.tuple(
            PacketCodecs.FLOAT, ThrowCardPayLoad::power,
            ThrowCardPayLoad::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId(){
        return ID;
    }
}