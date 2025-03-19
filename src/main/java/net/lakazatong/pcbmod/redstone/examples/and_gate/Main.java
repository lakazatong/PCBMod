package net.lakazatong.pcbmod.redstone.examples.and_gate;

import net.lakazatong.pcbmod.redstone.circuit.Circuit;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        Path nbtPath = Path.of("src/main/java/net/lakazatong/pcbmod/redstone/examples/and_gate/and_gate.nbt");
        Circuit circuit = new Circuit(nbtPath);
        circuit.stabilize();
        circuit.setInputSignal(1, 15);
        circuit.setInputSignal(2, 15);
        circuit.stabilize();
        circuit.setInputSignal(1, 0);
        circuit.setInputSignal(2, 0);
        circuit.stabilize();
        circuit.animate();
    }
}
