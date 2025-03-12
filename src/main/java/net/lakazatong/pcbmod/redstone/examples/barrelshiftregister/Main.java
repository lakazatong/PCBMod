package net.lakazatong.pcbmod.redstone.examples.barrelshiftregister;

import net.lakazatong.pcbmod.redstone.circuit.Circuit;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        Path nbtPath = Path.of("common/src/main/java/net/lakazatong/pcbmod/redstone/examples/barrelshiftregister/barrel_shift_register.nbt");
        Circuit circuit = new Circuit(nbtPath);
        circuit.simulateUntilUnchanged();
    }
}
