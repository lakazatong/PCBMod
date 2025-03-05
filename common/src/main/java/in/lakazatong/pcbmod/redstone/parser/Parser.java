package in.lakazatong.pcbmod.redstone.parser;

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
            List<Block> blockOutputs = new ArrayList<>();
            for (Block neighbor : structure.getNeighbors(block)) {
                if (neighbor.isInputOf(block))
                    blockInputs.add(neighbor);
                if (neighbor.isOutputOf(block))
                    blockOutputs.add(neighbor);
                queue.add(neighbor);
            }

            Map<String, Object> blockInfo = new HashMap<>();
            blockInfo.put("block", block);
            blockInfo.put("inputs", blockInputs);
            blockInfo.put("outputs", blockOutputs);

            graph.put(block.uuid, blockInfo);
        }

        return graph;
    }
}
