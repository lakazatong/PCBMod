package net.lakazatong.pcbmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lakazatong.pcbmod.block.ModBlockEntities;
import net.lakazatong.pcbmod.block.ModBlocks;
import net.lakazatong.pcbmod.block.custom.HubBlock;
import net.lakazatong.pcbmod.block.custom.PortBlock;
import net.lakazatong.pcbmod.block.entity.HubBlockEntity;
import net.lakazatong.pcbmod.block.entity.PortBlockEntity;
import net.lakazatong.pcbmod.item.ModItems;
import net.lakazatong.pcbmod.payloads.OpenHubScreenPayload;
import net.lakazatong.pcbmod.payloads.OpenPortScreenPayload;
import net.lakazatong.pcbmod.payloads.UpdateHubPayload;
import net.lakazatong.pcbmod.payloads.UpdatePortPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.nio.file.Path;

public class PCBMod implements ModInitializer {

    public static final String MOD_ID = "pcbmod";

    public static Path structuresPath;

    private static void handleUpdatePortPayload(UpdatePortPayload payload, ServerPlayNetworking.Context context) {
        if (context.player().getServerWorld().getBlockEntity(payload.pos()) instanceof PortBlockEntity be) {
            be.setPortNumber(payload.portNumber());
            be.setPortType(PortBlock.PortType.of(payload.portType()));
        }
    }

    private static void handleUpdateHubPayload(UpdateHubPayload payload, ServerPlayNetworking.Context context) {
        if (context.player().getServerWorld().getBlockEntity(payload.pos()) instanceof HubBlockEntity be) {
            be.setPortNumberAt(HubBlock.Side.FRONT.ordinal(), payload.frontPortNumber());
            be.setPortNumberAt(HubBlock.Side.BACK.ordinal(), payload.backPortNumber());
            be.setPortNumberAt(HubBlock.Side.LEFT.ordinal(), payload.leftPortNumber());
            be.setPortNumberAt(HubBlock.Side.RIGHT.ordinal(), payload.rightPortNumber());
            be.setPortNumberAt(HubBlock.Side.UP.ordinal(), payload.upPortNumber());
            be.setPortNumberAt(HubBlock.Side.DOWN.ordinal(), payload.downPortNumber());
        }
    }

    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStart);

        ModItems.initialize();
        ModBlocks.initialize();
        ModBlockEntities.initialize();

        // Payloads
        PayloadTypeRegistry.playS2C().register(OpenPortScreenPayload.ID, OpenPortScreenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenHubScreenPayload.ID, OpenHubScreenPayload.CODEC);

        PayloadTypeRegistry.playC2S().register(UpdatePortPayload.ID, UpdatePortPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateHubPayload.ID, UpdateHubPayload.CODEC);

        // Payload handlers
        ServerPlayNetworking.registerGlobalReceiver(UpdatePortPayload.ID, PCBMod::handleUpdatePortPayload);
        ServerPlayNetworking.registerGlobalReceiver(UpdateHubPayload.ID, PCBMod::handleUpdateHubPayload);
    }

    private void onServerStart(MinecraftServer server) {
        structuresPath = server.getSavePath(WorldSavePath.GENERATED).resolve("minecraft/structures");
        System.out.println("Structures path: " + structuresPath);
    }
}
