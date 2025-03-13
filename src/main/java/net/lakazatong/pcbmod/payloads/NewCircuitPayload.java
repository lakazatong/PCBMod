package net.lakazatong.pcbmod.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static net.lakazatong.pcbmod.PCBMod.MOD_ID;

public record NewCircuitPayload(String structureName) implements CustomPayload {
    public static final CustomPayload.Id<NewCircuitPayload> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "nbt_path"));
    public static final PacketCodec<PacketByteBuf, NewCircuitPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, NewCircuitPayload::structureName,
            NewCircuitPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
