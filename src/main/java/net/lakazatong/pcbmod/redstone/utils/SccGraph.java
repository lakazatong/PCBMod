package net.lakazatong.pcbmod.redstone.utils;

import net.lakazatong.pcbmod.redstone.circuit.Block;

import java.util.*;

public class SccGraph {
    private final Map<Integer, Block> graph;
    public final List<Set<Block>> sccs = new ArrayList<>();
    public final List<Set<Integer>> outputs = new ArrayList<>();
    public final List<Set<Integer>> inputs = new ArrayList<>();
    public final Map<Integer, Integer> nodeToSCC = new HashMap<>();

    public SccGraph(Map<Integer, Block> inputGraph) {
        this.graph = inputGraph;
        buildSCC();
    }

    private void buildSCC() {
        Map<Integer, Integer> indices = new HashMap<>();
        Map<Integer, Integer> lowLink = new HashMap<>();
        Deque<Integer> stack = new ArrayDeque<>();
        Set<Integer> inStack = new HashSet<>();
        int[] index = {0};
        int[] sccId = {0};

        for (Block block : graph.values()) {
            if (!indices.containsKey(block.uuid))
                dfs(block, indices, lowLink, stack, inStack, index, sccId);
        }

        computeEdges();
    }

        private void dfs(Block block, Map<Integer, Integer> indices, Map<Integer, Integer> lowLink,
                Deque<Integer> stack, Set<Integer> inStack, int[] index, int[] sccId) {
        int id = block.uuid;
        indices.put(id, index[0]);
        lowLink.put(id, index[0]);
        index[0]++;
        stack.push(id);
        inStack.add(id);

        block.inputs().forEach(neighbor -> {
            int neighborId = neighbor.uuid;
            if (!indices.containsKey(neighborId)) {
                dfs(neighbor, indices, lowLink, stack, inStack, index, sccId);
                lowLink.put(id, Math.min(lowLink.get(id), lowLink.get(neighborId)));
            } else if (inStack.contains(neighborId)) {
                lowLink.put(id, Math.min(lowLink.get(id), indices.get(neighborId)));
            }
        });

        if (lowLink.get(id).equals(indices.get(id))) {
            Set<Block> scc = new HashSet<>();
            int v;
            do {
                v = stack.pop();
                inStack.remove(v);
                scc.add(graph.get(v));
                nodeToSCC.put(v, sccId[0]);
            } while (v != id);
            sccs.add(scc);
            outputs.add(new HashSet<>());
            inputs.add(new HashSet<>());
            sccId[0]++;
        }
    }

    private void computeEdges() {
        for (Block block : graph.values()) {
            int sccU = nodeToSCC.get(block.uuid);
            block.outputs().forEach(output -> {
                int sccV = nodeToSCC.get(output.uuid);
                if (sccU != sccV) {
                    outputs.get(sccU).add(sccV);
                    inputs.get(sccV).add(sccU);
                }
            });
        }
    }

    public Set<Integer> outputs(int sccId) {
        return outputs.get(sccId);
    }

    public Set<Integer> inputs(int sccId) {
        return inputs.get(sccId);
    }
}
