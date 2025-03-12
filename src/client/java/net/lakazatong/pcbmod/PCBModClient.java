package net.lakazatong.pcbmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.lakazatong.pcbmod.payloads.NewStructurePayload;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

public class PCBModClient implements ClientModInitializer {
    private static void handleDirtBrokenPayload(NewStructurePayload payload, ClientPlayNetworking.Context context) {
        ClientPlayerEntity player = context.client().player;
        assert player != null;
        player.sendMessage(Text.literal("Total dirt blocks broken: " + payload.totalDirtBlocksBroken()), false);
    }

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(NewStructurePayload.ID, PCBModClient::handleDirtBrokenPayload);
    }
}
