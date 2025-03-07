package in.lakazatong.pcbmod.utils;

import in.lakazatong.pcbmod.redstone.Block;

import java.util.*;

public class SccGraph {
    private final Map<UUID, Block> original;

    public final Map<UUID, Set<Block>> graph = new HashMap<>();

    private final Map<UUID, Set<UUID>> inputs = new HashMap<>();

    public SccGraph(Map<UUID, Block> original) {
        this.original = original;
        buildSCCGraph();
    }

    void buildSCCGraph() {
        Map<UUID, List<UUID>> adjacencyList = new HashMap<>();
        for (Map.Entry<UUID, Block> entry : original.entrySet())
            adjacencyList.put(entry.getKey(), entry.getValue().inputs().map(i -> i.uuid).toList());

        Map<UUID, Integer> lowLink = new HashMap<>();
        Map<UUID, Integer> index = new HashMap<>();
        Set<UUID> onStack = new HashSet<>();
        Stack<UUID> stack = new Stack<>();
        int[] currentIndex = {0};

        for (UUID blockUuid : original.keySet()) {
            if (!index.containsKey(blockUuid))
                tarjan(blockUuid, adjacencyList, lowLink, index, onStack, stack, currentIndex);
        }
    }

    private void tarjan(UUID blockUuid, Map<UUID, List<UUID>> adjacencyList, Map<UUID, Integer> lowLink,
                        Map<UUID, Integer> index, Set<UUID> onStack, Stack<UUID> stack, int[] currentIndex) {
        index.put(blockUuid, currentIndex[0]);
        lowLink.put(blockUuid, currentIndex[0]);
        currentIndex[0]++;
        stack.push(blockUuid);
        onStack.add(blockUuid);

        for (UUID neighbor : adjacencyList.get(blockUuid)) {
            if (!index.containsKey(neighbor)) {
                tarjan(neighbor, adjacencyList, lowLink, index, onStack, stack, currentIndex);
                lowLink.put(blockUuid, Math.min(lowLink.get(blockUuid), lowLink.get(neighbor)));
            } else if (onStack.contains(neighbor)) {
                lowLink.put(blockUuid, Math.min(lowLink.get(blockUuid), index.get(neighbor)));
            }
        }

        if (lowLink.get(blockUuid).equals(index.get(blockUuid))) {
            Set<Block> sccBlocks = new HashSet<>();
            Set<UUID> sccUuids = new HashSet<>();
            UUID sccUuid = blockUuid;

            while (!stack.peek().equals(blockUuid)) {
                UUID node = stack.pop();
                onStack.remove(node);
                Block block = original.get(node);
                sccBlocks.add(block);
                sccUuids.add(node);
            }
            UUID node = stack.pop();
            onStack.remove(node);
            Block block = original.get(node);
            sccBlocks.add(block);
            sccUuids.add(node);

            graph.put(sccUuid, sccBlocks);

            for (UUID u : adjacencyList.get(blockUuid)) {
                if (!sccUuids.contains(u))
                    inputs.computeIfAbsent(sccUuid, k -> new HashSet<>()).add(u);
            }
        }
    }

    public Set<UUID> inputs(UUID sccUuid) {
        return inputs.get(sccUuid);
    }

    public Set<UUID> outputs(UUID sccUuid) {
        Set<UUID> outputs = new HashSet<>();
        for (Map.Entry<UUID, Set<UUID>> entry : inputs.entrySet()) {
            if (entry.getValue().contains(sccUuid))
                outputs.add(entry.getKey());
        }
        return outputs;
    }
}