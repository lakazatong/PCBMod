package net.lakazatong.pcbmod.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import static net.lakazatong.pcbmod.PCBMod.MOD_ID;

public record UpdatePortPayload(BlockPos pos, Integer portNumber, Integer portType) implements CustomPayload {
    public static final CustomPayload.Id<UpdatePortPayload> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "update_port"));
    public static final PacketCodec<PacketByteBuf, UpdatePortPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, UpdatePortPayload::pos,
            PacketCodecs.VAR_INT, UpdatePortPayload::portNumber,
            PacketCodecs.VAR_INT, UpdatePortPayload::portType,
            UpdatePortPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
