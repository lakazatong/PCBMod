package in.lakazatong.pcbmod.redstone;

import org.apache.commons.io.file.PathUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class Circuit {
    public final Structure structure;
    public final Map<UUID, Block> graph;
    private double time = 0; // in ticks

    private Circuit(Structure structure) {
        this.structure = structure;
        this.graph = new HashMap<>();
    }

    public void tick() {
        Map<UUID, Integer> nextSignals = new HashMap<>(graph.size());

        for (Block block : graph.values())
            nextSignals.put(block.uuid, block.tick(time));

        for (Map.Entry<UUID, Integer> entry : nextSignals.entrySet())
            graph.get(entry.getKey()).signal = entry.getValue();
    }

    public boolean hasChanged() {
        for (Block block : graph.values()) {
            if (block.hasChanged())
                return true;
        }
        return false;
    }

    public void simulateUntilUnchanged() {
        tick();
        while (hasChanged()) {
            time++;
            tick();
        }
    }

    public static Circuit fromStructure(Structure structure) {
        Circuit circuit = new Circuit(structure);
        Queue<Block> queue = new LinkedList<>();
        Set<UUID> visited = new HashSet<>();

        queue.add(structure.getFirstBlock());

        while (!queue.isEmpty()) {
            Block block = queue.poll();

            if (visited.contains(block.uuid))
                continue;
            visited.add(block.uuid);

            for (Block neighbor : structure.getNeighbors(block)) {
                if (neighbor.isInputOf(block))
                    block.inputs.add(neighbor);
                queue.add(neighbor);
            }

            circuit.graph.put(block.uuid, block);
        }

//        remove0TickNodes(circuit, BlockType.DUST);
//        remove0TickNodes(circuit, BlockType.SOLID);

        return circuit;
    }

    private static void remove0TickNodes(Circuit circuit, BlockType type) {
        for (Block block : circuit.graph.values()) {
            for (Block input : block.inputs)
                input.outputs.add(block);
        }

        Set<UUID> toRemove = new HashSet<>();

        for (Block block : circuit.graph.values()) {
            if (block.type == type) {
                for (Block input : block.inputs) {
                    for (Block output : block.outputs) {
                        if (input.uuid != output.uuid)
                            output.inputs.add(input);
                    }
                }
                toRemove.add(block.uuid);
            }
        }

        for (UUID uuid : toRemove)
            circuit.graph.remove(uuid);

        for (Block block : circuit.graph.values())
            block.inputs.removeIf(input -> toRemove.contains(input.uuid));

        for (Block block : circuit.graph.values())
            block.outputs.clear();
    }

    public void saveAsDot() throws IOException, InterruptedException {
        StringBuilder dotBuilder = new StringBuilder();
        dotBuilder.append("digraph G {\n");

        Set<String> edges = new HashSet<>();

        dotBuilder.append("    graph [bgcolor=\"black\"];\n");

        for (Map.Entry<UUID, Block> entry : graph.entrySet()) {
            UUID blockUUID = entry.getKey();
            Block block = entry.getValue();

            String blockName = block.type.name().toLowerCase();

            dotBuilder.append("    \"").append(blockUUID).append("\" [label=\"").append(blockName).append("\", style=filled, fillcolor=\"black\", fontcolor=\"white\", color=\"white\", width=0.2, height=0.2];\n");

            for (Block input : block.inputs) {
                String edge = "\"" + input.uuid + "\" -> \"" + blockUUID + "\"";
                if (!edges.contains(edge)) {
                    dotBuilder.append("    ").append(edge)
                            .append(" [color=\"white\", arrowhead=\"normal\", fontcolor=\"white\", penwidth=2];\n");
                    edges.add(edge);
                }
            }
        }

        dotBuilder.append("}\n");

        String dotPath = structure.path.getParent().resolve(PathUtils.getBaseName(structure.path) + ".dot").toAbsolutePath().toString();
        String pngPath = structure.path.getParent().resolve(PathUtils.getBaseName(structure.path) + ".png").toAbsolutePath().toString();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Path.of(dotPath).toFile()))) {
            writer.write(dotBuilder.toString());
        }

        dotPath = "/mnt/" + dotPath.split(":")[0].toLowerCase() + dotPath.substring(dotPath.indexOf(":") + 1);
        pngPath = "/mnt/" + pngPath.split(":")[0].toLowerCase() + pngPath.substring(pngPath.indexOf(":") + 1);

        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c",
                "dot -Tpng " +
                dotPath.replace("\\", "/") +
                " -o " +
                pngPath.replace("\\", "/"));

        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            System.out.println("PNG file generated successfully.");
        } else {
            System.out.println("Error generating PNG.");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println(line);
                }
            }
        }
    }
}
