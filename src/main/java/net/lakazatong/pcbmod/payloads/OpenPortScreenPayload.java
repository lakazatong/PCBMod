package net.lakazatong.pcbmod.payloads;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static net.lakazatong.pcbmod.PCBMod.MOD_ID;

public record OpenPortScreenPayload(boolean ignored) implements CustomPayload {
    public static final CustomPayload.Id<OpenPortScreenPayload> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "open_port_screen"));
    public static final PacketCodec<PacketByteBuf, OpenPortScreenPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOLEAN, OpenPortScreenPayload::ignored,
            OpenPortScreenPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
