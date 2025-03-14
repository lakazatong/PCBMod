package net.lakazatong.pcbmod.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import static net.lakazatong.pcbmod.PCBMod.MOD_ID;

public record UpdateHubPayload(BlockPos pos, String structureName,
                               Integer frontPortNumber, Integer backPortNumber,
                               Integer leftPortNumber, Integer rightPortNumber,
                               Integer upPortNumber, Integer downPortNumber
) implements CustomPayload {
    public static final Id<UpdateHubPayload> ID = new Id<>(Identifier.of(MOD_ID, "update_hub"));
    public static final PacketCodec<PacketByteBuf, UpdateHubPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, UpdateHubPayload::pos,
            PacketCodecs.STRING, UpdateHubPayload::structureName,
            PacketCodecs.INTEGER, UpdateHubPayload::frontPortNumber, PacketCodecs.INTEGER, UpdateHubPayload::backPortNumber,
            PacketCodecs.INTEGER, UpdateHubPayload::leftPortNumber, PacketCodecs.INTEGER, UpdateHubPayload::rightPortNumber,
            PacketCodecs.INTEGER, UpdateHubPayload::upPortNumber, PacketCodecs.INTEGER, UpdateHubPayload::downPortNumber,
            UpdateHubPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
