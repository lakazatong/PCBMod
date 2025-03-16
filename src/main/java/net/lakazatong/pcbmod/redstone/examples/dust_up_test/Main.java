package net.lakazatong.pcbmod.redstone.examples.dust_up_test;

import net.lakazatong.pcbmod.redstone.circuit.Circuit;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        Path nbtPath = Path.of("src/main/java/net/lakazatong/pcbmod/redstone/examples/dust_up_test/dust_up_test.nbt");
        Circuit circuit = new Circuit(nbtPath);
        circuit.simulateUntilUnchanged();
    }
}
