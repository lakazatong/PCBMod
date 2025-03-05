package in.lakazatong.pcbmod.redstone.parser.examples.and_gate;

import in.lakazatong.pcbmod.redstone.parser.Parser;
import in.lakazatong.pcbmod.redstone.parser.Structure;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        String wd = Paths.get("common/src/main/java/in/lakazatong/pcbmod/redstone/parser/examples/and_gate").toAbsolutePath().toString();

        Structure structure = Structure.fromNBT(Path.of(wd, "and_gate.nbt"));
        System.out.println(structure);

        Parser p = new Parser(structure);
        p.parse();
        p.saveGraphAsDot(Path.of(wd, "and_gate.dot"));
    }
}
