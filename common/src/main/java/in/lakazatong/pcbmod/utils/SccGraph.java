package in.lakazatong.pcbmod.utils;

import in.lakazatong.pcbmod.redstone.Block;

import java.util.*;

public class SccGraph {
    private final Map<UUID, Block> original;

    public final Map<UUID, Set<Block>> graph = new HashMap<>();

    private final Map<UUID, Set<UUID>> inputs = new HashMap<>();

    private final Map<UUID, UUID> blockToScc = new HashMap<>();

    public SccGraph(Map<UUID, Block> original) {
        this.original = original;
        buildSCCGraph();
    }

    void buildSCCGraph() {

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