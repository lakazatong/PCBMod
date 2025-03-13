package net.lakazatong.pcbmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.lakazatong.pcbmod.block.ModBlocks;
import net.lakazatong.pcbmod.block.custom.PortBlock;
import net.lakazatong.pcbmod.payloads.OpenPortScreenPayload;
import net.lakazatong.pcbmod.screen.PortScreen;
import net.minecraft.client.MinecraftClient;

public class PCBModClient implements ClientModInitializer {
//    private static void handleDirtBrokenPayload(NewCircuitPayload payload, ClientPlayNetworking.Context context) {
//        ClientPlayerEntity player = context.client().player;
//        assert player != null;
//        player.sendMessage(Text.literal("Total dirt blocks broken: " + payload.totalDirtBlocksBroken()), false);
//    }

    private static void handlePortScreenPayload(OpenPortScreenPayload payload, ClientPlayNetworking.Context context) {
        MinecraftClient.getInstance().setScreen(new PortScreen());
    }

    @Override
    public void onInitializeClient() {
//        ClientPlayNetworking.registerGlobalReceiver(NewCircuitPayload.ID, PCBModClient::handleDirtBrokenPayload);
        ClientPlayNetworking.registerGlobalReceiver(OpenPortScreenPayload.ID, PCBModClient::handlePortScreenPayload);

        ColorProviderRegistry.BLOCK.register(
                (state, view, pos, tintIndex) ->
                        state.get(PortBlock.SIDE).getColor()
                , ModBlocks.PORT);
    }
}
