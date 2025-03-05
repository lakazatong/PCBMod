package in.lakazatong.pcbmod.redstone.parser.examples.and_gate;

import in.lakazatong.pcbmod.redstone.parser.Structure;
import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        Structure w = Structure.fromNBT(Path.of("common/src/main/java/in/lakazatong/pcbmod/redstone/parser/examples/and_gate/and_gate.nbt"));
//        System.out.println(w);
//
//        Parser p = new Parser(w);
//        Object G = p.parse();
//
//        System.out.println(G);
    }
}
