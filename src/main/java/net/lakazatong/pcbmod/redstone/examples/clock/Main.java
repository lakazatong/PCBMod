package net.lakazatong.pcbmod.redstone.examples.clock;

import net.lakazatong.pcbmod.redstone.circuit.Circuit;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        Path nbtPath = Path.of("src/main/java/net/lakazatong/pcbmod/redstone/examples/clock/clock.nbt");
        Circuit circuit = new Circuit(nbtPath);
        circuit.stabilize();
        circuit.animate();
    }
}
