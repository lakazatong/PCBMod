package net.lakazatong.pcbmod.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

import static net.lakazatong.pcbmod.PCBMod.MOD_ID;

public record UpdateHubPayload(
        BlockPos pos,
        String structureName,
        Integer instanceId,
        List<Integer> portNumbers
) implements CustomPayload {
    public static final Id<UpdateHubPayload> ID = new Id<>(Identifier.of(MOD_ID, "update_hub"));
    public static final PacketCodec<PacketByteBuf, UpdateHubPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, UpdateHubPayload::pos,
            PacketCodecs.STRING, UpdateHubPayload::structureName,
            PacketCodecs.VAR_INT, UpdateHubPayload::instanceId,
            PacketCodecs.collection(ArrayList::new, PacketCodecs.VAR_INT, 6), UpdateHubPayload::portNumbers,
            UpdateHubPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
