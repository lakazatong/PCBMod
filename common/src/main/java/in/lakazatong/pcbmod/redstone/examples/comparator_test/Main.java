package in.lakazatong.pcbmod.redstone.examples.comparator_test;

import in.lakazatong.pcbmod.redstone.Circuit;
import in.lakazatong.pcbmod.redstone.Structure;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        String wd = Paths.get("common/src/main/java/in/lakazatong/pcbmod/redstone/examples/comparator_test").toAbsolutePath().toString();

        Structure structure = Structure.fromNBT(Path.of(wd, "comparator_test.nbt"));
        System.out.println(structure);

        Circuit circuit = Circuit.fromStructure(structure);
        circuit.saveAsDot(Path.of(wd, "comparator_test.dot"));
    }
}
