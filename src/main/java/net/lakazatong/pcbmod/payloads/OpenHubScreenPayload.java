package net.lakazatong.pcbmod.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import static net.lakazatong.pcbmod.PCBMod.MOD_ID;

public record OpenHubScreenPayload(BlockPos pos) implements CustomPayload {
    public static final Id<OpenHubScreenPayload> ID = new Id<>(Identifier.of(MOD_ID, "open_hub_screen"));
    public static final PacketCodec<PacketByteBuf, OpenHubScreenPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, OpenHubScreenPayload::pos,
            OpenHubScreenPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
