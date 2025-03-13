package net.lakazatong.pcbmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.lakazatong.pcbmod.block.ModBlocks;
import net.lakazatong.pcbmod.block.custom.PortBlock;
import net.lakazatong.pcbmod.payloads.NewCircuitPayload;
import net.minecraft.client.network.ClientPlayerEntity;

public class PCBModClient implements ClientModInitializer {
    private static void handleDirtBrokenPayload(NewCircuitPayload payload, ClientPlayNetworking.Context context) {
        ClientPlayerEntity player = context.client().player;
        assert player != null;
//        player.sendMessage(Text.literal("Total dirt blocks broken: " + payload.totalDirtBlocksBroken()), false);
    }

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(NewCircuitPayload.ID, PCBModClient::handleDirtBrokenPayload);

        ColorProviderRegistry.BLOCK.register(
                (state, view, pos, tintIndex) ->
                        state.get(PortBlock.SIDE).getColor()
                , ModBlocks.PORT);
    }
}
