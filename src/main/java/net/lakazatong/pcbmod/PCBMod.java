package net.lakazatong.pcbmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lakazatong.pcbmod.block.ModBlockEntities;
import net.lakazatong.pcbmod.block.ModBlocks;
import net.lakazatong.pcbmod.block.custom.PortBlock;
import net.lakazatong.pcbmod.block.entity.HubBlockEntity;
import net.lakazatong.pcbmod.block.entity.PortBlockEntity;
import net.lakazatong.pcbmod.item.ModItems;
import net.lakazatong.pcbmod.payloads.*;
import net.lakazatong.pcbmod.redstone.circuit.Circuit;
import net.lakazatong.pcbmod.redstone.circuit.Circuits;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.nio.file.Path;

public class PCBMod implements ModInitializer {

    public static final String MOD_ID = "pcbmod";

    public static Path STRUCTURES_PATH;

    public static Circuits CIRCUITS;

    public static boolean DEBUG = true;

    public static MutableText translate(String category, String... keys) {
        return Text.translatable(String.join(".", category, PCBMod.MOD_ID, String.join(".", keys)));
    }

    private static void handleUpdatePortPayload(UpdatePortPayload payload, ServerPlayNetworking.Context context) {
        if (context.player().getServerWorld().getBlockEntity(payload.pos()) instanceof PortBlockEntity be) {
            be.setPortNumber(payload.portNumber());
            be.setPortType(PortBlock.PortType.of(payload.portType()));
        }
    }

    private static void handleUpdateHubPayload(UpdateHubPayload payload, ServerPlayNetworking.Context context) {
        BlockPos pos = payload.pos();
        if (context.player().getServerWorld().getBlockEntity(pos) instanceof HubBlockEntity be) {
            be.setStructureName(payload.structureName());
            be.setInstanceId(payload.instanceId());
            be.setPortNumbers(payload.portNumbers().stream().mapToInt(Integer::intValue).toArray());

            String circuitName = be.getCircuitName();
            if (Circuits.isValidCircuitName(circuitName))
                CIRCUITS.get(circuitName).hubs.add(pos);
        }
    }

    private static void handleNewCircuitPayload(NewCircuitPayload payload, ServerPlayNetworking.Context context) {
        try {
            String circuitName = payload.circuitName();
            Path structurePath = Path.of(payload.structurePath());
            Circuit circuit = new Circuit(structurePath);
            CIRCUITS.put(circuitName, circuit);
            System.out.println("New circuit with structure at: " + structurePath.toAbsolutePath() + " (structureName: " + Circuit.structureNameFrom(circuitName) + ", instanceId: " + Circuit.instanceIdFrom(circuitName) + ")");
        } catch (IOException ignored) {
        }
    }

    public void onInitialize() {
        DEBUG = false;

        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStart);

        ModItems.initialize();
        ModBlocks.initialize();
        ModBlockEntities.initialize();

        // Payloads
        PayloadTypeRegistry.playS2C().register(OpenPortScreenPayload.ID, OpenPortScreenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenHubScreenPayload.ID, OpenHubScreenPayload.CODEC);

        PayloadTypeRegistry.playC2S().register(UpdatePortPayload.ID, UpdatePortPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateHubPayload.ID, UpdateHubPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(NewCircuitPayload.ID, NewCircuitPayload.CODEC);

        // Payload handlers
        ServerPlayNetworking.registerGlobalReceiver(UpdatePortPayload.ID, PCBMod::handleUpdatePortPayload);
        ServerPlayNetworking.registerGlobalReceiver(UpdateHubPayload.ID, PCBMod::handleUpdateHubPayload);
        ServerPlayNetworking.registerGlobalReceiver(NewCircuitPayload.ID, PCBMod::handleNewCircuitPayload);
    }

    private void onServerStart(MinecraftServer server) {
        STRUCTURES_PATH = server.getSavePath(WorldSavePath.GENERATED).resolve("minecraft/structures");
        System.out.println("Structures path: " + STRUCTURES_PATH);
        CIRCUITS = Circuits.init(server);

        System.out.println("CIRCUITS size: " + CIRCUITS.size());
    }
}
