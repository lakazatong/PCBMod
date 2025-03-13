package net.lakazatong.pcbmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.lakazatong.pcbmod.block.ModBlocks;
import net.lakazatong.pcbmod.item.ModItems;
import net.lakazatong.pcbmod.payloads.NewCircuitPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.nio.file.Path;

public class PCBMod implements ModInitializer {

    public static final String MOD_ID = "pcbmod";

    public static Path structuresPath;

    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(NewCircuitPayload.ID, NewCircuitPayload.CODEC);
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStart);

        ModItems.initialize();
        ModBlocks.initialize();
    }

    private void onServerStart(MinecraftServer server) {
        structuresPath = server.getSavePath(WorldSavePath.GENERATED).resolve("minecraft/structures");
        System.out.println("Structures path: " + structuresPath);
    }
}
