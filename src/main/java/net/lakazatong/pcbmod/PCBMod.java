package net.lakazatong.pcbmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lakazatong.pcbmod.payloads.NewStructurePayload;
import net.lakazatong.pcbmod.redstone.circuit.Circuits;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class PCBMod implements ModInitializer {

    public static final String MOD_ID = "pcbmod";

    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(NewStructurePayload.ID, NewStructurePayload.CODEC);

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            world.getBlockState(hitResult.getBlockPos()).getBlock().equals(Blocks.STRUCTURE_BLOCK)
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
            if (state.getBlock() == Blocks.GRASS_BLOCK || state.getBlock() == Blocks.DIRT) {
                // Send a packet to the client
                MinecraftServer server = world.getServer();
                assert server != null;

                // Increment the amount of dirt blocks that have been broken
                Circuits circuits = Circuits.getServerState(world.getServer());
                circuits.totalDirtBlocksBroken += 1;

                ServerPlayerEntity playerEntity = server.getPlayerManager().getPlayer(player.getUuid());
                server.execute(() -> {
                    assert playerEntity != null;
                    ServerPlayNetworking.send(playerEntity, new NewStructurePayload(circuits.totalDirtBlocksBroken));
                });
            }
        });
    }
}
