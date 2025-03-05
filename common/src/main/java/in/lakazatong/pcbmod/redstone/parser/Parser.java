package in.lakazatong.pcbmod.redstone.parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Parser {
    private final Structure structure;
    private final Map<UUID, Map<String, Object>> graph;

    public Parser(Structure structure) {
        this.structure = structure;
        this.graph = new HashMap<>();
    }

    public Map<UUID, Map<String, Object>> parse() {
        Queue<Block> queue = new LinkedList<>();
        Set<UUID> visited = new HashSet<>();

        queue.add(structure.getFirstBlock());

        while (!queue.isEmpty()) {
            Block block = queue.poll();

            if (visited.contains(block.uuid))
                continue;
            visited.add(block.uuid);

            List<Block> blockInputs = new ArrayList<>();
            for (Block neighbor : structure.getNeighbors(block)) {
                if (neighbor.isInputOf(block))
                    blockInputs.add(neighbor);
                queue.add(neighbor);
            }

            Map<String, Object> blockInfo = new HashMap<>();
            blockInfo.put("block", block);
            blockInfo.put("inputs", blockInputs);

            graph.put(block.uuid, blockInfo);
        }

        return graph;
    }

    @SuppressWarnings("unchecked")
    public void saveGraphAsDot(Path path) throws IOException {
        StringBuilder dotBuilder = new StringBuilder();
        dotBuilder.append("digraph G {\n");

        Set<String> edges = new HashSet<>();

        dotBuilder.append("    graph [bgcolor=\"black\"];\n");

        for (Map.Entry<UUID, Map<String, Object>> entry : graph.entrySet()) {
            UUID blockUUID = entry.getKey();
            Map<String, Object> blockInfo = entry.getValue();
            Block block = (Block) blockInfo.get("block");
            List<Block> blockInputs = (List<Block>) blockInfo.get("inputs");

            String blockName = block.type.name().toLowerCase();

            dotBuilder.append("    \"").append(blockUUID).append("\" [label=\"").append(blockName).append("\", style=filled, fillcolor=\"black\", fontcolor=\"white\", color=\"white\", width=0.2, height=0.2];\n");

            for (Block input : blockInputs) {
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
