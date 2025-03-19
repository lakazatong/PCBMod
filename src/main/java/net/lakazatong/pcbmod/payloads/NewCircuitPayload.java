package net.lakazatong.pcbmod.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static net.lakazatong.pcbmod.PCBMod.MOD_ID;

public record NewCircuitPayload(String circuitName, String structurePath) implements CustomPayload {
    public static final Id<NewCircuitPayload> ID = new Id<>(Identifier.of(MOD_ID, "new_circuit"));
    public static final PacketCodec<PacketByteBuf, NewCircuitPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, NewCircuitPayload::circuitName,
            PacketCodecs.STRING, NewCircuitPayload::structurePath,
            NewCircuitPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
