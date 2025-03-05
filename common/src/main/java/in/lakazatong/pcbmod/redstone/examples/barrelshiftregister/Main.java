package in.lakazatong.pcbmod.redstone.examples.barrelshiftregister;

import in.lakazatong.pcbmod.redstone.Circuit;
import in.lakazatong.pcbmod.redstone.Structure;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        String wd = Paths.get("common/src/main/java/in/lakazatong/pcbmod/redstone/examples/barrelshiftregister").toAbsolutePath().toString();

        Structure structure = Structure.fromNBT(Path.of(wd, "barrel_shift_register.nbt"));
        System.out.println(structure);

        Circuit circuit = Circuit.fromStructure(structure);
        circuit.saveAsDot(Path.of(wd, "barrel_shift_register.dot"));
    }
}
