package net.lakazatong.pcbmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.lakazatong.pcbmod.block.ModBlocks;
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

    public static int colorAt(int side) {
        return switch (side) {
            case 0 -> 0xFF0000;  // Red
            case 1 -> 0x00FF00;  // Green
            case 2 -> 0x0000FF;  // Blue
            case 3 -> 0xFFFF00;  // Yellow
            case 4 -> 0xFF00FF;  // Magenta
            case 5 -> 0x00FFFF;  // Cyan
            default -> 0xFFFFFF; // White
        };
    }

    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.BLOCK.register(
                (state, view, pos, tintIndex) -> colorAt(tintIndex)
                , ModBlocks.HUB);

        // Payload handlers
        ClientPlayNetworking.registerGlobalReceiver(OpenPortScreenPayload.ID, PCBModClient::handleOpenPortScreenPayload);
        ClientPlayNetworking.registerGlobalReceiver(OpenHubScreenPayload.ID, PCBModClient::handleOpenHubScreenPayload);
    }
}
