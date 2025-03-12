package net.lakazatong.pcbmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.lakazatong.pcbmod.payloads.DirtBrokenPayload;
import net.minecraft.text.Text;

import java.util.Objects;

public class PCBModClient implements ClientModInitializer {
    private static void handleDirtBrokenPayload(DirtBrokenPayload payload, ClientPlayNetworking.Context context) {
        Objects.requireNonNull(context.client().player).sendMessage(Text.literal("Total dirt blocks broken: " + payload.totalDirtBlocksBroken()), false);
    }

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(DirtBrokenPayload.ID, PCBModClient::handleDirtBrokenPayload);
    }
}