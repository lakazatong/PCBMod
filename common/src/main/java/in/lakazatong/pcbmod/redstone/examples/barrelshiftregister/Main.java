package in.lakazatong.pcbmod.redstone.examples.barrelshiftregister;

import in.lakazatong.pcbmod.redstone.Circuit;
import in.lakazatong.pcbmod.redstone.Structure;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        Path nbtPath = Path.of("common/src/main/java/in/lakazatong/pcbmod/redstone/examples/barrelshiftregister/barrel_shift_register.nbt");
        Structure structure = Structure.fromNBT(nbtPath);
        System.out.println(structure);

        Circuit circuit = new Circuit(structure);
        circuit.simulateUntilUnchanged();
    }
}
