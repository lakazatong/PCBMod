package in.lakazatong.pcbmod.redstone;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Circuit {
    public final Map<UUID, Block> graph;
    private double time = 0; // in ticks

    private Circuit() {
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
        Circuit circuit = new Circuit();
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

        return circuit;
    }

    @SuppressWarnings("unchecked")
    public void saveAsDot(Path path) throws IOException {
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

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
            writer.write(dotBuilder.toString());
        }
    }
}
