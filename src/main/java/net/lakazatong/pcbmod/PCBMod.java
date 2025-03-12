package net.lakazatong.pcbmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lakazatong.pcbmod.payloads.DirtBrokenPayload;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class PCBMod implements ModInitializer {

    public static final String MOD_ID = "pcbmod";

    private Integer totalDirtBlocksBroken = 0;

    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(DirtBrokenPayload.ID, DirtBrokenPayload.CODEC);

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
            if (state.getBlock() == Blocks.GRASS_BLOCK || state.getBlock() == Blocks.DIRT) {
                // Send a packet to the client
                MinecraftServer server = world.getServer();
                assert server != null;

                // Increment the amount of dirt blocks that have been broken
                totalDirtBlocksBroken += 1;

                ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(player.getUuid());
                server.execute(() -> {
                    assert playerEntity != null;
                    ServerPlayNetworking.send(playerEntity, new DirtBrokenPayload(totalDirtBlocksBroken));
                });
            }
        });
    }
}
