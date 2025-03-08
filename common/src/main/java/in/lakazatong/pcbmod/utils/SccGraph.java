package in.lakazatong.pcbmod.utils;

import in.lakazatong.pcbmod.redstone.Block;

import java.util.*;

public class SccGraph {
//    private final Map<UUID, Block> graph;
    private final Map<String, Block> graph;
    public final List<Set<Block>> sccs = new ArrayList<>();
    public final List<Set<Integer>> outputs = new ArrayList<>();
    public final List<Set<Integer>> inputs = new ArrayList<>();
//    public final Map<UUID, Integer> nodeToSCC = new HashMap<>();
    public final Map<String, Integer> nodeToSCC = new HashMap<>();

//    public SccGraph(Map<UUID, Block> inputGraph) {
    public SccGraph(Map<String, Block> inputGraph) {
        this.graph = inputGraph;
        buildSCC();
    }

    private void buildSCC() {
//        Map<UUID, Integer> indices = new HashMap<>();
//        Map<UUID, Integer> lowLink = new HashMap<>();
//        Deque<UUID> stack = new ArrayDeque<>();
//        Set<UUID> inStack = new HashSet<>();
        Map<String, Integer> indices = new HashMap<>();
        Map<String, Integer> lowLink = new HashMap<>();
        Deque<String> stack = new ArrayDeque<>();
        Set<String> inStack = new HashSet<>();
        int[] index = {0};
        int[] sccId = {0};

        for (Block block : graph.values()) {
//            if (!indices.containsKey(block.uuid))
//                dfs(block, indices, lowLink, stack, inStack, index, sccId);
            if (!indices.containsKey(block.uuidDebug))
                dfs(block, indices, lowLink, stack, inStack, index, sccId);
        }

        computeEdges();
    }

//    private void dfs(Block block, Map<UUID, Integer> indices, Map<UUID, Integer> lowLink,
//                     Deque<UUID> stack, Set<UUID> inStack, int[] index, int[] sccId) {
        private void dfs(Block block, Map<String, Integer> indices, Map<String, Integer> lowLink,
                Deque<String> stack, Set<String> inStack, int[] index, int[] sccId) {
//        UUID id = block.uuid;
        String id = block.uuidDebug;
        indices.put(id, index[0]);
        lowLink.put(id, index[0]);
        index[0]++;
        stack.push(id);
        inStack.add(id);

        block.inputs().forEach(neighbor -> {
//            UUID neighborId = neighbor.uuid;
            String neighborId = neighbor.uuidDebug;
            if (!indices.containsKey(neighborId)) {
                dfs(neighbor, indices, lowLink, stack, inStack, index, sccId);
                lowLink.put(id, Math.min(lowLink.get(id), lowLink.get(neighborId)));
            } else if (inStack.contains(neighborId)) {
                lowLink.put(id, Math.min(lowLink.get(id), indices.get(neighborId)));
            }
        });

        if (lowLink.get(id).equals(indices.get(id))) {
            Set<Block> scc = new HashSet<>();
//            UUID v;
            String v;
            do {
                v = stack.pop();
                inStack.remove(v);
                scc.add(graph.get(v));
                nodeToSCC.put(v, sccId[0]);
            } while (!v.equals(id));
            sccs.add(scc);
            outputs.add(new HashSet<>());
            inputs.add(new HashSet<>());
            sccId[0]++;
        }
    }

    private void computeEdges() {
        for (Block block : graph.values()) {
//            int sccU = nodeToSCC.get(block.uuid);
            int sccU = nodeToSCC.get(block.uuidDebug);
            block.outputs().forEach(output -> {
//                int sccV = nodeToSCC.get(output.uuid);
                int sccV = nodeToSCC.get(output.uuidDebug);
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
