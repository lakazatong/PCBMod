package net.lakazatong.pcbmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.lakazatong.pcbmod.payloads.DirtBrokenPayload;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

public class PCBModClient implements ClientModInitializer {
    private static void handleDirtBrokenPayload(DirtBrokenPayload payload, ClientPlayNetworking.Context context) {
        ClientPlayerEntity player = context.client().player;
        assert player != null;
        player.sendMessage(Text.literal("Total dirt blocks broken: " + payload.totalDirtBlocksBroken()), false);
    }

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(DirtBrokenPayload.ID, PCBModClient::handleDirtBrokenPayload);
    }
}
