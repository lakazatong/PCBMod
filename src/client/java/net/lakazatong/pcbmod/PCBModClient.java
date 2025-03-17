package net.lakazatong.pcbmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.lakazatong.pcbmod.block.ModBlocks;
import net.lakazatong.pcbmod.block.custom.HubBlock;
import net.lakazatong.pcbmod.payloads.OpenHubScreenPayload;
import net.lakazatong.pcbmod.payloads.OpenPortScreenPayload;
import net.lakazatong.pcbmod.screen.HubScreen;
import net.lakazatong.pcbmod.screen.PortScreen;
import net.minecraft.client.MinecraftClient;

public class PCBModClient implements ClientModInitializer {
    private static void handleOpenPortScreenPayload(OpenPortScreenPayload payload, ClientPlayNetworking.Context context) {
        MinecraftClient.getInstance().setScreen(new PortScreen(payload.pos()));
    }

    private static void handleOpenHubScreenPayload(OpenHubScreenPayload payload, ClientPlayNetworking.Context context) {
        MinecraftClient.getInstance().setScreen(new HubScreen(payload.pos()));
    }

    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.BLOCK.register(
                (state, view, pos, tintIndex) -> HubBlock.Side.colorAt(tintIndex)
                , ModBlocks.HUB);

        // Payload handlers
        ClientPlayNetworking.registerGlobalReceiver(OpenPortScreenPayload.ID, PCBModClient::handleOpenPortScreenPayload);
        ClientPlayNetworking.registerGlobalReceiver(OpenHubScreenPayload.ID, PCBModClient::handleOpenHubScreenPayload);
    }
}
